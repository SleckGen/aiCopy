package com.rag.simple.service;

import com.rag.simple.entity.DocumentRecord;
import com.rag.simple.repository.DocumentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档记录服务 — 关系型数据库操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentRecordService {

    private final DocumentRecordRepository repository;

    /**
     * 将解析后的 Document 列表批量存入关系型数据库
     *
     * @param fileName 文件名
     * @param fileType 文件类型
     * @param documents 文档片段列表
     * @return 保存的记录列表
     */
    @Transactional
    public List<DocumentRecord> saveDocuments(String fileName, String fileType, List<Document> documents) {
        log.info("开始保存文档到关系型数据库, 文件: {}, 类型: {}, 分块数: {}", fileName, fileType, documents.size());
        long startTime = System.currentTimeMillis();

        List<DocumentRecord> records = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            DocumentRecord record = DocumentRecord.builder()
                    .fileName(fileName)
                    .fileType(fileType)
                    .content(doc.getText())
                    .chunkIndex(i)
                    .sourceInfo(buildSourceInfo(doc))
                    .build();
            records.add(record);
        }

        List<DocumentRecord> saved = repository.saveAll(records);
        log.info("关系型数据库保存完成, 共保存 {} 条记录, 耗时 {}ms",
                saved.size(), System.currentTimeMillis() - startTime);
        return saved;
    }

    /**
     * 按关键字检索文档内容
     */
    public List<DocumentRecord> searchByKeyword(String keyword) {
        log.info("执行关键字检索, keyword='{}'", keyword);
        return repository.searchByKeyword(keyword);
    }

    /**
     * 按文件名查找文档记录
     */
    public List<DocumentRecord> findByFileName(String fileName) {
        return repository.findByFileName(fileName);
    }

    /**
     * 按文件类型查找文档记录
     */
    public List<DocumentRecord> findByFileType(String fileType) {
        return repository.findByFileType(fileType);
    }

    /**
     * 获取所有文档记录
     */
    public List<DocumentRecord> findAll() {
        return repository.findAll();
    }

    /**
     * 从 Document 元数据中构建来源信息
     */
    private String buildSourceInfo(Document doc) {
        StringBuilder sb = new StringBuilder();
        if (doc.getMetadata() != null) {
            Object sourceFile = doc.getMetadata().get("source_file");
            Object pdfReadMode = doc.getMetadata().get("pdf_read_mode");
            Object pageNumber = doc.getMetadata().get("page_number");

            if (sourceFile != null) sb.append("file=").append(sourceFile);
            if (pdfReadMode != null) sb.append(", mode=").append(pdfReadMode);
            if (pageNumber != null) sb.append(", page=").append(pageNumber);
        }
        return sb.toString();
    }
}
