package com.leyue.smartcs.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.knowledge.dataobject.FaqDO;
import com.leyue.smartcs.dto.knowledge.FaqScoreVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
    int incrementHitCount(@Param("id") Long id,  @Param("now") long now);
    
    /**
     * 根据问题文本进行全文检索
     * @param keyword 关键词
     * @param k 返回数量
     * @return FAQ ID与相关性分数的映射
     */
    List<FaqScoreVO> searchByQuestionFullText(@Param("keyword") String keyword, @Param("k") int k);
} 