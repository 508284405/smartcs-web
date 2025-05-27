package com.leyue.smartcs.bot.serviceimpl;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.BotProfileService;
import com.leyue.smartcs.bot.executor.BotProfileCreateCmdExe;
import com.leyue.smartcs.bot.executor.BotProfileDeleteCmdExe;
import com.leyue.smartcs.bot.executor.BotProfileGetQryExe;
import com.leyue.smartcs.bot.executor.BotProfilePageQryExe;
import com.leyue.smartcs.bot.executor.BotProfileUpdateCmdExe;
import com.leyue.smartcs.bot.executor.BotProfileEnableCmdExe;
import com.leyue.smartcs.dto.bot.BotProfileCreateCmd;
import com.leyue.smartcs.dto.bot.BotProfileDTO;
import com.leyue.smartcs.dto.bot.BotProfileDeleteCmd;
import com.leyue.smartcs.dto.bot.BotProfileGetQry;
import com.leyue.smartcs.dto.bot.BotProfilePageQry;
import com.leyue.smartcs.dto.bot.BotProfileUpdateCmd;
import com.leyue.smartcs.dto.bot.BotProfileEnableCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 机器人配置服务实现
 */
@Service
@RequiredArgsConstructor
public class BotProfileServiceImpl implements BotProfileService {
    
    private final BotProfileCreateCmdExe botProfileCreateCmdExe;
    private final BotProfileUpdateCmdExe botProfileUpdateCmdExe;
    private final BotProfileDeleteCmdExe botProfileDeleteCmdExe;
    private final BotProfileGetQryExe botProfileGetQryExe;
    private final BotProfilePageQryExe botProfilePageQryExe;
    private final BotProfileEnableCmdExe botProfileEnableCmdExe;
    
    @Override
    public SingleResponse<BotProfileDTO> createBotProfile(BotProfileCreateCmd cmd) {
        return botProfileCreateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<BotProfileDTO> updateBotProfile(BotProfileUpdateCmd cmd) {
        return botProfileUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<Boolean> deleteBotProfile(BotProfileDeleteCmd cmd) {
        return botProfileDeleteCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<BotProfileDTO> getBotProfile(BotProfileGetQry qry) {
        return botProfileGetQryExe.execute(qry);
    }
    
    @Override
    public PageResponse<BotProfileDTO> pageBotProfiles(BotProfilePageQry qry) {
        return botProfilePageQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<Boolean> enableBotProfile(BotProfileEnableCmd cmd) {
        return botProfileEnableCmdExe.execute(cmd);
    }
} 