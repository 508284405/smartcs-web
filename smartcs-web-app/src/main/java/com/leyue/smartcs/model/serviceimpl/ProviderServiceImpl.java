package com.leyue.smartcs.model.serviceimpl;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ProviderService;
import com.leyue.smartcs.dto.model.ProviderCreateCmd;
import com.leyue.smartcs.dto.model.ProviderDTO;
import com.leyue.smartcs.dto.model.ProviderDeleteCmd;
import com.leyue.smartcs.dto.model.ProviderPageQry;
import com.leyue.smartcs.dto.model.ProviderUpdateCmd;
import com.leyue.smartcs.dto.model.VisualModelProviderQry;
import com.leyue.smartcs.model.executor.ProviderCreateCmdExe;
import com.leyue.smartcs.model.executor.ProviderDeleteCmdExe;
import com.leyue.smartcs.model.executor.ProviderGetQryExe;
import com.leyue.smartcs.model.executor.ProviderPageQryExe;
import com.leyue.smartcs.model.executor.ProviderUpdateCmdExe;
import com.leyue.smartcs.model.executor.VisualProviderPageQryExe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 模型提供商服务实现
 */
@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {
    
    private final ProviderCreateCmdExe providerCreateCmdExe;
    private final ProviderUpdateCmdExe providerUpdateCmdExe;
    private final ProviderDeleteCmdExe providerDeleteCmdExe;
    private final ProviderGetQryExe providerGetQryExe;
    private final ProviderPageQryExe providerPageQryExe;
    private final VisualProviderPageQryExe visualProviderPageQryExe;
    
    @Override
    public SingleResponse<ProviderDTO> createProvider(ProviderCreateCmd cmd) {
        return providerCreateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<ProviderDTO> updateProvider(ProviderUpdateCmd cmd) {
        return providerUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<Boolean> deleteProvider(ProviderDeleteCmd cmd) {
        return providerDeleteCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<ProviderDTO> getProvider(Long id) {
        return providerGetQryExe.execute(id);
    }
    
    @Override
    public PageResponse<ProviderDTO> pageProviders(ProviderPageQry qry) {
        return providerPageQryExe.execute(qry);
    }
    
    @Override
    public PageResponse<ProviderDTO> pageVisualProviders(VisualModelProviderQry qry) {
        return visualProviderPageQryExe.execute(qry);
    }
}