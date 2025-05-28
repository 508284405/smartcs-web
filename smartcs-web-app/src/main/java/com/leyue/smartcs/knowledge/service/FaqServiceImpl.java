package com.leyue.smartcs.knowledge.service;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.FaqService;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import com.leyue.smartcs.dto.knowledge.FaqSearchQry;
import com.leyue.smartcs.dto.knowledge.FaqAddCmd;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchQry;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.knowledge.executor.command.FaqAddCmdExe;
import com.leyue.smartcs.knowledge.executor.command.FaqDeleteCmdExe;
import com.leyue.smartcs.knowledge.executor.query.FaqListQryExe;
import com.leyue.smartcs.knowledge.executor.query.FaqSearchQryExe;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {
    private final FaqAddCmdExe faqAddCmdExe;
    private final FaqDeleteCmdExe faqDeleteCmdExe;
    private final FaqListQryExe faqListQryExe;
    private final FaqSearchQryExe faqSearchQryExe;
    
    @Override
    public SingleResponse<FaqDTO> addFaq(FaqAddCmd cmd) {
        return faqAddCmdExe.execute(cmd);
    }
    
    @Override
    public Response deleteFaq(SingleClientObject<Long> idCmd) {
        return faqDeleteCmdExe.execute(idCmd);
    }
    
    @Override
    public PageResponse<FaqDTO> listFaqs(KnowledgeSearchQry qry) {
        return faqListQryExe.execute(qry);
    }
    
    @Override
    public MultiResponse<FaqDTO> searchFaq(FaqSearchQry qry) {
        return faqSearchQryExe.execute(qry);
    }
}
