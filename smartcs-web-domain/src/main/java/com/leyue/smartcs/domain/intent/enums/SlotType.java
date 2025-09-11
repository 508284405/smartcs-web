package com.leyue.smartcs.domain.intent.enums;

/**
 * 槽位类型枚举
 * 
 * @author Claude
 */
public enum SlotType {
    
    /**
     * 字符串类型
     */
    STRING,
    
    /**
     * 数值类型
     */
    NUMBER,
    
    /**
     * 整数类型
     */
    INTEGER,
    
    /**
     * 日期类型
     */
    DATE,
    
    /**
     * 时间类型
     */
    TIME,
    
    /**
     * 日期时间类型
     */
    DATETIME,
    
    /**
     * 枚举类型
     */
    ENUM,
    
    /**
     * 布尔类型
     */
    BOOLEAN,
    
    /**
     * 电子邮件
     */
    EMAIL,
    
    /**
     * 手机号码
     */
    PHONE,
    
    /**
     * URL地址
     */
    URL,
    
    /**
     * 金额类型
     */
    MONEY,
    
    /**
     * 地理位置
     */
    LOCATION,
    
    /**
     * JSON对象
     */
    JSON,
    
    /**
     * 数组类型
     */
    ARRAY,
    
    /**
     * 文件路径
     */
    FILE_PATH,
    
    /**
     * 实体引用
     */
    ENTITY_REF
}