package com.docmanager.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.docmanager.common.Result;
import com.docmanager.dto.ApprovalActionRequest;
import com.docmanager.dto.ApprovalFlowRequest;
import com.docmanager.entity.ApprovalFlow;
import com.docmanager.entity.ApprovalRecord;
import com.docmanager.entity.User;
import com.docmanager.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @GetMapping("/flows")
    public Result<List<ApprovalFlow>> getFlowList(@RequestParam(required = false) String name) {
        return Result.success(approvalService.getFlowList(name));
    }

    @GetMapping("/flows/{id}")
    public Result<Map<String, Object>> getFlowDetail(@PathVariable Long id) {
        return Result.success(approvalService.getFlowDetail(id));
    }

    @PostMapping("/flows")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ApprovalFlow> createFlow(@Valid @RequestBody ApprovalFlowRequest request,
                                            @AuthenticationPrincipal User currentUser) {
        ApprovalFlow flow = approvalService.createFlow(request, currentUser);
        return Result.success("审批流程创建成功", flow);
    }

    @PutMapping("/flows/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ApprovalFlow> updateFlow(@PathVariable Long id,
                                            @Valid @RequestBody ApprovalFlowRequest request,
                                            @AuthenticationPrincipal User currentUser) {
        ApprovalFlow flow = approvalService.updateFlow(id, request, currentUser);
        return Result.success("审批流程更新成功", flow);
    }

    @DeleteMapping("/flows/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteFlow(@PathVariable Long id) {
        approvalService.deleteFlow(id);
        return Result.success("审批流程删除成功", null);
    }

    @PutMapping("/flows/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleFlowStatus(@PathVariable Long id) {
        approvalService.toggleFlowStatus(id);
        return Result.success("状态切换成功", null);
    }

    @GetMapping("/records/my")
    public Result<IPage<ApprovalRecord>> getMyApprovals(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) String status,
                                                         @AuthenticationPrincipal User currentUser) {
        return Result.success(approvalService.getMyApprovals(page, size, currentUser.getId(), status));
    }

    @GetMapping("/records/document/{documentId}")
    public Result<List<ApprovalRecord>> getDocumentApprovalHistory(@PathVariable Long documentId) {
        return Result.success(approvalService.getDocumentApprovalHistory(documentId));
    }

    @PostMapping("/process")
    public Result<Void> processApproval(@Valid @RequestBody ApprovalActionRequest request,
                                         @AuthenticationPrincipal User currentUser) {
        approvalService.processApproval(request, currentUser);
        return Result.success("审批处理成功", null);
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> getApprovalStats(@AuthenticationPrincipal User currentUser) {
        return Result.success(approvalService.getApprovalStats(currentUser.getId()));
    }
}
