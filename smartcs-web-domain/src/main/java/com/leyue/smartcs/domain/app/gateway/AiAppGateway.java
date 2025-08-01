package com.leyue.smartcs.domain.app.gateway;

import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.enums.AppStatus;
import com.leyue.smartcs.domain.app.enums.AppType;

import java.util.List;

/**
 * AI应用领域网关接口
 */
public interface AiAppGateway {
    
    /**
     * 创建AI应用
     * @param aiApp AI应用
     * @return 创建后的AI应用
     */
    AiApp create(AiApp aiApp);
    
    /**
     * 更新AI应用
     * @param aiApp AI应用
     * @return 更新后的AI应用
     */
    AiApp update(AiApp aiApp);
    
    /**
     * 根据ID获取AI应用
     * @param id 应用ID
     * @return AI应用
     */
    AiApp getById(Long id);
    
    /**
     * 根据编码获取AI应用
     * @param code 应用编码
     * @return AI应用
     */
    AiApp getByCode(String code);
    
    /**
     * 删除AI应用（逻辑删除）
     * @param id 应用ID
     * @return 是否删除成功
     */
    boolean delete(Long id);
    
    /**
     * 分页查询AI应用列表
     * @param creatorId 创建者ID（可选）
     * @param type 应用类型（可选）
     * @param status 应用状态（可选）
     * @param keyword 关键词（可选）
     * @param offset 偏移量
     * @param limit 限制条数
     * @return AI应用列表
     */
    List<AiApp> listByPage(Long creatorId, AppType type, AppStatus status, String keyword, int offset, int limit);
    
    /**
     * 统计AI应用总数
     * @param creatorId 创建者ID（可选）
     * @param type 应用类型（可选）
     * @param status 应用状态（可选）
     * @param keyword 关键词（可选）
     * @return 总数
     */
    long count(Long creatorId, AppType type, AppStatus status, String keyword);
    
    /**
     * 检查应用编码是否存在
     * @param code 应用编码
     * @param excludeId 排除的应用ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByCode(String code, Long excludeId);
    
    /**
     * 保存AI应用（包含创建和更新）
     * @param aiApp AI应用
     * @return 保存后的AI应用
     */
    AiApp save(AiApp aiApp);
}