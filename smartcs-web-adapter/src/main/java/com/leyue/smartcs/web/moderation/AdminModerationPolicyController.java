package com.leyue.smartcs.web.moderation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.app.executor.ModerationPolicyCmdExe;
import com.leyue.smartcs.app.executor.ModerationPolicyQryExe;
import com.leyue.smartcs.dto.moderation.ModerationDimensionDTO;
import com.leyue.smartcs.dto.moderation.ModerationPolicyCreateCmd;
import com.leyue.smartcs.dto.moderation.ModerationPolicyDTO;
import com.leyue.smartcs.dto.moderation.ModerationPolicyPageQry;
import com.leyue.smartcs.dto.moderation.ModerationPolicyTemplateDTO;
import com.leyue.smartcs.dto.moderation.ModerationPolicyUpdateCmd;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理端审核策略控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/moderation/policies")
@RequiredArgsConstructor
public class AdminModerationPolicyController {

    private final ModerationPolicyCmdExe policyCommandExecutor;
    private final ModerationPolicyQryExe policyQueryExecutor;

    @PostMapping
    public SingleResponse<Long> createPolicy(@RequestBody ModerationPolicyCreateCmd cmd, HttpServletRequest request) {
        log.info("Admin creating moderation policy: {}", cmd.getCode());
        
        // 设置创建者（从会话中获取）
        cmd.setCreatedBy(getUserFromRequest(request));
        
        return policyCommandExecutor.createPolicy(cmd);
    }

    @PutMapping("/{id}")
    public Response updatePolicy(@PathVariable Long id, @RequestBody ModerationPolicyUpdateCmd cmd, HttpServletRequest request) {
        log.info("Admin updating moderation policy: id={}", id);
        
        cmd.setId(id);
        cmd.setUpdatedBy(getUserFromRequest(request));
        
        return policyCommandExecutor.updatePolicy(cmd);
    }

    @PostMapping("/{id}/enable")
    public Response enablePolicy(@PathVariable Long id, HttpServletRequest request) {
        log.info("Admin enabling moderation policy: id={}", id);
        
        return policyCommandExecutor.enablePolicy(id, getUserFromRequest(request));
    }

    @PostMapping("/{id}/disable")
    public Response disablePolicy(@PathVariable Long id, HttpServletRequest request) {
        log.info("Admin disabling moderation policy: id={}", id);
        
        return policyCommandExecutor.disablePolicy(id, getUserFromRequest(request));
    }

    @DeleteMapping("/{id}")
    public Response deletePolicy(@PathVariable Long id) {
        log.info("Admin deleting moderation policy: id={}", id);
        
        return policyCommandExecutor.deletePolicy(id);
    }

    @GetMapping("/{id}")
    public SingleResponse<ModerationPolicyDTO> getPolicyById(@PathVariable Long id) {
        log.debug("Admin querying moderation policy: id={}", id);
        
        return policyQueryExecutor.getPolicyById(id);
    }

    @GetMapping("/code/{code}")
    public SingleResponse<ModerationPolicyDTO> getPolicyByCode(@PathVariable String code) {
        log.debug("Admin querying moderation policy by code: {}", code);
        
        return policyQueryExecutor.getPolicyByCode(code);
    }

    @GetMapping
    public PageResponse<ModerationPolicyDTO> queryPolicies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String scenario,
            @RequestParam(required = false) String policyType,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long templateId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "priority") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {
        
        log.debug("Admin querying moderation policies with filters");
        
        ModerationPolicyPageQry qry = ModerationPolicyPageQry.builder()
                .name(name)
                .code(code)
                .scenario(scenario)
                .policyType(policyType)
                .isActive(isActive)
                .templateId(templateId)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .build();
        
        return policyQueryExecutor.queryPolicies(qry);
    }

    @GetMapping("/scenario/{scenario}")
    public SingleResponse<List<ModerationPolicyDTO>> getPoliciesByScenario(@PathVariable String scenario) {
        log.debug("Admin querying policies by scenario: {}", scenario);
        
        return policyQueryExecutor.getPoliciesByScenario(scenario);
    }

    @GetMapping("/{id}/dimensions")
    public SingleResponse<List<ModerationDimensionDTO>> getPolicyDimensions(@PathVariable Long id) {
        log.debug("Admin querying policy dimensions: policyId={}", id);
        
        return policyQueryExecutor.getPolicyDimensions(id);
    }

    @PostMapping("/{id}/dimensions")
    public Response configurePolicyDimensions(
            @PathVariable Long id,
            @RequestBody List<ModerationPolicyCmdExe.PolicyDimensionConfig> configs,
            HttpServletRequest request) {
        
        log.info("Admin configuring policy dimensions: policyId={}", id);
        
        return policyCommandExecutor.configurePolicyDimensions(id, configs, getUserFromRequest(request));
    }

    @GetMapping("/dimensions")
    public SingleResponse<List<ModerationDimensionDTO>> getAllActiveDimensions() {
        log.debug("Admin querying all active dimensions");
        
        return policyQueryExecutor.getAllActiveDimensions();
    }

    @GetMapping("/templates")
    public SingleResponse<List<ModerationPolicyTemplateDTO>> getAllActiveTemplates() {
        log.debug("Admin querying all active templates");
        
        return policyQueryExecutor.getAllActiveTemplates();
    }

    /**
     * 从请求中获取用户信息
     */
    private String getUserFromRequest(HttpServletRequest request) {
        // 这里应该从认证上下文或会话中获取用户信息
        // 简化实现，实际项目中应该有统一的用户获取逻辑
        String user = request.getHeader("X-User-Id");
        if (user == null) {
            user = "admin"; // 默认值
        }
        return user;
    }
}