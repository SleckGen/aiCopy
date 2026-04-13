package com.rag.simple.repository;

import com.rag.simple.entity.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档记录 JPA Repository — 关系型数据库检索
 */
@Repository
public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, Long> {

    /** 按文件名查询 */
    List<DocumentRecord> findByFileName(String fileName);

    /** 按文件类型查询 */
    List<DocumentRecord> findByFileType(String fileType);

    /** 按内容关键字模糊搜索 */
    @Query("SELECT d FROM DocumentRecord d WHERE d.content LIKE %:keyword%")
    List<DocumentRecord> searchByKeyword(@Param("keyword") String keyword);

    /** 按文件名模糊搜索 */
    List<DocumentRecord> findByFileNameContaining(String fileName);

    /** 按时间范围查询 */
    List<DocumentRecord> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /** 按文件名和文件类型查询 */
    List<DocumentRecord> findByFileNameAndFileType(String fileName, String fileType);

    /** 统计指定文件的分块数量 */
    long countByFileName(String fileName);
}
