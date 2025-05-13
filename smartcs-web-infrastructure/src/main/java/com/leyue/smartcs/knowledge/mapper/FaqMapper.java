package com.leyue.smartcs.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.knowledge.dataobject.FaqDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * FAQ Mapper接口
 */
@Mapper
public interface FaqMapper extends BaseMapper<FaqDO> {
    
    /**
     * 根据问题查询FAQ（模糊匹配）
     * @param question 问题关键词
     * @return FAQ列表
     */
    List<FaqDO> findByQuestionLike(@Param("question") String question);
    
    /**
     * 分页查询FAQ
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 每页大小
     * @return FAQ列表
     */
    List<FaqDO> listByPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 获取总记录数
     * @param keyword 关键词
     * @return 总记录数
     */
    long count(@Param("keyword") String keyword);
    
    /**
     * 增加命中次数
     * @param id FAQ ID
     * @return 受影响行数
     */
    int incrementHitCount(@Param("id") Long id);
} 