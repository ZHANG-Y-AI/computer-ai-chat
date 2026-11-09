package com.hua.kb.domain;

import com.hua.system.general.annotation.Excel;
import com.hua.system.general.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 知识库文档
 */
public class KbDocument extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long docId;

    @Excel(name = "模块")
    private String module;

    @Excel(name = "标题")
    private String title;

    @Excel(name = "文件名")
    private String fileName;

    /** 相对 profile 的路径 */
    private String filePath;

    /** 0待索引 1成功 2失败 */
    @Excel(name = "索引状态", readConverterExp = "0=待索引,1=已索引,2=失败")
    private String indexStatus;

    /** 0正常 1停用 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getIndexStatus() {
        return indexStatus;
    }

    public void setIndexStatus(String indexStatus) {
        this.indexStatus = indexStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("docId", docId)
                .append("module", module)
                .append("title", title)
                .append("fileName", fileName)
                .append("filePath", filePath)
                .append("indexStatus", indexStatus)
                .append("status", status)
                .toString();
    }
}
