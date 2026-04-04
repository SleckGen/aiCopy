package com.rag.simple.strategy.impl;

import com.rag.simple.strategy.DocumentReaderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PdfOutlineReaderStrategy implements DocumentReaderStrategy {

    @Override
    public boolean supports(String fileType, String subType) {
        return "pdf".equalsIgnoreCase(fileType) && "outline".equalsIgnoreCase(subType);
    }

    @Override
    public List<Document> read(Resource resource) {
        String fileName = resource.getFilename();
        log.info("开始按目录模式解析 PDF 文件: {}", fileName);
        long startTime = System.currentTimeMillis();

        try {
            ExtractedTextFormatter textFormatter = ExtractedTextFormatter.builder()
                    .withNumberOfTopTextLinesToDelete(0)
                    .withNumberOfBottomTextLinesToDelete(0)
                    .build();

            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(textFormatter)
                    .build();

            ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(resource, config);
            List<Document> documents = pdfReader.read();

            // 统一挂载溯源元数据
            for (Document document : documents) {
                document.getMetadata().put("source_file", fileName);
                document.getMetadata().put("document_format", "pdf");
                document.getMetadata().put("pdf_read_mode", "outline");
            }

            log.info("PDF 文件 [{}] 目录解析完成, 共产出 {} 个 Document 片段, 耗时 {}ms",
                    fileName, documents.size(), System.currentTimeMillis() - startTime);
            return documents;
        } catch (Exception e) {
            log.error("按目录模式解析 PDF 文件 [{}] 时发生异常: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("PDF 目录结构解析异常: " + fileName, e);
        }
    }
}
