package com.hua.kb.controller;

import com.hua.kb.domain.KbDocument;
import com.hua.kb.domain.KbModule;
import com.hua.kb.service.IKbDocumentService;
import com.hua.system.general.core.controller.BaseController;
import com.hua.system.general.core.domain.AjaxResult;
import com.hua.system.general.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库文档管理
 */
@RestController
@RequestMapping("/kb/document")
public class KbDocumentController extends BaseController {

    @Autowired
    private IKbDocumentService kbDocumentService;

    @PreAuthorize("@ss.hasPermi('kb:knowledge:list')")
    @GetMapping("/modules")
    public AjaxResult modules() {
        List<Map<String, String>> list = Arrays.stream(KbModule.values())
                .map(m -> {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("code", m.getCode());
                    row.put("label", m.getLabel());
                    return row;
                })
                .collect(Collectors.toList());
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('kb:knowledge:list')")
    @GetMapping("/list")
    public TableDataInfo list(KbDocument query) {
        startPage();
        List<KbDocument> list = kbDocumentService.selectKbDocumentList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('kb:knowledge:list')")
    @GetMapping("/stats")
    public AjaxResult stats(@RequestParam(value = "module", required = false) String module) {
        return AjaxResult.success(kbDocumentService.stats(module));
    }

    @PreAuthorize("@ss.hasPermi('kb:doc:query')")
    @GetMapping("/{docId}/preview")
    public AjaxResult preview(@PathVariable Long docId) {
        String content = kbDocumentService.previewText(docId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", content);
        return AjaxResult.success(body);
    }

    @PreAuthorize("@ss.hasPermi('kb:knowledge:list')")
    @GetMapping("/{docId}")
    public AjaxResult getInfo(@PathVariable Long docId) {
        return AjaxResult.success(kbDocumentService.selectKbDocumentById(docId));
    }

    @PreAuthorize("@ss.hasPermi('kb:doc:add')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("module") String module,
                             @RequestParam("title") String title,
                             @RequestPart("file") MultipartFile file) throws Exception {
        KbDocument doc = kbDocumentService.upload(module, title, file, getUsername());
        return AjaxResult.success(doc);
    }

    @PreAuthorize("@ss.hasPermi('kb:doc:remove')")
    @DeleteMapping("/{docId}")
    public AjaxResult remove(@PathVariable Long docId) {
        return toAjax(kbDocumentService.deleteKbDocument(docId));
    }

    @PreAuthorize("@ss.hasPermi('kb:doc:index')")
    @PostMapping("/reindex/{docId}")
    public AjaxResult reindex(@PathVariable Long docId) throws Exception {
        kbDocumentService.reindex(docId, getUsername());
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('kb:knowledge:list')")
    @PutMapping("/status/{docId}")
    public AjaxResult updateStatus(@PathVariable Long docId, @RequestParam("status") String status) {
        kbDocumentService.updateStatus(docId, status, getUsername());
        return AjaxResult.success(kbDocumentService.selectKbDocumentById(docId));
    }
}
