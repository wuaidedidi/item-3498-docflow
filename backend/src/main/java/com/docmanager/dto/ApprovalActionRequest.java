package com.docmanager.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ApprovalActionRequest {

    @NotNull(message = "审批记录ID不能为空")
    private Long recordId;

    @NotBlank(message = "审批操作不能为空")
    private String action;

    private String comment;
}
