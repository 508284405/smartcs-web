package com.leyue.smartcs.model.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.leyue.smartcs.common.secret.SecretCryptoService;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.model.dataobject.ProviderDO;

import lombok.extern.slf4j.Slf4j;

/**
 * 模型提供商转换器
 * 负责处理API Key的加密和解密转换
 */
@Slf4j
@Mapper(componentModel = "spring")
public abstract class ProviderConvertor {
    
    @Autowired
    private SecretCryptoService secretCryptoService;
    
    /**
     * DO转领域对象
     * 不填充明文apiKey，根据密文存在情况设置hasApiKey
     */
    @Mapping(target = "apiKey", ignore = true)
    @Mapping(target = "hasApiKey", expression = "java(determineHasApiKey(providerDO))")
    @Mapping(target = "supportedModelTypesList", ignore = true)
    public abstract Provider toDomain(ProviderDO providerDO);
    
    /**
     * 领域对象转DO
     * 如果apiKey非空，则进行加密；否则保留原有密文不变
     */
    @Mapping(target = "apiKeyCipher", ignore = true)
    @Mapping(target = "apiKeyIv", ignore = true) 
    @Mapping(target = "apiKeyKid", ignore = true)
    public abstract ProviderDO toDO(Provider provider);
    
    /**
     * 领域对象转DO（用于更新，支持加密新的API Key）
     */
    public ProviderDO toDOWithEncryption(Provider provider, ProviderDO existingDO) {
        ProviderDO newDO = toDO(provider);
        
        // 如果provider有新的apiKey，则加密并替换
        if (provider.getApiKey() != null && !provider.getApiKey().trim().isEmpty()) {
            try {
                SecretCryptoService.EncryptResult encryptResult = 
                    secretCryptoService.encryptForProviderApiKey(provider.getApiKey());
                
                newDO.setApiKeyCipher(encryptResult.getCiphertext());
                newDO.setApiKeyIv(encryptResult.getIv());
                newDO.setApiKeyKid(encryptResult.getKid());
                // 清空明文apiKey（过渡期保留）
                newDO.setApiKey(null);
                
                log.debug("API Key已加密，Provider ID: {}, Kid: {}", 
                    provider.getId(), encryptResult.getKid());
                
            } catch (Exception e) {
                log.error("API Key加密失败，Provider ID: {}", provider.getId(), e);
                throw new RuntimeException("API Key加密失败", e);
            }
        } else if (existingDO != null) {
            // 如果没有新apiKey，保持原有密文不变
            newDO.setApiKeyCipher(existingDO.getApiKeyCipher());
            newDO.setApiKeyIv(existingDO.getApiKeyIv());
            newDO.setApiKeyKid(existingDO.getApiKeyKid());
            newDO.setApiKey(existingDO.getApiKey());
        }
        
        return newDO;
    }
    
    /**
     * 解密API Key（仅在模型调用时使用）
     * 
     * @param providerDO 提供商数据对象
     * @return 解密后的明文API Key，如果无密文则返回明文字段（兼容过渡期）
     */
    public String decryptApiKey(ProviderDO providerDO) {
        if (providerDO == null) {
            return null;
        }
        
        // 优先使用加密存储的apiKey
        if (providerDO.getApiKeyCipher() != null && providerDO.getApiKeyCipher().length > 0
            && providerDO.getApiKeyIv() != null && providerDO.getApiKeyIv().length > 0
            && providerDO.getApiKeyKid() != null && !providerDO.getApiKeyKid().trim().isEmpty()) {
            
            try {
                String decrypted = secretCryptoService.decryptProviderApiKey(
                    providerDO.getApiKeyCipher(), 
                    providerDO.getApiKeyIv(), 
                    providerDO.getApiKeyKid()
                );
                
                log.debug("API Key已解密，Provider ID: {}, Kid: {}", 
                    providerDO.getId(), providerDO.getApiKeyKid());
                return decrypted;
                
            } catch (Exception e) {
                log.error("API Key解密失败，Provider ID: {}, Kid: {}", 
                    providerDO.getId(), providerDO.getApiKeyKid(), e);
                throw new RuntimeException("API Key解密失败", e);
            }
        }
        
        // 兼容过渡期：如果没有密文，返回明文字段
        if (providerDO.getApiKey() != null && !providerDO.getApiKey().trim().isEmpty()) {
            log.warn("使用明文API Key（过渡期兼容），Provider ID: {}", providerDO.getId());
            return providerDO.getApiKey();
        }
        
        return null;
    }
    
    /**
     * 判断是否已设置API Key
     */
    protected Boolean determineHasApiKey(ProviderDO providerDO) {
        if (providerDO == null) {
            return Boolean.FALSE;
        }
        
        // 优先检查加密字段
        boolean hasCipher = providerDO.getApiKeyCipher() != null 
                           && providerDO.getApiKeyCipher().length > 0;
                           
        // 兼容明文字段（过渡期）
        boolean hasPlaintext = providerDO.getApiKey() != null 
                              && !providerDO.getApiKey().trim().isEmpty();
                              
        return hasCipher || hasPlaintext;
    }
}