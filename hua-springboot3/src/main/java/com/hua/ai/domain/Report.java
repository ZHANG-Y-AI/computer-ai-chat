package com.hua.ai.domain;

import lombok.Data;

import java.util.List;

/**
 * 报告数据记录类
 * 用于封装报告相关的结构化数据，便于在系统中传递和处理报告信息
 */
@Data
public class Report {
    //报告名称或标识符，用于区分不同类型的报告
    private String name;
    //报告内容的消息列表，包含报告的具体段落或条目信息
    private List<String> msgList;
}
