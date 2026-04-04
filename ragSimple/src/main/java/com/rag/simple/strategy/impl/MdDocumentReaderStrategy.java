package com.rag.simple.strategy.impl;

import com.rag.simple.strategy.DocumentReaderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MdDocumentReaderStrategy implements DocumentReaderStrategy {

    @Override
    public boolean supports(String fileType, String subType) {
        return "md".equalsIgnoreCase(fileType);
    }

    @Override
    public List<Document> read(Resource resource) {
        String fileName = resource.getFilename();
        log.info("开始解析 Markdown 文件: {}", fileName);
        long startTime = System.currentTimeMillis();

        try {
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withIncludeCodeBlock(true)
                    .withIncludeBlockquote(true)
                    .build();

            MarkdownDocumentReader mdReader = new MarkdownDocumentReader(resource, config);
            List<Document> documents = mdReader.get();

            // 统一挂载溯源元数据
            for (Document document : documents) {
                document.getMetadata().put("source_file", fileName);
                document.getMetadata().put("document_format", "md");
            }

            log.info("Markdown 文件 [{}] 解析完成, 共产出 {} 个 Document 片段, 耗时 {}ms",
                    fileName, documents.size(), System.currentTimeMillis() - startTime);
            return documents;
        } catch (Exception e) {
            log.error("解析 Markdown 文件 [{}] 时发生异常: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Markdown 文件解析异常: " + fileName, e);
        }
    }
}
