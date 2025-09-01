package com.leyue.smartcs.domain.chat;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果领域对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    
    /**
     * 数据列表
     */
    private List<T> data;
    
    /**
     * 总数量
     */
    private long totalCount;
    
    /**
     * 当前页数
     */
    private int pageIndex;
    
    /**
     * 页面大小
     */
    private int pageSize;
}