package com.rag.simple.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档记录实体 — 关系型存储文档元数据和内容片段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_records", indexes = {
        @Index(name = "idx_file_name", columnList = "fileName"),
        @Index(name = "idx_file_type", columnList = "fileType"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
public class DocumentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 文件名 */
    @Column(nullable = false, length = 500)
    private String fileName;

    /** 文件类型 (pdf / txt / md) */
    @Column(nullable = false, length = 20)
    private String fileType;

    /** 文档内容片段 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 分段序号 */
    private Integer chunkIndex;

    /** 来源信息 (例如 PDF第几页、MD标题等) */
    @Column(length = 1000)
    private String sourceInfo;

    /** 入库时间 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
