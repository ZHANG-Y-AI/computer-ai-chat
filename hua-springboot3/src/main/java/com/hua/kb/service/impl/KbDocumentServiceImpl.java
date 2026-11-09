package com.hua.kb.service.impl;

import com.hua.kb.domain.KbDocument;
import com.hua.kb.domain.KbModule;
import com.hua.kb.mapper.KbDocumentMapper;
import com.hua.kb.service.IKbDocumentService;
import com.hua.kb.service.KbEmbeddingSyncService;
import com.hua.system.general.constant.Constants;
import com.hua.system.general.config.HuaCaiConfig;
import com.hua.system.general.exception.ServiceException;
import com.hua.system.general.utils.StringUtils;
import com.hua.system.general.utils.file.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库文档：上传落盘 + 写库 + 向量索引
 */
@Service
public class KbDocumentServiceImpl implements IKbDocumentService {

    private static final String[] KB_EXTENSIONS = {"txt", "md"};
    private static final long PREVIEW_MAX_BYTES = 5L * 1024 * 1024;

    @Autowired
    private KbDocumentMapper kbDocumentMapper;

    @Autowired
    private KbEmbeddingSyncService kbEmbeddingSyncService;

    @Override
    public KbDocument selectKbDocumentById(Long docId) {
        return kbDocumentMapper.selectKbDocumentById(docId);
    }

    @Override
    public List<KbDocument> selectKbDocumentList(KbDocument query) {
        return kbDocumentMapper.selectKbDocumentList(query);
    }

    @Override
    public KbDocument upload(String moduleCode, String title, MultipartFile file, String username) throws Exception {
        KbModule.fromCode(moduleCode);
        if (StringUtils.isEmpty(title)) {
            throw new ServiceException("标题不能为空");
        }
        String baseDir = HuaCaiConfig.getProfile() + java.io.File.separator + "kb"
                + java.io.File.separator + moduleCode;
        String relative = FileUploadUtils.upload(baseDir, file, KB_EXTENSIONS);

        KbDocument doc = new KbDocument();
        doc.setModule(moduleCode);
        doc.setTitle(title.trim());
        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(relative);
        doc.setIndexStatus("0");
        doc.setStatus("0");
        doc.setCreateBy(username);
        kbDocumentMapper.insertKbDocument(doc);

        Path abs = resolveAbsolute(relative);
        try {
            kbEmbeddingSyncService.ingestTextFile(doc.getDocId(), moduleCode, doc.getFileName(), abs);
            doc.setIndexStatus("1");
        } catch (Exception ex) {
            doc.setIndexStatus("2");
            doc.setRemark(StringUtils.substring(ex.getMessage(), 0, 500));
        }
        doc.setUpdateBy(username);
        kbDocumentMapper.updateKbDocument(doc);
        return kbDocumentMapper.selectKbDocumentById(doc.getDocId());
    }

    @Override
    public int deleteKbDocument(Long docId) {
        KbDocument doc = kbDocumentMapper.selectKbDocumentById(docId);
        if (doc == null) {
            return 0;
        }
        kbEmbeddingSyncService.removeByDocId(docId);
        java.io.File f = resolveAbsolute(doc.getFilePath()).toFile();
        if (f.exists()) {
            // noinspection ResultOfMethodCallIgnored
            f.delete();
        }
        return kbDocumentMapper.deleteKbDocumentById(docId);
    }

