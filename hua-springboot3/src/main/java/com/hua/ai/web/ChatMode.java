package com.hua.ai.web;

/**
 * 交互模式枚举（前端通过 mode 参数选择能力策略）
 */
public enum ChatMode {
    NORMAL,
    RAG,
    REPORT,
    STREAM;

    public static ChatMode from(String mode) {
        if (mode == null) {
            return NORMAL;
        }
        return switch (mode.trim().toLowerCase()) {
            case "normal" -> NORMAL;
            case "rag" -> RAG;
            case "report" -> REPORT;
            case "stream" -> STREAM;
            default -> NORMAL;
        };
    }
}

