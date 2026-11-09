package com.hua.kb.mapper;

import com.hua.kb.domain.KbDocument;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 知识库文档 Mapper
 */
public interface KbDocumentMapper {

    KbDocument selectKbDocumentById(Long docId);

    List<KbDocument> selectKbDocumentList(KbDocument query);

    int insertKbDocument(KbDocument doc);

    int updateKbDocument(KbDocument doc);

    int deleteKbDocumentById(Long docId);

    List<Map<String, Object>> countByIndexStatus(@Param("module") String module);

    List<Map<String, Object>> countByModule(@Param("module") String module);
}
