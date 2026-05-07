package com.docmanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doc_document")
public class Document {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Integer version;

    private String status;

    private Long categoryId;

    private Long creatorId;

    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private String categoryName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
