package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.app.AiAppDTO;
import com.leyue.smartcs.dto.app.AiAppCreateCmd;
import com.leyue.smartcs.dto.app.AiAppUpdateCmd;
import com.leyue.smartcs.dto.app.AiAppStatusUpdateCmd;
import com.leyue.smartcs.dto.app.AiAppListQry;

/**
 * AI应用管理服务接口
 */
public interface AiAppService {
    
    /**
     * 创建AI应用
     * @param cmd 创建命令
     * @return 创建的应用
     */
    SingleResponse<AiAppDTO> createApp(AiAppCreateCmd cmd);
    
    /**
     * 更新AI应用
     * @param cmd 更新命令
     * @return 更新结果
     */
    Response updateApp(AiAppUpdateCmd cmd);
    
    /**
     * 更新AI应用状态
     * @param cmd 状态更新命令
     * @return 更新结果
     */
    Response updateAppStatus(AiAppStatusUpdateCmd cmd);
    
    /**
     * 根据ID查询AI应用
     * @param id 应用ID
     * @return 应用信息
     */
    SingleResponse<AiAppDTO> getApp(Long id);
    
    /**
     * 删除AI应用
     * @param id 应用ID
     * @return 删除结果
     */
    Response deleteApp(Long id);
    
    /**
     * 分页查询AI应用列表
     * @param qry 查询条件
     * @return 应用列表
     */
    PageResponse<AiAppDTO> listApps(AiAppListQry qry);
}