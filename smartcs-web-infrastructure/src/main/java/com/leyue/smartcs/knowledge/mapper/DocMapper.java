package com.leyue.smartcs.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.knowledge.dataobject.DocDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档 Mapper接口
 */
@Mapper
public interface DocMapper extends BaseMapper<DocDO> {
    
    /**
     * 分页查询文档
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 每页大小
     * @return 文档列表
     */
    List<DocDO> listByPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 获取总记录数
     * @param keyword 关键词
     * @return 总记录数
     */
    long count(@Param("keyword") String keyword);
} 