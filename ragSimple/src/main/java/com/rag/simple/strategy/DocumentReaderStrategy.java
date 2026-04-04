package com.rag.simple.strategy;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 文档读取策略接口
 */
public interface DocumentReaderStrategy {

    /**
     * 判断当前策略是否支持该类型和子类型的处理
     *
     * @param fileType 文件类型（如 txt, md, pdf）
     * @param subType  子类型或读取模式（如 pdf 中的 page 或 outline），可以为空
     * @return 是否支持
     */
    boolean supports(String fileType, String subType);

    /**
     * 读取并解析资源内容为 Document 列表
     *
     * @param resource Spring资源对象
     * @return 解析后的段落集合
     */
    List<Document> read(Resource resource);
}
