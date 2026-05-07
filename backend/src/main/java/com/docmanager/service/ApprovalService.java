package com.docmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.docmanager.dto.ApprovalActionRequest;
import com.docmanager.dto.ApprovalFlowRequest;
import com.docmanager.entity.*;
import com.docmanager.exception.BusinessException;
import com.docmanager.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalService.class);

    @Autowired
    private ApprovalFlowMapper approvalFlowMapper;

    @Autowired
    private ApprovalNodeMapper approvalNodeMapper;

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentVersionMapper documentVersionMapper;

    public List<ApprovalFlow> getFlowList(String name) {
        return approvalFlowMapper.selectFlowList(name);
    }

    public Map<String, Object> getFlowDetail(Long id) {
        ApprovalFlow flow = approvalFlowMapper.selectById(id);
        if (flow == null || flow.getDeleted() == 1) {
            throw new BusinessException("审批流程不存在");
        }
        List<ApprovalNode> nodes = approvalNodeMapper.selectByFlowId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("flow", flow);
        result.put("nodes", nodes);
        return result;
    }

    @Transactional
    public ApprovalFlow createFlow(ApprovalFlowRequest request, User currentUser) {
        ApprovalFlow flow = new ApprovalFlow();
        flow.setName(request.getName());
        flow.setDescription(request.getDescription());
        flow.setStatus(1);
        flow.setCreatorId(currentUser.getId());
        flow.setDeleted(0);
        approvalFlowMapper.insert(flow);

        int order = 1;
        for (ApprovalFlowRequest.ApprovalNodeItem nodeItem : request.getNodes()) {
            ApprovalNode node = new ApprovalNode();
            node.setFlowId(flow.getId());
            node.setName(nodeItem.getName());
            node.setApproverId(nodeItem.getApproverId());
            node.setNodeOrder(order++);
            approvalNodeMapper.insert(node);
        }

        logger.info("审批流程创建成功: {} by {}", flow.getName(), currentUser.getUsername());
        return flow;
    }

    @Transactional
    public ApprovalFlow updateFlow(Long id, ApprovalFlowRequest request, User currentUser) {
        ApprovalFlow flow = approvalFlowMapper.selectById(id);
        if (flow == null || flow.getDeleted() == 1) {
            throw new BusinessException("审批流程不存在");
        }

        LambdaQueryWrapper<ApprovalRecord> recordQuery = new LambdaQueryWrapper<>();
        recordQuery.eq(ApprovalRecord::getFlowId, id).eq(ApprovalRecord::getStatus, "pending");
        Long pendingCount = approvalRecordMapper.selectCount(recordQuery);
        if (pendingCount > 0) {
            throw new BusinessException("该流程有待审批的记录，无法修改");
        }

        flow.setName(request.getName());
        flow.setDescription(request.getDescription());
        approvalFlowMapper.updateById(flow);

        LambdaQueryWrapper<ApprovalNode> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ApprovalNode::getFlowId, id);
        approvalNodeMapper.delete(deleteWrapper);

        int order = 1;
        for (ApprovalFlowRequest.ApprovalNodeItem nodeItem : request.getNodes()) {
            ApprovalNode node = new ApprovalNode();
            node.setFlowId(flow.getId());
            node.setName(nodeItem.getName());
            node.setApproverId(nodeItem.getApproverId());
            node.setNodeOrder(order++);
            approvalNodeMapper.insert(node);
        }

        logger.info("审批流程更新成功: {} by {}", flow.getName(), currentUser.getUsername());
        return flow;
    }

    @Transactional
    public void deleteFlow(Long id) {
        ApprovalFlow flow = approvalFlowMapper.selectById(id);
        if (flow == null || flow.getDeleted() == 1) {
            throw new BusinessException("审批流程不存在");
        }

        LambdaQueryWrapper<ApprovalRecord> recordQuery = new LambdaQueryWrapper<>();
        recordQuery.eq(ApprovalRecord::getFlowId, id).eq(ApprovalRecord::getStatus, "pending");
        Long pendingCount = approvalRecordMapper.selectCount(recordQuery);
        if (pendingCount > 0) {
            throw new BusinessException("该流程有待审批的记录，无法删除");
        }

        approvalFlowMapper.deleteById(id);

        LambdaQueryWrapper<ApprovalNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(ApprovalNode::getFlowId, id);
        approvalNodeMapper.delete(nodeWrapper);

        logger.info("审批流程删除成功: {}", flow.getName());
    }

    public void toggleFlowStatus(Long id) {
        ApprovalFlow flow = approvalFlowMapper.selectById(id);
        if (flow == null || flow.getDeleted() == 1) {
            throw new BusinessException("审批流程不存在");
        }
        flow.setStatus(flow.getStatus() == 1 ? 0 : 1);
        approvalFlowMapper.updateById(flow);
        logger.info("审批流程状态切换: {} -> {}", flow.getName(), flow.getStatus() == 1 ? "启用" : "禁用");
    }

    public IPage<ApprovalRecord> getMyApprovals(int page, int size, Long approverId, String status) {
        Page<ApprovalRecord> pageParam = new Page<>(page, size);
        return approvalRecordMapper.selectRecordPage(pageParam, approverId, status, null);
    }

    public List<ApprovalRecord> getDocumentApprovalHistory(Long documentId) {
        return approvalRecordMapper.selectByDocumentId(documentId);
    }

    @Transactional
    public void processApproval(ApprovalActionRequest request, User currentUser) {
        ApprovalRecord record = approvalRecordMapper.selectById(request.getRecordId());
        if (record == null) {
            throw new BusinessException("审批记录不存在");
        }
        if (!"pending".equals(record.getStatus())) {
            throw new BusinessException("该审批已处理");
        }
        if (!record.getApproverId().equals(currentUser.getId())) {
            throw new BusinessException("无权处理此审批");
        }

        String action = request.getAction();
        record.setComment(request.getComment());

        Document document = documentMapper.selectById(record.getDocumentId());
        if (document == null) {
            throw new BusinessException("关联文档不存在");
        }

        if ("approve".equals(action)) {
            record.setStatus("approved");
            approvalRecordMapper.updateById(record);

            List<ApprovalNode> nodes = approvalNodeMapper.selectByFlowId(record.getFlowId());
            ApprovalNode currentNode = null;
            ApprovalNode nextNode = null;

            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getNodeOrder().equals(record.getCurrentNodeOrder())) {
                    currentNode = nodes.get(i);
                    if (i + 1 < nodes.size()) {
                        nextNode = nodes.get(i + 1);
                    }
                    break;
                }
            }

            if (nextNode != null) {
                ApprovalRecord nextRecord = new ApprovalRecord();
                nextRecord.setDocumentId(record.getDocumentId());
                nextRecord.setFlowId(record.getFlowId());
                nextRecord.setNodeId(nextNode.getId());
                nextRecord.setApproverId(nextNode.getApproverId());
                nextRecord.setStatus("pending");
                nextRecord.setCurrentNodeOrder(nextNode.getNodeOrder());
                approvalRecordMapper.insert(nextRecord);

                logger.info("审批通过，转下一节点: docId={}, nextNode={}", document.getId(), nextNode.getName());
            } else {
                LambdaQueryWrapper<DocumentVersion> versionQuery = new LambdaQueryWrapper<>();
                versionQuery.eq(DocumentVersion::getDocumentId, document.getId())
                        .eq(DocumentVersion::getApprovalStatus, "pending")
                        .orderByDesc(DocumentVersion::getVersion)
                        .last("LIMIT 1");
                DocumentVersion latestVersion = documentVersionMapper.selectOne(versionQuery);

                if (latestVersion != null) {
                    latestVersion.setApprovalStatus("approved");
                    documentVersionMapper.updateById(latestVersion);

                    document.setVersion(latestVersion.getVersion());
                    document.setFileName(latestVersion.getFileName());
                    document.setFilePath(latestVersion.getFilePath());
                    document.setFileSize(latestVersion.getFileSize());
                    document.setFileType(getFileExtension(latestVersion.getFileName()));
                    document.setStatus("published");
                    documentMapper.updateById(document);
                }

                logger.info("审批全部通过，文档发布: docId={}", document.getId());
            }
        } else if ("reject".equals(action)) {
            record.setStatus("rejected");
            approvalRecordMapper.updateById(record);

            LambdaQueryWrapper<DocumentVersion> versionQuery = new LambdaQueryWrapper<>();
            versionQuery.eq(DocumentVersion::getDocumentId, document.getId())
                    .eq(DocumentVersion::getApprovalStatus, "pending")
                    .orderByDesc(DocumentVersion::getVersion)
                    .last("LIMIT 1");
            DocumentVersion latestVersion = documentVersionMapper.selectOne(versionQuery);
            if (latestVersion != null) {
                latestVersion.setApprovalStatus("rejected");
                documentVersionMapper.updateById(latestVersion);
            }

            document.setStatus("rejected");
            documentMapper.updateById(document);

            logger.info("审批被拒绝: docId={}, by {}", document.getId(), currentUser.getUsername());
        } else {
            throw new BusinessException("无效的审批操作");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public Map<String, Long> getApprovalStats(Long userId) {
        Map<String, Long> stats = new HashMap<>();

        LambdaQueryWrapper<ApprovalRecord> pendingQuery = new LambdaQueryWrapper<>();
        pendingQuery.eq(ApprovalRecord::getApproverId, userId).eq(ApprovalRecord::getStatus, "pending");
        stats.put("pending", approvalRecordMapper.selectCount(pendingQuery));

        LambdaQueryWrapper<ApprovalRecord> approvedQuery = new LambdaQueryWrapper<>();
        approvedQuery.eq(ApprovalRecord::getApproverId, userId).eq(ApprovalRecord::getStatus, "approved");
        stats.put("approved", approvalRecordMapper.selectCount(approvedQuery));

        LambdaQueryWrapper<ApprovalRecord> rejectedQuery = new LambdaQueryWrapper<>();
        rejectedQuery.eq(ApprovalRecord::getApproverId, userId).eq(ApprovalRecord::getStatus, "rejected");
        stats.put("rejected", approvalRecordMapper.selectCount(rejectedQuery));

        return stats;
    }
}
