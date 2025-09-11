package com.leyue.smartcs.web.ltm;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.client.ltm.dto.personalization.*;
import com.leyue.smartcs.app.ltm.executor.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * LTM个性化控制器
 * 提供个性化设置和偏好管理功能
 */
@RestController
@RequestMapping("/api/v1/ltm/personalization")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "LTM个性化", description = "长期记忆系统的个性化设置和偏好管理")
public class LTMPersonalizationController {

    private final PersonalizationQueryExe personalizationQueryExe;
    private final PersonalizationCmdExe personalizationCmdExe;
    private final UserPreferenceCmdExe userPreferenceCmdExe;

    @Operation(summary = "获取用户个性化配置", description = "获取用户的LTM个性化配置信息")
    @GetMapping("/config")
    public SingleResponse<PersonalizationConfigDTO> getPersonalizationConfig(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId) {
        
        log.debug("获取用户个性化配置: userId={}", userId);
        
        PersonalizationConfigQry qry = new PersonalizationConfigQry();
        qry.setUserId(userId);
        
        return personalizationQueryExe.getPersonalizationConfig(qry);
    }

    @Operation(summary = "更新个性化配置", description = "更新用户的LTM系统配置")
    @PutMapping("/config")
    public Response updatePersonalizationConfig(
            @Valid @RequestBody UpdatePersonalizationConfigCmd cmd) {
        
        log.info("更新个性化配置: userId={}, ltmEnabled={}, personalizationLevel={}", 
                 cmd.getUserId(), cmd.getLtmEnabled(), cmd.getPersonalizationLevel());
        
        return personalizationCmdExe.updatePersonalizationConfig(cmd);
    }

    @Operation(summary = "获取用户偏好档案", description = "获取AI学习到的用户偏好和习惯档案")
    @GetMapping("/profile")
    public SingleResponse<UserPreferenceProfileDTO> getUserPreferenceProfile(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId) {
        
        log.debug("获取用户偏好档案: userId={}", userId);
        
        UserPreferenceProfileQry qry = new UserPreferenceProfileQry();
        qry.setUserId(userId);
        
        return personalizationQueryExe.getUserPreferenceProfile(qry);
    }

