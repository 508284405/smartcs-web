package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.model.Faq;

import java.util.List;
import java.util.Optional;

/**
 * FAQ存储网关接口
 */
public interface FaqGateway {
    /**
     * 保存FAQ
     * @param faq FAQ实体
     * @return 保存后的FAQ
     */
    Faq save(Faq faq);
    
    /**
     * 根据ID查询FAQ
     * @param id FAQ ID
     * @return FAQ实体(可能为空)
     */
    Optional<Faq> findById(Long id);
    
    /**
     * 根据ID删除FAQ
     * @param id FAQ ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
    
    /**
     * 根据问题文本查询FAQ
     * @param question 问题文本
     * @return FAQ列表
     */
    List<Faq> findByQuestionLike(String question);
    
    /**
     * 分页查询FAQ
     * @param keyword 关键词
     * @param pageNum 页码(从1开始)
     * @param pageSize 每页大小
     * @return FAQ列表
     */
    List<Faq> listByPage(String keyword, int pageNum, int pageSize);
    
    /**
     * 获取总记录数
     * @param keyword 关键词
     * @return 总记录数
     */
    long count(String keyword);
    
    /**
     * 增加命中次数
     * @param id FAQ ID
     * @return 新的命中次数
     */
    long incrementHitCount(Long id);
} 