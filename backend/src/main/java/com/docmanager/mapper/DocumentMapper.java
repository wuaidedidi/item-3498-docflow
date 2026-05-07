package com.docmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.docmanager.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    @Select("<script>" +
            "SELECT d.*, u.nickname AS creatorName, c.name AS categoryName " +
            "FROM doc_document d " +
            "LEFT JOIN sys_user u ON d.creator_id = u.id " +
            "LEFT JOIN doc_category c ON d.category_id = c.id " +
            "WHERE d.deleted = 0 " +
            "<if test='title != null and title != \"\"'> AND d.title LIKE CONCAT('%', #{title}, '%') </if>" +
            "<if test='categoryId != null'> AND d.category_id = #{categoryId} </if>" +
            "<if test='status != null and status != \"\"'> AND d.status = #{status} </if>" +
            "<if test='creatorId != null'> AND d.creator_id = #{creatorId} </if>" +
            "ORDER BY d.update_time DESC" +
            "</script>")
    IPage<Document> selectDocumentPage(Page<Document> page,
                                        @Param("title") String title,
                                        @Param("categoryId") Long categoryId,
                                        @Param("status") String status,
                                        @Param("creatorId") Long creatorId);

    @Select("SELECT d.*, u.nickname AS creatorName, c.name AS categoryName " +
            "FROM doc_document d " +
            "LEFT JOIN sys_user u ON d.creator_id = u.id " +
            "LEFT JOIN doc_category c ON d.category_id = c.id " +
            "WHERE d.id = #{id} AND d.deleted = 0")
    Document selectDocumentDetail(@Param("id") Long id);
}
