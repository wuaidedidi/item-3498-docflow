package com.docmanager.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ApprovalFlowRequest {

    @NotBlank(message = "流程名称不能为空")
    private String name;

    private String description;

    @NotEmpty(message = "审批节点不能为空")
    private List<ApprovalNodeItem> nodes;

    @Data
    public static class ApprovalNodeItem {
        @NotBlank(message = "节点名称不能为空")
        private String name;
        @NotNull(message = "审批人不能为空")
        private Long approverId;
        private Integer nodeOrder;
    }
}
