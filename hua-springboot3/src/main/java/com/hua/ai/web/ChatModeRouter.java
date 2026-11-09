package com.hua.ai.web;

import com.hua.ai.domain.Report;
import com.hua.ai.service.DialogueService;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.service.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class ChatModeRouter {

    @Autowired
    private DialogueService dialogueService;

    public ChatResponse chat(ChatRequest req) {
        ChatMode mode = ChatMode.from(req.getMode());
        String message = req.getMessage() == null ? "" : req.getMessage();
        int sessionId = req.getSessionId();

        String answer;
        List<ChatResponse.Source> sources = new ArrayList<>();
        switch (mode) {
            case RAG -> {
                Result<String> res = dialogueService.getChatRag(sessionId, message);
                answer = res == null ? "" : res.content();
                sources = extractSources(res);
            }
            case REPORT -> {
                Report report = dialogueService.getReport(sessionId, message);
                if (report == null) {
                    answer = "";
                } else {
                    StringJoiner sj = new StringJoiner("\n");
                    if (report.getName() != null && !report.getName().isBlank()) {
                        sj.add(report.getName());
                    }
                    if (report.getMsgList() != null) {
                        for (String line : report.getMsgList()) {
                            if (line != null && !line.isBlank()) {
                                sj.add(line);
                            }
                        }
                    }
                    answer = sj.toString();
                }
            }
            case STREAM -> {
                throw new IllegalArgumentException("stream 模式请使用 /chat/stream 接口");
            }
            case NORMAL -> answer = dialogueService.chat(sessionId, message);
            default -> answer = dialogueService.chat(sessionId, message);
        }

        String modeStr = mode.name().toLowerCase();
        return new ChatResponse(modeStr, sessionId, answer, System.currentTimeMillis(), sources);
    }

    public Flux<String> stream(int sessionId, String message) {
        return dialogueService.sseChat(sessionId, message);
    }

    private List<ChatResponse.Source> extractSources(Result<String> result) {
        if (result == null || result.sources() == null || result.sources().isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, ChatResponse.Source> uniqueSources = new LinkedHashMap<>();
        for (Content content : result.sources()) {
            if (content == null || content.textSegment() == null) {
                continue;
            }

            String fileName = content.textSegment().metadata().getString("file_name");
            String text = content.textSegment().text();
            if (fileName == null || fileName.isBlank()) {
                fileName = extractFileNameFromText(text);
            }
            if (fileName == null || fileName.isBlank()) {
                fileName = "未知来源";
            }

            Double score = null;
            Object scoreValue = content.metadata() == null ? null : content.metadata().get(ContentMetadata.SCORE);
            if (scoreValue instanceof Number number) {
                score = number.doubleValue();
            }

            String preview = buildPreview(text, fileName);
            String key = fileName + "|" + preview;
            uniqueSources.putIfAbsent(key, new ChatResponse.Source(fileName, preview, score));
        }
        return new ArrayList<>(uniqueSources.values());
    }

    private String extractFileNameFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String[] lines = text.split("\\R", 2);
        if (lines.length == 0) {
            return null;
        }
        String firstLine = lines[0].trim();
        if (firstLine.startsWith("📄")) {
            return firstLine.substring(2).trim();
        }
        return null;
    }

    private String buildPreview(String text, String fileName) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String preview = text.trim();
        if (preview.startsWith("📄")) {
            int firstLineEnd = preview.indexOf('\n');
            if (firstLineEnd >= 0) {
                preview = preview.substring(firstLineEnd + 1).trim();
            } else {
                preview = preview.replace("📄", "").replace(fileName, "").trim();
            }
        }
        preview = preview.replaceAll("\\s+", " ");
        return preview.length() > 120 ? preview.substring(0, 120) + "..." : preview;
    }
}

