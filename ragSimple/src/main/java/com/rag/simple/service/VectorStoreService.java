package com.rag.simple.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 向量检索服务 — 基于 PgVector 的语义相似度搜索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

    /**
     * 将文档列表向量化并写入 PgVector
     *
     * @param documents Spring AI Document 列表
     */
    public void addDocuments(List<Document> documents) {
        log.info("开始向量化并写入 PgVector, 文档数量: {}", documents.size());
        long startTime = System.currentTimeMillis();
        vectorStore.add(documents);
        log.info("向量化写入完成, 耗时 {}ms", System.currentTimeMillis() - startTime);
    }

    /**
     * 按语义相似度检索文档
     *
     * @param query 查询文本
     * @param topK  返回最相似的前 K 条
     * @return 相似文档列表
     */
    public List<Document> similaritySearch(String query, int topK) {
        log.info("执行向量相似度检索, query='{}', topK={}", query, topK);
        long startTime = System.currentTimeMillis();
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build()
        );
        log.info("向量检索完成, 返回 {} 条结果, 耗时 {}ms",
                results.size(), System.currentTimeMillis() - startTime);
        return results;
    }

    /**
     * 带阈值的语义相似度检索
     *
     * @param query            查询文本
     * @param topK             返回最相似的前 K 条
     * @param similarityThreshold 相似度阈值 (0.0 ~ 1.0)
     * @return 相似文档列表
     */
    public List<Document> similaritySearch(String query, int topK, double similarityThreshold) {
        log.info("执行向量相似度检索 (带阈值), query='{}', topK={}, threshold={}",
                query, topK, similarityThreshold);
        long startTime = System.currentTimeMillis();
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .build()
        );
        log.info("向量检索完成, 返回 {} 条结果, 耗时 {}ms",
                results.size(), System.currentTimeMillis() - startTime);
        return results;
    }
}
