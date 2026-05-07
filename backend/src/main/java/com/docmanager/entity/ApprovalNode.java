package com.docmanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doc_approval_node")
public class ApprovalNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flowId;

    private String name;

    private Long approverId;

    private Integer nodeOrder;

    @TableField(exist = false)
    private String approverName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
