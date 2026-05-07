package com.docmanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doc_approval_record")
public class ApprovalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private Long flowId;

    private Long nodeId;

    private Long approverId;

    private String status;

    private String comment;

    private Integer currentNodeOrder;

    @TableField(exist = false)
    private String documentTitle;

    @TableField(exist = false)
    private String approverName;

    @TableField(exist = false)
    private String nodeName;

    @TableField(exist = false)
    private String flowName;

    @TableField(exist = false)
    private String submitterName;

    @TableField(exist = false)
    private Long submitterId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
