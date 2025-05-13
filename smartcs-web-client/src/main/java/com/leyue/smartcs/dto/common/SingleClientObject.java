package com.leyue.smartcs.dto.common;

import com.alibaba.cola.dto.ClientObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 单值对象传输类
 * @param <T> 内部值类型
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SingleClientObject<T> extends ClientObject {
    
    /**
     * 单个值
     */
    private T value;
    
    /**
     * 构造方法
     * @param value 值
     */
    public SingleClientObject(T value) {
        this.value = value;
    }
    
    /**
     * 无参构造方法
     */
    public SingleClientObject() {
    }
    
    /**
     * 创建实例的工厂方法
     * @param value 值
     * @param <T> 值类型
     * @return 新实例
     */
    public static <T> SingleClientObject<T> of(T value) {
        return new SingleClientObject<>(value);
    }
} 