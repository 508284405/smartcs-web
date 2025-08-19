package com.leyue.smartcs.domain.intent.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshot;
import com.leyue.smartcs.domain.intent.enums.SnapshotStatus;

import java.util.List;

/**
 * 意图快照Gateway接口
 * 
 * @author Claude
 */
public interface IntentSnapshotGateway {
    
    /**
     * 保存意图快照
     * @param snapshot 意图快照对象
     * @return 保存后的意图快照对象
     */
    IntentSnapshot save(IntentSnapshot snapshot);
    
    /**
     * 更新意图快照
     * @param snapshot 意图快照对象
     */
    void update(IntentSnapshot snapshot);
    
    /**
     * 根据ID查找意图快照
     * @param id 快照ID
     * @return 意图快照对象
     */
    IntentSnapshot findById(Long id);
    
    /**
     * 根据编码查找意图快照
     * @param code 快照编码
     * @return 意图快照对象
     */
    IntentSnapshot findByCode(String code);
    
    /**
     * 获取当前激活的快照
     * @return 当前激活快照
     */
    IntentSnapshot getCurrentActiveSnapshot();
    
    /**
     * 根据状态查找快照列表
     * @param status 快照状态
     * @return 快照列表
     */
    List<IntentSnapshot> findByStatus(SnapshotStatus status);
    
    /**
     * 分页查询意图快照
     * @param status 快照状态
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<IntentSnapshot> findByPage(SnapshotStatus status, String keyword, int pageNum, int pageSize);
    
    /**
     * 删除意图快照
     * @param id 快照ID
     */
    void deleteById(Long id);
}