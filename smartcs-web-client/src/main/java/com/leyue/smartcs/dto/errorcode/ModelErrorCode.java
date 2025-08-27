package com.leyue.smartcs.dto.errorcode;

/**
 * 模型相关错误码枚举
 * 统一定义所有模型相关的异常信息
 */
public enum ModelErrorCode {

    // 嵌入模型相关错误
    NO_EMBEDDING_MODEL("NO_EMBEDDING_MODEL", "未找到可用的嵌入模型"),
    GET_DEFAULT_EMBEDDING_MODEL_FAILED("GET_DEFAULT_EMBEDDING_MODEL_FAILED", "获取默认嵌入模型失败"),
    EMBEDDING_MODEL_NOT_FOUND("EMBEDDING_MODEL_NOT_FOUND", "指定的嵌入模型不存在"),
    EMBEDDING_MODEL_INACTIVE("EMBEDDING_MODEL_INACTIVE", "嵌入模型未激活"),
    
    // LLM模型相关错误
    NO_LLM_MODEL("NO_LLM_MODEL", "未找到可用的LLM模型"),
    GET_DEFAULT_LLM_MODEL_FAILED("GET_DEFAULT_LLM_MODEL_FAILED", "获取默认LLM模型失败"),
    LLM_MODEL_NOT_FOUND("LLM_MODEL_NOT_FOUND", "指定的LLM模型不存在"),
    LLM_MODEL_INACTIVE("LLM_MODEL_INACTIVE", "LLM模型未激活"),
    
    // 模型管理相关错误
    MODEL_NOT_FOUND("MODEL_NOT_FOUND", "模型不存在"),
    MODEL_CREATE_FAILED("MODEL_CREATE_FAILED", "模型创建失败"),
    MODEL_UPDATE_FAILED("MODEL_UPDATE_FAILED", "模型更新失败"),
    MODEL_DELETE_FAILED("MODEL_DELETE_FAILED", "模型删除失败"),
    MODEL_ENABLE_FAILED("MODEL_ENABLE_FAILED", "模型启用失败"),
    MODEL_DISABLE_FAILED("MODEL_DISABLE_FAILED", "模型停用失败"),
    
    // 模型配置相关错误
    MODEL_CONFIG_INVALID("MODEL_CONFIG_INVALID", "模型配置无效"),
    MODEL_API_KEY_MISSING("MODEL_API_KEY_MISSING", "模型API密钥缺失"),
    MODEL_BASE_URL_INVALID("MODEL_BASE_URL_INVALID", "模型基础URL无效"),
    MODEL_TYPE_UNSUPPORTED("MODEL_TYPE_UNSUPPORTED", "不支持的模型类型"),
    
    // 模型推理相关错误
    MODEL_INFERENCE_FAILED("MODEL_INFERENCE_FAILED", "模型推理失败"),
    MODEL_INFERENCE_TIMEOUT("MODEL_INFERENCE_TIMEOUT", "模型推理超时"),
    MODEL_RESPONSE_INVALID("MODEL_RESPONSE_INVALID", "模型响应格式无效"),
    
    // 向量化相关错误
    CHUNK_VECTORIZE_FAILED("CHUNK_VECTORIZE_FAILED", "切片向量化处理失败"),
    CHUNK_NOT_FOUND("CHUNK_NOT_FOUND", "切片不存在"),
    CHUNK_CONTENT_EMPTY("CHUNK_CONTENT_EMPTY", "切片内容为空，无法进行向量化"),
    
    // 内容解析相关错误
    CONTENT_PARSING_FAILED("CONTENT_PARSING_FAILED", "内容解析失败"),
    CONTENT_NOT_FOUND("CONTENT_NOT_FOUND", "内容不存在"),
    CONTENT_STATUS_INVALID("CONTENT_STATUS_INVALID", "内容状态无效"),
    
    // 动态模型管理相关错误
    DYNAMIC_MODEL_BUILD_FAILED("DYNAMIC_MODEL_BUILD_FAILED", "动态模型构建失败"),
    DYNAMIC_MODEL_CACHE_ERROR("DYNAMIC_MODEL_CACHE_ERROR", "动态模型缓存错误"),
    DYNAMIC_MODEL_NOT_AVAILABLE("DYNAMIC_MODEL_NOT_AVAILABLE", "动态模型不可用");

    private final String errCode;
    private final String errDesc;

    ModelErrorCode(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }

    public String getErrCode() {
        return errCode;
    }

    public String getErrDesc() {
        return errDesc;
    }

    /**
     * 根据错误码获取错误描述
     * 
     * @param errCode 错误码
     * @return 错误描述，如果未找到则返回默认描述
     */
    public static String getErrDescByCode(String errCode) {
        for (ModelErrorCode errorCode : ModelErrorCode.values()) {
            if (errorCode.getErrCode().equals(errCode)) {
                return errorCode.getErrDesc();
            }
        }
        return "未知的模型错误";
    }

    /**
     * 检查错误码是否存在
     * 
     * @param errCode 错误码
     * @return true如果错误码存在，否则false
     */
    public static boolean containsCode(String errCode) {
        for (ModelErrorCode errorCode : ModelErrorCode.values()) {
            if (errorCode.getErrCode().equals(errCode)) {
                return true;
            }
        }
        return false;
    }
}