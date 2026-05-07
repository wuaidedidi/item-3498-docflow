package com.docmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docmanager.entity.DocumentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentVersionMapper extends BaseMapper<DocumentVersion> {

    @Select("SELECT v.*, u.nickname AS uploaderName " +
            "FROM doc_document_version v " +
            "LEFT JOIN sys_user u ON v.uploader_id = u.id " +
            "WHERE v.document_id = #{documentId} " +
            "ORDER BY v.version DESC")
    List<DocumentVersion> selectByDocumentId(@Param("documentId") Long documentId);
}
