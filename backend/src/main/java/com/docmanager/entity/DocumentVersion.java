package com.docmanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doc_document_version")
public class DocumentVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private Integer version;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String changeLog;

    private Long uploaderId;

    private String approvalStatus;

    private Long approvalRecordId;

    @TableField(exist = false)
    private String uploaderName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
