package com.leyue.smartcs.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.moderation.dataobject.ModerationRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 内容审核记录Mapper
 */
@Mapper
public interface ModerationRecordMapper extends BaseMapper<ModerationRecordDO> {

    /**
     * 根据内容哈希查找审核记录
     */
    @Select("SELECT * FROM t_moderation_record WHERE content_hash = #{contentHash} ORDER BY created_at DESC LIMIT 1")
    ModerationRecordDO findByContentHash(@Param("contentHash") String contentHash);

    /**
     * 根据源ID和类型查找审核记录
     */
    @Select("SELECT * FROM t_moderation_record WHERE source_id = #{sourceId} AND source_type = #{sourceType} ORDER BY created_at DESC")
    List<ModerationRecordDO> findBySourceIdAndType(@Param("sourceId") String sourceId, @Param("sourceType") String sourceType);

    /**
     * 根据用户ID查找审核记录
     */
    @Select("SELECT * FROM t_moderation_record WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<ModerationRecordDO> findByUserId(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 根据会话ID查找审核记录
     */
    @Select("SELECT * FROM t_moderation_record WHERE session_id = #{sessionId} ORDER BY created_at DESC")
    List<ModerationRecordDO> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 查找需要人工审核的记录
     */
    @Select("SELECT * FROM t_moderation_record WHERE moderation_result = 'NEEDS_REVIEW' AND manual_review_status IN ('PENDING', 'NOT_REQUIRED') ORDER BY created_at ASC LIMIT #{limit}")
    List<ModerationRecordDO> findRecordsNeedingManualReview(@Param("limit") int limit);

    /**
     * 查找被阻断的记录
     */
    @Select("SELECT * FROM t_moderation_record WHERE is_blocked = 1 ORDER BY created_at DESC LIMIT #{limit}")
    List<ModerationRecordDO> findBlockedRecords(@Param("limit") int limit);

    /**
     * 统计各风险等级的记录数量
     */
    @Select({
        "SELECT ",
        "SUM(CASE WHEN risk_level = 'LOW' THEN 1 ELSE 0 END) as lowCount,",
        "SUM(CASE WHEN risk_level = 'MEDIUM' THEN 1 ELSE 0 END) as mediumCount,",
        "SUM(CASE WHEN risk_level = 'HIGH' THEN 1 ELSE 0 END) as highCount,",
        "SUM(CASE WHEN risk_level = 'CRITICAL' THEN 1 ELSE 0 END) as criticalCount ",
        "FROM t_moderation_record"
    })
    Map<String, Object> countByRiskLevel();

    /**
     * 获取审核统计信息
     */
    @Select({
        "SELECT ",
        "COUNT(*) as totalRecords,",
        "SUM(CASE WHEN moderation_result = 'APPROVED' THEN 1 ELSE 0 END) as approvedCount,",
        "SUM(CASE WHEN moderation_result = 'REJECTED' THEN 1 ELSE 0 END) as rejectedCount,",
        "SUM(CASE WHEN moderation_result = 'NEEDS_REVIEW' THEN 1 ELSE 0 END) as pendingCount,",
        "SUM(CASE WHEN is_blocked = 1 THEN 1 ELSE 0 END) as blockedCount,",
        "AVG(processing_time_ms) as avgProcessingTime,",
        "SUM(CASE WHEN created_at >= #{todayStart} THEN 1 ELSE 0 END) as todayRecords ",
        "FROM t_moderation_record"
    })
    Map<String, Object> getModerationStatistics(@Param("todayStart") Long todayStart);

    /**
     * 获取违规趋势统计（按日期分组）
     */
    @Select({
        "<script>",
        "SELECT ",
        "DATE(FROM_UNIXTIME(created_at/1000)) as date,",
        "created_at - (created_at % 86400000) as timestamp,",
        "COUNT(*) as violationCount ",
        "FROM t_moderation_record ",
        "WHERE moderation_result = 'REJECTED' ",
        "AND created_at BETWEEN #{startTime} AND #{endTime} ",
        "GROUP BY DATE(FROM_UNIXTIME(created_at/1000)) ",
        "ORDER BY date ASC",
        "</script>"
    })
    List<Map<String, Object>> getViolationTrends(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    /**
     * 复杂条件分页查询
     */
    IPage<ModerationRecordDO> findRecordsByPage(Page<ModerationRecordDO> page,
                                              @Param("contentType") String contentType,
                                              @Param("sourceType") String sourceType,
                                              @Param("moderationResult") String moderationResult,
                                              @Param("riskLevel") String riskLevel,
                                              @Param("userId") String userId,
                                              @Param("sessionId") String sessionId,
                                              @Param("isBlocked") Integer isBlocked,
                                              @Param("startTime") Long startTime,
                                              @Param("endTime") Long endTime);

    /**
     * 获取最新的审核记录
     */
    @Select("SELECT * FROM t_moderation_record ORDER BY created_at DESC LIMIT #{limit}")
    List<ModerationRecordDO> findLatestRecords(@Param("limit") int limit);

    /**
     * 根据时间范围统计记录数量
     */
    @Select("SELECT COUNT(*) FROM t_moderation_record WHERE created_at BETWEEN #{startTime} AND #{endTime}")
    long countRecordsByTimeRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    /**
     * 清理过期记录（根据配置的保留时间）
     */
    int deleteExpiredRecords(@Param("expireTime") Long expireTime);
}