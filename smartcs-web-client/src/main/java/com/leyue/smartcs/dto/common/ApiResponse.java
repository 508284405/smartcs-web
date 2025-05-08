package com.leyue.smartcs.dto.common;

import lombok.Data;

/**
 * API通用响应对象
 * @param <T> 数据类型
 */
@Data
public class ApiResponse<T> {
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误码
     */
    private String errCode;
    
    /**
     * 错误信息
     */
    private String errMessage;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 创建成功响应
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    /**
     * 创建失败响应
     * @param errCode 错误码
     * @param errMessage 错误信息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> failure(String errCode, String errMessage) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setErrCode(errCode);
        response.setErrMessage(errMessage);
        return response;
    }
} 