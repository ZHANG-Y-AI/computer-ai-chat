package com.hua.ai.web;

import java.util.ArrayList;
import java.util.List;

public class ChatResponse {
    private String mode;
    private int sessionId;
    private String answer;
    private long time;
    private List<Source> sources = new ArrayList<>();

    public ChatResponse() {
    }

    public ChatResponse(String mode, int sessionId, String answer, long time) {
        this(mode, sessionId, answer, time, new ArrayList<>());
    }

    public ChatResponse(String mode, int sessionId, String answer, long time, List<Source> sources) {
        this.mode = mode;
        this.sessionId = sessionId;
        this.answer = answer;
        this.time = time;
        this.sources = sources == null ? new ArrayList<>() : sources;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources == null ? new ArrayList<>() : sources;
    }

    public static class Source {
        private String fileName;
        private String preview;
        private Double score;

        public Source() {
        }

        public Source(String fileName, String preview, Double score) {
            this.fileName = fileName;
            this.preview = preview;
            this.score = score;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getPreview() {
            return preview;
        }

        public void setPreview(String preview) {
            this.preview = preview;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }
}