    @Operation(summary = "获取个性化建议", description = "基于用户记忆获取个性化的使用建议")
    @GetMapping("/suggestions")
    public SingleResponse<PersonalizationSuggestionsDTO> getPersonalizationSuggestions(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "建议类型") 
            @RequestParam(required = false) String suggestionType) {
        
        log.debug("获取个性化建议: userId={}, type={}", userId, suggestionType);
        
        PersonalizationSuggestionsQry qry = new PersonalizationSuggestionsQry();
        qry.setUserId(userId);
        qry.setSuggestionType(suggestionType);
        
        return personalizationQueryExe.getPersonalizationSuggestions(qry);
    }

    @Operation(summary = "手动添加用户偏好", description = "用户手动设置个人偏好和习惯")
    @PostMapping("/preference")
    public Response addUserPreference(@Valid @RequestBody AddUserPreferenceCmd cmd) {
        
        log.info("添加用户偏好: userId={}, category={}, preference={}", 
                 cmd.getUserId(), cmd.getCategory(), cmd.getPreferenceName());
        
        return userPreferenceCmdExe.addUserPreference(cmd);
    }

    @Operation(summary = "更新用户偏好", description = "更新已有的用户偏好设置")
    @PutMapping("/preference/{preferenceId}")
    public Response updateUserPreference(
            @Parameter(description = "偏好ID") 
            @PathVariable @NotNull @Positive Long preferenceId,
            @Valid @RequestBody UpdateUserPreferenceCmd cmd) {
        
        log.info("更新用户偏好: preferenceId={}, userId={}", preferenceId, cmd.getUserId());
        
        cmd.setPreferenceId(preferenceId);
        return userPreferenceCmdExe.updateUserPreference(cmd);
    }

    @Operation(summary = "删除用户偏好", description = "删除指定的用户偏好设置")
    @DeleteMapping("/preference/{preferenceId}")
    public Response deleteUserPreference(
            @Parameter(description = "偏好ID") 
            @PathVariable @NotNull @Positive Long preferenceId,
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId) {
        
        log.info("删除用户偏好: preferenceId={}, userId={}", preferenceId, userId);
        
        DeleteUserPreferenceCmd cmd = new DeleteUserPreferenceCmd();
        cmd.setPreferenceId(preferenceId);
        cmd.setUserId(userId);
        
        return userPreferenceCmdExe.deleteUserPreference(cmd);
    }

    @Operation(summary = "重置个性化设置", description = "重置用户的所有个性化设置到默认状态")
    @PostMapping("/reset")
    public Response resetPersonalization(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "是否保留核心偏好") 
            @RequestParam(defaultValue = "true") boolean keepCorePreferences) {
        
        log.info("重置个性化设置: userId={}, keepCore={}", userId, keepCorePreferences);
        
        ResetPersonalizationCmd cmd = new ResetPersonalizationCmd();
        cmd.setUserId(userId);
        cmd.setKeepCorePreferences(keepCorePreferences);
        
        return personalizationCmdExe.resetPersonalization(cmd);
    }

    @Operation(summary = "获取个性化效果评估", description = "获取个性化功能对用户体验的改善效果评估")
    @GetMapping("/evaluation")
    public SingleResponse<PersonalizationEvaluationDTO> getPersonalizationEvaluation(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "评估时间范围（天）") 
            @RequestParam(defaultValue = "30") int days) {
        
        log.debug("获取个性化效果评估: userId={}, days={}", userId, days);
        
        PersonalizationEvaluationQry qry = new PersonalizationEvaluationQry();
        qry.setUserId(userId);
        qry.setDays(days);
        
        return personalizationQueryExe.getPersonalizationEvaluation(qry);
    }

    @Operation(summary = "训练个性化模型", description = "基于用户反馈手动触发个性化模型训练")
    @PostMapping("/train")
    public Response trainPersonalizationModel(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId,
            @Parameter(description = "训练数据范围（天）") 
            @RequestParam(defaultValue = "90") int trainingDays) {
        
        log.info("训练个性化模型: userId={}, trainingDays={}", userId, trainingDays);
        
        TrainPersonalizationModelCmd cmd = new TrainPersonalizationModelCmd();
        cmd.setUserId(userId);
        cmd.setTrainingDays(trainingDays);
        
        return personalizationCmdExe.trainPersonalizationModel(cmd);
    }

    @Operation(summary = "导入偏好设置", description = "从外部数据导入用户偏好设置")
    @PostMapping("/import")
    public Response importPersonalizationData(
            @Valid @RequestBody ImportPersonalizationDataCmd cmd) {
        
        log.info("导入个性化数据: userId={}, dataSize={}", 
                 cmd.getUserId(), cmd.getPreferenceData().size());
        
        return personalizationCmdExe.importPersonalizationData(cmd);
    }

    @Operation(summary = "导出偏好设置", description = "导出用户的个性化设置和偏好数据")
    @PostMapping("/export")
    public SingleResponse<PersonalizationExportDTO> exportPersonalizationData(
            @Valid @RequestBody ExportPersonalizationDataCmd cmd) {
        
        log.info("导出个性化数据: userId={}, includeHistory={}", 
                 cmd.getUserId(), cmd.getIncludeHistory());
        
        return personalizationCmdExe.exportPersonalizationData(cmd);
    }

    @Operation(summary = "获取学习进度", description = "查看AI对用户的学习和了解程度")
    @GetMapping("/learning-progress")
    public SingleResponse<LearningProgressDTO> getLearningProgress(
            @Parameter(description = "用户ID") 
            @RequestParam @NotNull @Positive Long userId) {
        
        log.debug("获取学习进度: userId={}", userId);
        
        LearningProgressQry qry = new LearningProgressQry();
        qry.setUserId(userId);
        
        return personalizationQueryExe.getLearningProgress(qry);
    }

    @Operation(summary = "反馈个性化效果", description = "用户反馈个性化服务的效果")
    @PostMapping("/feedback")
    public Response submitPersonalizationFeedback(
            @Valid @RequestBody PersonalizationFeedbackCmd cmd) {
        
        log.info("提交个性化反馈: userId={}, rating={}, category={}", 
                 cmd.getUserId(), cmd.getRating(), cmd.getFeedbackCategory());
        
        return personalizationCmdExe.submitPersonalizationFeedback(cmd);
    }
}