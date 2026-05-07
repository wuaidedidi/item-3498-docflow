package com.docmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docmanager.entity.ApprovalFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApprovalFlowMapper extends BaseMapper<ApprovalFlow> {

    @Select("<script>" +
            "SELECT f.*, u.nickname AS creatorName " +
            "FROM doc_approval_flow f " +
            "LEFT JOIN sys_user u ON f.creator_id = u.id " +
            "WHERE f.deleted = 0 " +
            "<if test='name != null and name != \"\"'> AND f.name LIKE CONCAT('%', #{name}, '%') </if>" +
            "ORDER BY f.create_time DESC" +
            "</script>")
    List<ApprovalFlow> selectFlowList(@Param("name") String name);
}
