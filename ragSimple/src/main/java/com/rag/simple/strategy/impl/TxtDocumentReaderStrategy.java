package com.rag.simple.strategy.impl;

import com.rag.simple.strategy.DocumentReaderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class TxtDocumentReaderStrategy implements DocumentReaderStrategy {

    @Override
    public boolean supports(String fileType, String subType) {
        return "txt".equalsIgnoreCase(fileType);
    }

    @Override
    public List<Document> read(Resource resource) {
        String fileName = resource.getFilename();
        log.info("开始解析 TXT 文件: {}", fileName);
        long startTime = System.currentTimeMillis();

        try {
            TextReader textReader = new TextReader(resource);
            textReader.getCustomMetadata().put("source_file", fileName);
            textReader.getCustomMetadata().put("document_format", "txt");
            textReader.setCharset(StandardCharsets.UTF_8);

            List<Document> documents = textReader.read();
            log.info("TXT 文件 [{}] 解析完成, 共产出 {} 个 Document 片段, 耗时 {}ms",
                    fileName, documents.size(), System.currentTimeMillis() - startTime);
            return documents;
        } catch (Exception e) {
            log.error("解析 TXT 文件 [{}] 时发生异常: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("TXT 文件解析异常: " + fileName, e);
        }
    }
}
