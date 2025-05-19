package com.leyue.smartcs.dto.knowledge;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 索引信息DTO
 */
@Data
public class IndexInfoDTO {
    /** 索引名称 */
    private String name;

    /** 索引选项 */
    private Map<String, Object> options;

    /** 索引定义 */
    private Map<String, Object> definition;

    /** 索引属性列表 (字段信息和统计信息等) */
    private List<Map<String, Object>> attributes;

    /** GC统计信息 */
    private Map<String, Object> gcStats;

    /** Cursor统计信息 */
    private Map<String, Object> cursorStats;

    /** 方言统计信息 */
    private Map<String, Object> dialectStats;

    /** 文档数量 */
    private Double docs;

    /** 最大文档ID */
    private Double maxDocId;

    /** 术语数量 */
    private Double terms;

    /** 记录数量 */
    private Double records;

    /** 倒排索引大小 (MB) */
    private Double invertedSize;

    /** 向量索引大小 (MB) */
    private Double vectorIndexSize;

    /** 总倒排索引块数量 */
    private Double totalInvertedIndexBlocks;

    /** Offset向量大小 (MB) */
    private Double offsetVectorsSize;

    /** 文档表大小 (MB) */
    private Double docTableSize;

    /** 可排序值大小 (MB) */
    private Double sortableValuesSize;

    /** 键表大小 (MB) */
    private Double keyTableSize;

    /** 每个文档的平均记录数 */
    private Double recordsPerDocAverage;

    /** 每个记录的平均字节数 */
    private Double bytesPerRecordAverage;

    /** 每个术语的平均Offset数 */
    private Double offsetsPerTermAverage;
    /** 每个记录的平均Offset位数 */
    private Double offsetBitsPerRecordAverage;

    /** Hash索引失败次数 */
    private Long hashIndexingFailures;

    /** 总索引时间 (秒) */
    private Double totalIndexingTime;

    /** 是否正在索引 (1: 是, 0: 否) */
    private Long indexing;

    /** 索引进度百分比 */
    private Double percentIndexed;

    /** 使用次数 */
    private Long numberOfUses;
} 