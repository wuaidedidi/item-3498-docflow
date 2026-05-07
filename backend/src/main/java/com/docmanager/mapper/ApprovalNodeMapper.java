package com.docmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docmanager.entity.ApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApprovalNodeMapper extends BaseMapper<ApprovalNode> {

    @Select("SELECT n.*, u.nickname AS approverName " +
            "FROM doc_approval_node n " +
            "LEFT JOIN sys_user u ON n.approver_id = u.id " +
            "WHERE n.flow_id = #{flowId} " +
            "ORDER BY n.node_order ASC")
    List<ApprovalNode> selectByFlowId(@Param("flowId") Long flowId);
}
