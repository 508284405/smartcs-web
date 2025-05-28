package com.leyue.smartcs.api;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import com.leyue.smartcs.dto.knowledge.FaqSearchQry;
import com.leyue.smartcs.dto.knowledge.FaqAddCmd;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchQry;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;

public interface FaqService {
     /**
     * 创建或更新FAQ
     *
     * @param cmd FAQ创建/更新命令
     * @return 创建/更新的FAQ
     */
    SingleResponse<FaqDTO> addFaq(FaqAddCmd cmd);
    
    /**
     * 删除FAQ
     *
     * @param idCmd FAQ ID
     * @return 操作结果
     */
    Response deleteFaq(SingleClientObject<Long> idCmd);
    
    /**
     * 查询FAQ列表
     *
     * @param qry 查询条件
     * @return FAQ列表（分页）
     */
    PageResponse<FaqDTO> listFaqs(KnowledgeSearchQry qry);

    /**
     * FAQ 向量检索
     *
     * @param qry 查询条件
     * @return 检索结果
     */
    MultiResponse<FaqDTO> searchFaq(FaqSearchQry qry);
}
