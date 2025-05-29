package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.dto.knowledge.ContentDTO;
import com.leyue.smartcs.knowledge.convertor.ContentConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内容查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentGetQryExe {

    private final ContentGateway contentGateway;
    private final ContentConvertor contentConvertor;

    /**
     * 执行内容查询
     * @param id 内容ID
     * @return 内容信息
     */
    public SingleResponse<ContentDTO> execute(Long id) {
        log.info("执行内容查询，ID: {}", id);
        
        // 参数校验
        if (id == null) {
            throw new BizException("内容ID不能为空");
        }
        
        // 查询内容
        Content content = contentGateway.findById(id);
        if (content == null) {
            throw new BizException("内容不存在，ID: " + id);
        }
        
        // 转换为DTO
        ContentDTO contentDTO = contentConvertor.toDTO(content);
        
        log.info("内容查询成功，ID: {}", id);
        return SingleResponse.of(contentDTO);
    }
}