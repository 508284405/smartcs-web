package com.leyue.smartcs.app.service;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.AiAppService;
import com.leyue.smartcs.app.executor.command.AiAppCreateCmdExe;
import com.leyue.smartcs.app.executor.command.AiAppDeleteCmdExe;
import com.leyue.smartcs.app.executor.command.AiAppStatusUpdateCmdExe;
import com.leyue.smartcs.app.executor.command.AiAppUpdateCmdExe;
import com.leyue.smartcs.app.executor.query.AiAppGetQryExe;
import com.leyue.smartcs.app.executor.query.AiAppListQryExe;
import com.leyue.smartcs.dto.app.AiAppDTO;
import com.leyue.smartcs.dto.app.AiAppCreateCmd;
import com.leyue.smartcs.dto.app.AiAppUpdateCmd;
import com.leyue.smartcs.dto.app.AiAppStatusUpdateCmd;
import com.leyue.smartcs.dto.app.AiAppListQry;
import com.leyue.smartcs.dto.app.AiAppPromptOptimizeCmd;
import com.leyue.smartcs.dto.app.AiAppPromptOptimizeResponse;
import com.leyue.smartcs.dto.app.AiAppFunctionConfigCmd;
import com.leyue.smartcs.dto.app.AiAppFunctionConfigResponse;
import com.leyue.smartcs.app.executor.AiAppPromptOptimizeCmdExe;
import com.leyue.smartcs.app.executor.AiAppFunctionConfigCmdExe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI应用管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAppServiceImpl implements AiAppService {
    
    private final AiAppCreateCmdExe aiAppCreateCmdExe;
    private final AiAppUpdateCmdExe aiAppUpdateCmdExe;
    private final AiAppStatusUpdateCmdExe aiAppStatusUpdateCmdExe;
    private final AiAppDeleteCmdExe aiAppDeleteCmdExe;
    private final AiAppGetQryExe aiAppGetQryExe;
    private final AiAppListQryExe aiAppListQryExe;
    private final AiAppPromptOptimizeCmdExe aiAppPromptOptimizeCmdExe;
    private final AiAppFunctionConfigCmdExe aiAppFunctionConfigCmdExe;
    
    @Override
    public SingleResponse<AiAppDTO> createApp(AiAppCreateCmd cmd) {
        return aiAppCreateCmdExe.execute(cmd);
    }
    
    @Override
    public Response updateApp(AiAppUpdateCmd cmd) {
        return aiAppUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public Response updateAppStatus(AiAppStatusUpdateCmd cmd) {
        return aiAppStatusUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<AiAppDTO> getApp(Long id) {
        return aiAppGetQryExe.execute(id);
    }
    
    @Override
    public Response deleteApp(Long id) {
        return aiAppDeleteCmdExe.execute(id);
    }
    
    @Override
    public PageResponse<AiAppDTO> listApps(AiAppListQry qry) {
        return aiAppListQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<AiAppPromptOptimizeResponse> optimizePrompt(AiAppPromptOptimizeCmd cmd) {
        return aiAppPromptOptimizeCmdExe.execute(cmd);
    }
    
    @Override
    public Response updateFunctionConfig(AiAppFunctionConfigCmd cmd) {
        return aiAppFunctionConfigCmdExe.updateFunctionConfig(cmd);
    }
    
    @Override
    public SingleResponse<AiAppFunctionConfigResponse> getFunctionConfig(Long appId) {
        return aiAppFunctionConfigCmdExe.getFunctionConfig(appId);
    }
}