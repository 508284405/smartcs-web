package com.leyue.smartcs.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI应用数据访问接口
 */
@Mapper
public interface AiAppMapper extends BaseMapper<AiAppDO> {
    
    /**
     * 分页查询AI应用列表
     * @param page 分页参数
     * @param creatorId 创建者ID
     * @param type 应用类型
     * @param status 应用状态
     * @param keyword 关键词
     * @return 分页结果
     */
    IPage<AiAppDO> selectAppPage(Page<AiAppDO> page, 
                                @Param("creatorId") Long creatorId,
                                @Param("type") String type,
                                @Param("status") String status,
                                @Param("keyword") String keyword);
    
    /**
     * 统计AI应用数量
     * @param creatorId 创建者ID
     * @param type 应用类型
     * @param status 应用状态
     * @param keyword 关键词
     * @return 数量
     */
    long countApps(@Param("creatorId") Long creatorId,
                   @Param("type") String type,
                   @Param("status") String status,
                   @Param("keyword") String keyword);
    
    /**
     * 根据编码查询应用（排除指定ID）
     * @param code 应用编码
     * @param excludeId 排除的ID
     * @return 应用数据
     */
    AiAppDO selectByCodeExcludeId(@Param("code") String code, @Param("excludeId") Long excludeId);
}