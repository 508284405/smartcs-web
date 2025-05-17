package com.leyue.smartcs.dto.knowledge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FAQ全文检索结果视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaqScoreVO {

    private Long id;

    private Float score;
} 