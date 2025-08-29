package com.leyue.smartcs.dto.chat;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表情反应DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReactionDTO {
    
    /**
     * 表情符号
     */
    private String emoji;
    
    /**
     * 表情名称
     */
    private String name;
    
    /**
     * 反应总数
     */
    private Integer count;
    
    /**
     * 反应用户列表
     */
    private List<String> userIds;
    
    /**
     * 当前用户是否已反应
     */
    private Boolean hasReacted;
}