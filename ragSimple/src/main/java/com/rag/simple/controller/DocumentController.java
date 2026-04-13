package com.rag.simple.controller;

import com.rag.simple.entity.DocumentRecord;
import com.rag.simple.factory.DocumentReaderFactory;
import com.rag.simple.service.DocumentRecordService;
import com.rag.simple.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档管理 REST API — 向量检索 + 关系型检索
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final VectorStoreService vectorStoreService;
    private final DocumentRecordService documentRecordService;
    private final DocumentReaderFactory documentReaderFactory;

    /**
     * 解析文件并同时写入向量库 + 关系库
     *
     * @param filePath 本地文件绝对路径
     * @param subType  PDF 解析模式 (page/outline)，可选
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestDocument(
            @RequestParam String filePath,
            @RequestParam(required = false) String subType) {

        log.info("开始文档摄入, filePath={}, subType={}", filePath, subType);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 读取文件
            Resource resource = new FileSystemResource(filePath);
            String fileName = resource.getFilename();
            if (fileName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "无法获取文件名"));
            }

            // 2. 使用策略工厂解析文档
            var strategy = documentReaderFactory.getStrategy(fileName, subType);
            List<Document> documents = strategy.read(resource);

            // 3. 提取文件类型
            String fileType = fileName.substring(fileName.lastIndexOf('.') + 1);

            // 4. 写入向量库
            vectorStoreService.addDocuments(documents);

            // 5. 写入关系库
            List<DocumentRecord> records = documentRecordService.saveDocuments(fileName, fileType, documents);

            long elapsed = System.currentTimeMillis() - startTime;
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("fileName", fileName);
            result.put("fileType", fileType);
            result.put("chunkCount", documents.size());
            result.put("dbRecordCount", records.size());
            result.put("elapsedMs", elapsed);

            log.info("文档摄入完成: {}, chunks={}, elapsed={}ms", fileName, documents.size(), elapsed);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("文档摄入异常: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 向量语义检索
     *
     * @param query 查询文本
     * @param topK  返回前 K 条，默认 5
     * @param threshold 相似度阈值 (0.0 ~ 1.0)，可选
     */
    @GetMapping("/vector-search")
    public ResponseEntity<Map<String, Object>> vectorSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(required = false) Double threshold) {

        log.info("向量检索请求, query='{}', topK={}, threshold={}", query, topK, threshold);

        List<Document> results;
        if (threshold != null) {
            results = vectorStoreService.similaritySearch(query, topK, threshold);
        } else {
            results = vectorStoreService.similaritySearch(query, topK);
        }

        List<Map<String, Object>> items = results.stream()
                .map(this::documentToMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("resultCount", items.size());
        response.put("results", items);

        return ResponseEntity.ok(response);
    }

    /**
     * 关系型关键字检索
     *
     * @param keyword 搜索关键字
     */
    @GetMapping("/keyword-search")
    public ResponseEntity<Map<String, Object>> keywordSearch(@RequestParam String keyword) {

        log.info("关键字检索请求, keyword='{}'", keyword);

        List<DocumentRecord> results = documentRecordService.searchByKeyword(keyword);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("keyword", keyword);
        response.put("resultCount", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    /**
     * 混合检索 — 同时使用向量检索 + 关键字检索，合并去重
     *
     * @param query   查询文本
     * @param keyword 关键字 (可选，默认与 query 相同)
     * @param topK    向量检索返回数量
     */
    @GetMapping("/hybrid-search")
    public ResponseEntity<Map<String, Object>> hybridSearch(
            @RequestParam String query,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "5") int topK) {

        log.info("混合检索请求, query='{}', keyword='{}', topK={}", query, keyword, topK);
        String actualKeyword = (keyword != null) ? keyword : query;

        // 1. 向量检索
        List<Document> vectorResults = vectorStoreService.similaritySearch(query, topK);

        // 2. 关键字检索
        List<DocumentRecord> keywordResults = documentRecordService.searchByKeyword(actualKeyword);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("keyword", actualKeyword);

        // 向量检索结果
        List<Map<String, Object>> vectorItems = vectorResults.stream()
                .map(this::documentToMap)
                .collect(Collectors.toList());
        response.put("vectorResults", Map.of(
                "count", vectorItems.size(),
                "items", vectorItems
        ));

        // 关键字检索结果
        response.put("keywordResults", Map.of(
                "count", keywordResults.size(),
                "items", keywordResults
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * 按文件名 / 类型列出文档记录
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listDocuments(
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String fileType) {

        List<DocumentRecord> records;
        if (fileName != null) {
            records = documentRecordService.findByFileName(fileName);
        } else if (fileType != null) {
            records = documentRecordService.findByFileType(fileType);
        } else {
            records = documentRecordService.findAll();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalCount", records.size());
        response.put("records", records);

        return ResponseEntity.ok(response);
    }

    /**
     * 将 Spring AI Document 转换为 Map (便于 JSON 序列化)
     */
    private Map<String, Object> documentToMap(Document doc) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", doc.getId());
        map.put("content", doc.getText());
        map.put("metadata", doc.getMetadata());
        return map;
    }
}
