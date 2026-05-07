package com.docmanager.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.docmanager.common.Result;
import com.docmanager.entity.Document;
import com.docmanager.entity.DocumentVersion;
import com.docmanager.entity.User;
import com.docmanager.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping
    public Result<IPage<Document>> getDocumentList(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) String title,
                                                    @RequestParam(required = false) Long categoryId,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) Long creatorId) {
        return Result.success(documentService.getDocumentList(page, size, title, categoryId, status, creatorId));
    }

    @GetMapping("/{id}")
    public Result<Document> getDocumentDetail(@PathVariable Long id) {
        return Result.success(documentService.getDocumentDetail(id));
    }

    @PostMapping
    public Result<Document> createDocument(@RequestParam String title,
                                            @RequestParam(required = false) String description,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam("file") MultipartFile file,
                                            @AuthenticationPrincipal User currentUser) {
        Document doc = documentService.createDocument(title, description, categoryId, file, currentUser);
        return Result.success("文档创建成功", doc);
    }

    @PutMapping("/{id}")
    public Result<Document> updateDocument(@PathVariable Long id,
                                            @RequestParam(required = false) String title,
                                            @RequestParam(required = false) String description,
                                            @RequestParam(required = false) Long categoryId,
                                            @AuthenticationPrincipal User currentUser) {
        Document doc = documentService.updateDocumentInfo(id, title, description, categoryId, currentUser);
        return Result.success("文档信息更新成功", doc);
    }

    @PostMapping("/{id}/versions")
    public Result<DocumentVersion> uploadNewVersion(@PathVariable Long id,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam(required = false) String changeLog,
                                                     @RequestParam(required = false) Long flowId,
                                                     @AuthenticationPrincipal User currentUser) {
        DocumentVersion version = documentService.uploadNewVersion(id, file, changeLog, flowId, currentUser);
        return Result.success("版本上传成功", version);
    }

    @GetMapping("/{id}/versions")
    public Result<List<DocumentVersion>> getDocumentVersions(@PathVariable Long id) {
        return Result.success(documentService.getDocumentVersions(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        documentService.deleteDocument(id, currentUser);
        return Result.success("文档删除成功", null);
    }

    @GetMapping("/download/{filePath}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filePath,
                                                  @RequestParam(required = false) String fileName) {
        File file = documentService.getFile(filePath);
        Resource resource = new FileSystemResource(file);

        String downloadName = fileName != null ? fileName : file.getName();
        String encodedFileName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }
}
