package com.hua.kb.domain;

import com.hua.system.general.utils.StringUtils;

/**
 * 知识库四大模块（与前端约定一致）
 */
public enum KbModule {
    LEARNING_PATH("LEARNING_PATH", "学习路线"),
    PROJECT_LAB("PROJECT_LAB", "项目实战"),
    QNA("QNA", "知识点答疑"),
    DEV_TOOLCHAIN("DEV_TOOLCHAIN", "开发者工具链");

    private final String code;
    private final String label;

    KbModule(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static KbModule fromCode(String code) {
        if (StringUtils.isEmpty(code)) {
            throw new IllegalArgumentException("module 不能为空");
        }
        for (KbModule m : values()) {
            if (m.code.equalsIgnoreCase(code.trim())) {
                return m;
            }
        }
        throw new IllegalArgumentException("非法 module: " + code);
    }
}