    @Override
    public void updateStatus(Long docId, String status, String username) {
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("非法状态");
        }
        KbDocument doc = kbDocumentMapper.selectKbDocumentById(docId);
        if (doc == null) {
            throw new ServiceException("文档不存在");
        }
        if ("1".equals(status)) {
            kbEmbeddingSyncService.removeByDocId(docId);
            doc.setStatus("1");
            doc.setIndexStatus("0");
            doc.setUpdateBy(username);
            kbDocumentMapper.updateKbDocument(doc);
            return;
        }
        doc.setStatus("0");
        Path abs = resolveAbsolute(doc.getFilePath());
        try {
            kbEmbeddingSyncService.ingestTextFile(docId, doc.getModule(), doc.getFileName(), abs);
            doc.setIndexStatus("1");
            doc.setRemark("");
        } catch (Exception ex) {
            doc.setIndexStatus("2");
            doc.setRemark(StringUtils.substring(ex.getMessage(), 0, 500));
        }
        doc.setUpdateBy(username);
        kbDocumentMapper.updateKbDocument(doc);
    }

    @Override
    public void reindex(Long docId, String username) throws Exception {
        KbDocument doc = kbDocumentMapper.selectKbDocumentById(docId);
        if (doc == null) {
            throw new ServiceException("文档不存在");
        }
        if (!"0".equals(doc.getStatus())) {
            throw new ServiceException("已停用文档不可重建索引");
        }
        Path abs = resolveAbsolute(doc.getFilePath());
        try {
            kbEmbeddingSyncService.ingestTextFile(docId, doc.getModule(), doc.getFileName(), abs);
            doc.setIndexStatus("1");
            doc.setRemark(null);
        } catch (Exception ex) {
            doc.setIndexStatus("2");
            doc.setRemark(StringUtils.substring(ex.getMessage(), 0, 500));
            throw ex;
        } finally {
            doc.setUpdateBy(username);
            kbDocumentMapper.updateKbDocument(doc);
        }
    }

    @Override
    public String previewText(Long docId) {
        KbDocument doc = kbDocumentMapper.selectKbDocumentById(docId);
        if (doc == null) {
            throw new ServiceException("文档不存在");
        }
        String fn = doc.getFileName();
        if (StringUtils.isEmpty(fn)) {
            throw new ServiceException("无法预览");
        }
        String lower = fn.toLowerCase();
        if (!lower.endsWith(".txt") && !lower.endsWith(".md")) {
            throw new ServiceException("仅支持预览 .txt / .md 文件");
        }
        Path abs = resolveAbsolute(doc.getFilePath());
        Path profileBase = Paths.get(HuaCaiConfig.getProfile()).toAbsolutePath().normalize();
        Path norm = abs.toAbsolutePath().normalize();
        if (!norm.startsWith(profileBase)) {
            throw new ServiceException("非法文件路径");
        }
        if (!Files.isRegularFile(norm)) {
            throw new ServiceException("文件不存在");
        }
        try {
            long size = Files.size(norm);
            if (size > PREVIEW_MAX_BYTES) {
                throw new ServiceException("文件过大，请下载后查看（预览上限 5MB）");
            }
            return Files.readString(norm, StandardCharsets.UTF_8);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("读取文件失败：" + ex.getMessage());
        }
    }

    @Override
    public Map<String, Object> stats(String moduleCode) {
        if (StringUtils.isNotEmpty(moduleCode)) {
            KbModule.fromCode(moduleCode);
        }
        String moduleParam = StringUtils.isEmpty(moduleCode) ? null : moduleCode;
        Map<String, Long> byIndexStatus = new LinkedHashMap<>();
        for (Map<String, Object> row : kbDocumentMapper.countByIndexStatus(moduleParam)) {
            Object k = row.get("ikey");
            Object c = row.get("mcnt");
            if (c instanceof Number) {
                byIndexStatus.put(k == null ? "" : k.toString(), ((Number) c).longValue());
            }
        }
        Map<String, Long> byModule = new LinkedHashMap<>();
        for (Map<String, Object> row : kbDocumentMapper.countByModule(moduleParam)) {
            Object k = row.get("mkey");
            Object c = row.get("mcnt");
            if (c instanceof Number) {
                byModule.put(k == null ? "" : k.toString(), ((Number) c).longValue());
            }
        }
        long total = byIndexStatus.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", total);
        out.put("byIndexStatus", byIndexStatus);
        out.put("byModule", byModule);
        return out;
    }

    private static Path resolveAbsolute(String resourcePath) {
        String localPath = HuaCaiConfig.getProfile();
        String after = StringUtils.substringAfter(resourcePath, Constants.RESOURCE_PREFIX);
        return Paths.get(localPath + after);
    }
}
