package com.docmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.docmanager.common.Result;
import com.docmanager.entity.*;
import com.docmanager.mapper.*;
import com.docmanager.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ApprovalService approvalService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<Document> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.eq(Document::getDeleted, 0);
        stats.put("totalDocuments", documentMapper.selectCount(docWrapper));

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeleted, 0);
        stats.put("totalUsers", userMapper.selectCount(userWrapper));

        LambdaQueryWrapper<Category> catWrapper = new LambdaQueryWrapper<>();
        catWrapper.eq(Category::getDeleted, 0);
        stats.put("totalCategories", categoryMapper.selectCount(catWrapper));

        Map<String, Long> approvalStats = approvalService.getApprovalStats(currentUser.getId());
        stats.put("pendingApprovals", approvalStats.get("pending"));
        stats.put("approvedCount", approvalStats.get("approved"));
        stats.put("rejectedCount", approvalStats.get("rejected"));

        return Result.success(stats);
    }
}
