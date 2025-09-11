package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 字典条目分页结果
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryEntryPageResult {
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 页大小
     */
    private Integer pageSize;
    
    /**
     * 总条数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 数据列表
     */
    private List<DictionaryEntryDTO> list;
}