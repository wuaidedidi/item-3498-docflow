package com.docmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.docmanager.entity.*;
import com.docmanager.exception.BusinessException;
import com.docmanager.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentVersionMapper documentVersionMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    @Autowired
    private ApprovalFlowMapper approvalFlowMapper;

    @Autowired
    private ApprovalNodeMapper approvalNodeMapper;

    public IPage<Document> getDocumentList(int page, int size, String title, Long categoryId, String status, Long creatorId) {
        Page<Document> pageParam = new Page<>(page, size);
        return documentMapper.selectDocumentPage(pageParam, title, categoryId, status, creatorId);
    }

    public Document getDocumentDetail(Long id) {
        Document doc = documentMapper.selectDocumentDetail(id);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }
        return doc;
    }

    @Transactional
    public Document createDocument(String title, String description, Long categoryId, MultipartFile file, User currentUser) {
        if (categoryId != null) {
            Category category = categoryMapper.selectById(categoryId);
            if (category == null || category.getDeleted() == 1) {
                throw new BusinessException("分类不存在");
            }
        }

        String savedFileName = saveFile(file);
        String originalFileName = file.getOriginalFilename();

        Document document = new Document();
        document.setTitle(title);
        document.setDescription(description);
        document.setCategoryId(categoryId);
        document.setFileName(originalFileName);
        document.setFilePath(savedFileName);
        document.setFileSize(file.getSize());
        document.setFileType(getFileExtension(originalFileName));
        document.setVersion(1);
        document.setStatus("published");
        document.setCreatorId(currentUser.getId());
        document.setDeleted(0);
        documentMapper.insert(document);

        DocumentVersion version = new DocumentVersion();
        version.setDocumentId(document.getId());
        version.setVersion(1);
        version.setFileName(originalFileName);
        version.setFilePath(savedFileName);
        version.setFileSize(file.getSize());
        version.setChangeLog("初始版本");
        version.setUploaderId(currentUser.getId());
        version.setApprovalStatus("approved");
        documentVersionMapper.insert(version);

        logger.info("文档创建成功: {} by {}", title, currentUser.getUsername());
        return documentMapper.selectDocumentDetail(document.getId());
    }

    @Transactional
    public Document updateDocumentInfo(Long id, String title, String description, Long categoryId, User currentUser) {
        Document document = documentMapper.selectById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }

        if (title != null) document.setTitle(title);
        if (description != null) document.setDescription(description);
        if (categoryId != null) document.setCategoryId(categoryId);
        documentMapper.updateById(document);

        logger.info("文档信息更新成功: {} by {}", document.getTitle(), currentUser.getUsername());
        return documentMapper.selectDocumentDetail(id);
    }

    @Transactional
    public DocumentVersion uploadNewVersion(Long documentId, MultipartFile file, String changeLog, Long flowId, User currentUser) {
        Document document = documentMapper.selectById(documentId);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }

        String savedFileName = saveFile(file);
        String originalFileName = file.getOriginalFilename();
        int newVersion = document.getVersion() + 1;

        DocumentVersion version = new DocumentVersion();
        version.setDocumentId(documentId);
        version.setVersion(newVersion);
        version.setFileName(originalFileName);
        version.setFilePath(savedFileName);
        version.setFileSize(file.getSize());
        version.setChangeLog(changeLog);
        version.setUploaderId(currentUser.getId());

        if (flowId != null) {
            ApprovalFlow flow = approvalFlowMapper.selectById(flowId);
            if (flow == null || flow.getDeleted() == 1 || flow.getStatus() != 1) {
                throw new BusinessException("审批流程不存在或未启用");
            }

            List<ApprovalNode> nodes = approvalNodeMapper.selectByFlowId(flowId);
            if (nodes.isEmpty()) {
                throw new BusinessException("审批流程没有配置审批节点");
            }

            version.setApprovalStatus("pending");
            documentVersionMapper.insert(version);

            document.setStatus("pending_approval");
            documentMapper.updateById(document);

            ApprovalNode firstNode = nodes.get(0);
            ApprovalRecord record = new ApprovalRecord();
            record.setDocumentId(documentId);
            record.setFlowId(flowId);
            record.setNodeId(firstNode.getId());
            record.setApproverId(firstNode.getApproverId());
            record.setStatus("pending");
            record.setCurrentNodeOrder(firstNode.getNodeOrder());
            approvalRecordMapper.insert(record);

            version.setApprovalRecordId(record.getId());
            documentVersionMapper.updateById(version);

            logger.info("文档版本上传并提交审批: docId={}, version={}, flowId={}", documentId, newVersion, flowId);
        } else {
            version.setApprovalStatus("approved");
            documentVersionMapper.insert(version);

            document.setVersion(newVersion);
            document.setFileName(originalFileName);
            document.setFilePath(savedFileName);
            document.setFileSize(file.getSize());
            document.setFileType(getFileExtension(originalFileName));
            document.setStatus("published");
            documentMapper.updateById(document);

            logger.info("文档版本直接更新: docId={}, version={}", documentId, newVersion);
        }

        return version;
    }

    public List<DocumentVersion> getDocumentVersions(Long documentId) {
        return documentVersionMapper.selectByDocumentId(documentId);
    }

    @Transactional
    public void deleteDocument(Long id, User currentUser) {
        Document document = documentMapper.selectById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException("文档不存在");
        }

        LambdaQueryWrapper<ApprovalRecord> recordQuery = new LambdaQueryWrapper<>();
        recordQuery.eq(ApprovalRecord::getDocumentId, id).eq(ApprovalRecord::getStatus, "pending");
        Long pendingCount = approvalRecordMapper.selectCount(recordQuery);
        if (pendingCount > 0) {
            throw new BusinessException("该文档有待审批的记录，无法删除");
        }

        documentMapper.deleteById(id);
        logger.info("文档删除成功: {} by {}", document.getTitle(), currentUser.getUsername());
    }

    public File getFile(String filePath) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path path = uploadPath.resolve(filePath).normalize();
        if (!path.startsWith(uploadPath)) {
            throw new BusinessException("非法文件路径");
        }
        File file = path.toFile();
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }
        return file;
    }

    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String extension = getFileExtension(file.getOriginalFilename());
            String savedFileName = UUID.randomUUID().toString() + "." + extension;
            Path filePath = uploadPath.resolve(savedFileName);
            file.transferTo(filePath.toFile());
            return savedFileName;
        } catch (IOException e) {
            logger.error("文件保存失败: {}", e.getMessage());
            throw new BusinessException("文件上传失败，请重试");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
