package com.leyue.smartcs.web.bot;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.BotProfileService;
import com.leyue.smartcs.dto.bot.BotProfileCreateCmd;
import com.leyue.smartcs.dto.bot.BotProfileDTO;
import com.leyue.smartcs.dto.bot.BotProfileDeleteCmd;
import com.leyue.smartcs.dto.bot.BotProfileGetQry;
import com.leyue.smartcs.dto.bot.BotProfilePageQry;
import com.leyue.smartcs.dto.bot.BotProfileUpdateCmd;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端机器人配置控制器
 */
@RestController
@RequestMapping("/api/admin/bot/profile")
@RequiredArgsConstructor
@Slf4j
public class AdminBotProfileController {
    
    private final BotProfileService botProfileService;
    
    /**
     * 创建机器人配置
     */
    @PostMapping
    public SingleResponse<BotProfileDTO> createBotProfile(@RequestBody @Valid BotProfileCreateCmd cmd) {
        return botProfileService.createBotProfile(cmd);
    }
    
    /**
     * 分页查询机器人配置列表
     */
    @GetMapping("/page")
    public PageResponse<BotProfileDTO> pageBotProfiles(BotProfilePageQry qry) {
        return botProfileService.pageBotProfiles(qry);
    }
    
    /**
     * 获取机器人配置详情
     */
    @GetMapping("/{botId}")
    public SingleResponse<BotProfileDTO> getBotProfile(@PathVariable("botId") Long botId) {
        BotProfileGetQry qry = new BotProfileGetQry();
        qry.setBotId(botId);
        return botProfileService.getBotProfile(qry);
    }
    
    /**
     * 更新机器人配置
     */
    @PutMapping
    public SingleResponse<BotProfileDTO> updateBotProfile(@RequestBody BotProfileUpdateCmd cmd) {
        return botProfileService.updateBotProfile(cmd);
    }
    
    /**
     * 删除机器人配置
     */
    @DeleteMapping("/{botId}")
    public SingleResponse<Boolean> deleteBotProfile(@PathVariable("botId") Long botId) {
        BotProfileDeleteCmd cmd = new BotProfileDeleteCmd();
        cmd.setBotId(botId);
        return botProfileService.deleteBotProfile(cmd);
    }
} 