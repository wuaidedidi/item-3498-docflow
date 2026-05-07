package com.docmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.docmanager.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    @Select("<script>" +
            "SELECT r.*, d.title AS documentTitle, u.nickname AS approverName, " +
            "n.name AS nodeName, f.name AS flowName, su.nickname AS submitterName, d.creator_id AS submitterId " +
            "FROM doc_approval_record r " +
            "LEFT JOIN doc_document d ON r.document_id = d.id " +
            "LEFT JOIN sys_user u ON r.approver_id = u.id " +
            "LEFT JOIN doc_approval_node n ON r.node_id = n.id " +
            "LEFT JOIN doc_approval_flow f ON r.flow_id = f.id " +
            "LEFT JOIN sys_user su ON d.creator_id = su.id " +
            "WHERE 1=1 " +
            "<if test='approverId != null'> AND r.approver_id = #{approverId} </if>" +
            "<if test='status != null and status != \"\"'> AND r.status = #{status} </if>" +
            "<if test='documentId != null'> AND r.document_id = #{documentId} </if>" +
            "ORDER BY r.create_time DESC" +
            "</script>")
    IPage<ApprovalRecord> selectRecordPage(Page<ApprovalRecord> page,
                                            @Param("approverId") Long approverId,
                                            @Param("status") String status,
                                            @Param("documentId") Long documentId);

    @Select("SELECT r.*, d.title AS documentTitle, u.nickname AS approverName, " +
            "n.name AS nodeName, f.name AS flowName " +
            "FROM doc_approval_record r " +
            "LEFT JOIN doc_document d ON r.document_id = d.id " +
            "LEFT JOIN sys_user u ON r.approver_id = u.id " +
            "LEFT JOIN doc_approval_node n ON r.node_id = n.id " +
            "LEFT JOIN doc_approval_flow f ON r.flow_id = f.id " +
            "WHERE r.document_id = #{documentId} " +
            "ORDER BY r.current_node_order ASC, r.create_time ASC")
    java.util.List<ApprovalRecord> selectByDocumentId(@Param("documentId") Long documentId);
}
