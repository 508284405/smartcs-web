package com.leyue.smartcs.domain.dictionary.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 字典配置值对象
 * 封装多租户、多渠道、多领域的配置信息
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryConfig {
    
    /**
     * 租户标识
     */
    private String tenant;
    
    /**
     * 渠道标识
     */
    private String channel;
    
    /**
     * 领域标识
     */
    private String domain;
    
    /**
     * 创建字典配置
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 字典配置对象
     */
    public static DictionaryConfig of(String tenant, String channel, String domain) {
        validateParams(tenant, channel, domain);
        
        return DictionaryConfig.builder()
                .tenant(tenant.trim())
                .channel(channel.trim())
                .domain(domain.trim())
                .build();
    }
    
    /**
     * 获取配置标识符
     * 格式: tenant:channel:domain
     * 
     * @return 配置标识符
     */
    public String getIdentifier() {
        return String.format("%s:%s:%s", tenant, channel, domain);
    }
    
    /**
     * 检查是否为默认配置
     * 
     * @return 是否为默认配置
     */
    public boolean isDefault() {
        return "default".equals(tenant) && "default".equals(channel) && "default".equals(domain);
    }
    
    /**
     * 检查是否与指定配置匹配
     * 
     * @param otherTenant 其他租户
     * @param otherChannel 其他渠道
     * @param otherDomain 其他领域
     * @return 是否匹配
     */
    public boolean matches(String otherTenant, String otherChannel, String otherDomain) {
        return Objects.equals(tenant, otherTenant) && 
               Objects.equals(channel, otherChannel) && 
               Objects.equals(domain, otherDomain);
    }
    
    /**
     * 参数校验
     */
    private static void validateParams(String tenant, String channel, String domain) {
        if (tenant == null || tenant.trim().isEmpty()) {
            throw new IllegalArgumentException("租户标识不能为空");
        }
        if (channel == null || channel.trim().isEmpty()) {
            throw new IllegalArgumentException("渠道标识不能为空");
        }
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("领域标识不能为空");
        }
        
        // 长度限制
        if (tenant.trim().length() > 50) {
            throw new IllegalArgumentException("租户标识长度不能超过50个字符");
        }
        if (channel.trim().length() > 50) {
            throw new IllegalArgumentException("渠道标识长度不能超过50个字符");
        }
        if (domain.trim().length() > 50) {
            throw new IllegalArgumentException("领域标识长度不能超过50个字符");
        }
        
        // 格式校验（只允许字母、数字、下划线、短划线）
        if (!isValidIdentifier(tenant.trim())) {
            throw new IllegalArgumentException("租户标识格式无效，只允许字母、数字、下划线、短划线");
        }
        if (!isValidIdentifier(channel.trim())) {
            throw new IllegalArgumentException("渠道标识格式无效，只允许字母、数字、下划线、短划线");
        }
        if (!isValidIdentifier(domain.trim())) {
            throw new IllegalArgumentException("领域标识格式无效，只允许字母、数字、下划线、短划线");
        }
    }
    
    /**
     * 校验标识符格式
     */
    private static boolean isValidIdentifier(String identifier) {
        return identifier.matches("^[a-zA-Z0-9_-]+$");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictionaryConfig that = (DictionaryConfig) o;
        return Objects.equals(tenant, that.tenant) && 
               Objects.equals(channel, that.channel) && 
               Objects.equals(domain, that.domain);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tenant, channel, domain);
    }
    
    @Override
    public String toString() {
        return getIdentifier();
    }
}