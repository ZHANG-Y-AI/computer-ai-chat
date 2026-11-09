package com.hua.kb.service;

import com.hua.kb.domain.KbDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IKbDocumentService {

    KbDocument selectKbDocumentById(Long docId);

    List<KbDocument> selectKbDocumentList(KbDocument query);

    KbDocument upload(String moduleCode, String title, MultipartFile file, String username) throws Exception;

    int deleteKbDocument(Long docId);

    void reindex(Long docId, String username) throws Exception;

    /**
     * 启用(0) / 停用(1)。停用会移除向量；启用会尝试重新写入向量。
     */
    void updateStatus(Long docId, String status, String username);

    /**
     * 全文 UTF-8 预览（仅 .txt / .md），有大小上限
     */
    String previewText(Long docId);

    /**
     * 按索引状态、模块聚合数量；module 为空表示全库
     */
    Map<String, Object> stats(String moduleCode);
}
