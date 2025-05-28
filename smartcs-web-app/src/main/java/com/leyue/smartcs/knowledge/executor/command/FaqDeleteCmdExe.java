package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.common.SingleClientObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.leyue.smartcs.domain.common.Constants.FAQ_INDEX_REDISEARCH;

/**
 * FAQ删除命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FaqDeleteCmdExe {
    
    private final FaqGateway faqGateway;

    private final SearchGateway searchGateway;
    /**
     * 执行FAQ删除命令
     * @param cmd FAQ ID
     * @return 删除结果
     */
    public Response execute(SingleClientObject<Long> cmd) {
        log.info("执行FAQ删除命令: {}", cmd);
        
        // 参数校验
        if (cmd.getValue() == null) {
            throw new BizException("FAQ ID不能为空");
        }
        
        Long faqId = cmd.getValue();
        
        // 检查FAQ是否存在
        if (faqGateway.findById(faqId).isEmpty()) {
            throw new BizException("FAQ不存在，ID: " + faqId);
        }
        
        // 执行删除
        boolean success = faqGateway.deleteById(faqId);
        
        if (!success) {
            return Response.buildFailure("FAQ-DELETE-ERROR", "删除FAQ失败");
        }

        // 删除索引
        searchGateway.deleteDocument(FAQ_INDEX_REDISEARCH, faqId);
        
        log.info("成功删除FAQ, ID: {}", faqId);
        return Response.buildSuccess();
    }
} 