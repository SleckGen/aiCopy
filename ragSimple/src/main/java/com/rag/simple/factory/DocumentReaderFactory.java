package com.rag.simple.factory;

import com.rag.simple.strategy.DocumentReaderStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 文档读取策略路由工厂
 */
@Component
public class DocumentReaderFactory {

    private final List<DocumentReaderStrategy> strategies;

    public DocumentReaderFactory(List<DocumentReaderStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 根据文件名及附加模式获取对应的处理策略
     *
     * @param fileName 文件名 (如: report.pdf)
     * @param subType  特定类型的处理模式 (如: page / outline)，可为空
     * @return 匹配的策略
     */
    public DocumentReaderStrategy getStrategy(String fileName, String subType) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("文件名无效或缺少后缀: " + fileName);
        }
        
        // 自动提取文件后缀作为 fileType
        String fileExt = fileName.substring(fileName.lastIndexOf('.') + 1);

        Optional<DocumentReaderStrategy> strategy = strategies.stream()
                .filter(s -> s.supports(fileExt, subType))
                .findFirst();

        return strategy.orElseThrow(() -> new IllegalArgumentException("暂不支持该文件后缀或解析模式: " + fileExt + " [" + subType + "]"));
    }
}
