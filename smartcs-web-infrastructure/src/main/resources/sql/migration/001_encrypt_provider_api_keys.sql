-- 数据迁移脚本：为现有API Key进行加密存储
-- 执行前请确保：
-- 1. 备份现有数据
-- 2. 已正确配置 smartcs.secrets.keys 和 smartcs.secrets.activeKid
-- 3. 在非生产环境测试通过

-- 注意：此脚本仅为数据结构迁移，实际的加密转换需要通过Java应用程序完成
-- 因为加密过程需要使用SecretCryptoService，无法直接在SQL中完成

-- 步骤1: 验证新字段已存在
SELECT COUNT(*) as field_check FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 't_model_provider' 
  AND COLUMN_NAME IN ('api_key_cipher', 'api_key_iv', 'api_key_kid');
-- 期望结果：3

-- 步骤2: 查看需要迁移的数据
SELECT 
    id,
    provider_type,
    CASE 
        WHEN api_key IS NOT NULL AND LENGTH(TRIM(api_key)) > 0 THEN 'HAS_KEY'
        ELSE 'NO_KEY'
    END as key_status,
    CASE 
        WHEN api_key_cipher IS NOT NULL AND LENGTH(api_key_cipher) > 0 THEN 'ENCRYPTED'
        ELSE 'NOT_ENCRYPTED'
    END as encryption_status
FROM t_model_provider 
WHERE is_deleted = 0
ORDER BY id;

-- 步骤3: 统计迁移范围
SELECT 
    COUNT(*) as total_providers,
    SUM(CASE WHEN api_key IS NOT NULL AND LENGTH(TRIM(api_key)) > 0 THEN 1 ELSE 0 END) as providers_with_keys,
    SUM(CASE WHEN api_key_cipher IS NOT NULL AND LENGTH(api_key_cipher) > 0 THEN 1 ELSE 0 END) as already_encrypted
FROM t_model_provider 
WHERE is_deleted = 0;

-- 重要提示：
-- 实际的加密迁移需要通过Java应用程序完成，建议创建一个迁移任务或管理命令
-- 迁移步骤：
-- 1. 查询所有有api_key但未加密的记录
-- 2. 使用SecretCryptoService.encryptForProviderApiKey()进行加密
-- 3. 更新数据库记录，设置api_key_cipher, api_key_iv, api_key_kid
-- 4. 验证加密结果
-- 5. 清空原api_key字段（可选，用于过渡期兼容）

-- 以下是迁移完成后的验证查询：

-- 验证加密迁移结果
SELECT 
    id,
    provider_type,
    CASE 
        WHEN api_key_cipher IS NOT NULL AND LENGTH(api_key_cipher) > 0 THEN '已加密'
        WHEN api_key IS NOT NULL AND LENGTH(TRIM(api_key)) > 0 THEN '待加密'
        ELSE '无密钥'
    END as encryption_status,
    api_key_kid,
    CASE 
        WHEN api_key_iv IS NOT NULL AND LENGTH(api_key_iv) > 0 THEN LENGTH(api_key_iv)
        ELSE 0
    END as iv_length
FROM t_model_provider 
WHERE is_deleted = 0
ORDER BY id;

-- 清理查询（迁移完成且验证通过后执行）
-- UPDATE t_model_provider SET api_key = NULL WHERE api_key_cipher IS NOT NULL AND LENGTH(api_key_cipher) > 0;