package com.hua.kb.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * 将知识库文档写入/移出向量库（与 RagConfig 使用同一 EmbeddingStore）
 */
@Service
public class KbEmbeddingSyncService {

    private static final Logger log = LoggerFactory.getLogger(KbEmbeddingSyncService.class);

    private static final DocumentByParagraphSplitter SPLITTER = new DocumentByParagraphSplitter(1000, 200);

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 按 kb_doc_id 移除旧向量后重新索引整文件（UTF-8 文本）
     */
    public void ingestTextFile(Long docId, String moduleCode, String displayFileName, Path absoluteFile) throws Exception {
        removeByDocId(docId);
        String text = Files.readString(absoluteFile, StandardCharsets.UTF_8);
        Metadata meta = Metadata.metadata("file_name", displayFileName)
                .put("kb_doc_id", String.valueOf(docId))
                .put("kb_module", moduleCode);

        Document document = Document.from(text, meta);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(SPLITTER)
                .textSegmentTransformer(ts -> TextSegment.from(
                        "📚 [" + moduleCode + "] " + displayFileName + "\n" + ts.text(),
                        ts.metadata()))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);
    }

    public void removeByDocId(Long docId) {
        try {
            embeddingStore.removeAll(metadataKey("kb_doc_id").isEqualTo(String.valueOf(docId)));
        } catch (Exception e) {
            log.warn("remove embedding by kb_doc_id failed: {}", docId, e);
        }
    }
}
