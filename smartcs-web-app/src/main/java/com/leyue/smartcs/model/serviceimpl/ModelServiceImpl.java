package com.leyue.smartcs.model.serviceimpl;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ModelService;
import com.leyue.smartcs.dto.model.ModelCreateCmd;
import com.leyue.smartcs.dto.model.ModelDTO;
import com.leyue.smartcs.dto.model.ModelDeleteCmd;
import com.leyue.smartcs.dto.model.ModelEnableCmd;
import com.leyue.smartcs.dto.model.ModelPageQry;
import com.leyue.smartcs.dto.model.ModelUpdateCmd;
import com.leyue.smartcs.model.executor.ModelCreateCmdExe;
import com.leyue.smartcs.model.executor.ModelDeleteCmdExe;
import com.leyue.smartcs.model.executor.ModelEnableCmdExe;
import com.leyue.smartcs.model.executor.ModelGetQryExe;
import com.leyue.smartcs.model.executor.ModelPageQryExe;
import com.leyue.smartcs.model.executor.ModelUpdateCmdExe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 模型实例服务实现
 */
@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {
    
    private final ModelCreateCmdExe modelCreateCmdExe;
    private final ModelUpdateCmdExe modelUpdateCmdExe;
    private final ModelDeleteCmdExe modelDeleteCmdExe;
    private final ModelGetQryExe modelGetQryExe;
    private final ModelPageQryExe modelPageQryExe;
    private final ModelEnableCmdExe modelEnableCmdExe;
    
    @Override
    public SingleResponse<ModelDTO> createModel(ModelCreateCmd cmd) {
        return modelCreateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<ModelDTO> updateModel(ModelUpdateCmd cmd) {
        return modelUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<Boolean> deleteModel(ModelDeleteCmd cmd) {
        return modelDeleteCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<ModelDTO> getModel(Long id) {
        return modelGetQryExe.execute(id);
    }
    
    @Override
    public PageResponse<ModelDTO> pageModels(ModelPageQry qry) {
        return modelPageQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<Boolean> enableModel(ModelEnableCmd cmd) {
        return modelEnableCmdExe.execute(cmd);
    }
}