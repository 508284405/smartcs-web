package com.leyue.smartcs.web.knowledge;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.FaqService;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.leyue.smartcs.dto.knowledge.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 知识域REST接口
 */
@RestController
@RequestMapping("/api/admin/faq")
@RequiredArgsConstructor
@Slf4j
public class AdminFaqController {

    private final FaqService faqService;

    /**
     * 创建/更新FAQ
     */
    @PostMapping
    public SingleResponse<FaqDTO> addFaq(@RequestBody FaqAddCmd cmd) {
        log.info("创建/更新FAQ请求: {}", cmd);
        return faqService.addFaq(cmd);
    }

    /**
     * 删除FAQ
     */
    @DeleteMapping("/{id}")
    public Response deleteFaq(@PathVariable("id") Long id) {
        log.info("删除FAQ请求, ID: {}", id);
        SingleClientObject<Long> cmd = SingleClientObject.of(id);
        return faqService.deleteFaq(cmd);
    }

    /**
     * 查询FAQ列表
     */
    @GetMapping
    public PageResponse<FaqDTO> listFaqs(KnowledgeSearchQry qry) {
        return faqService.listFaqs(qry);
    }

    /**
     * FAQ 向量检索
     */
    @GetMapping("/search")
    public MultiResponse<FaqDTO> searchFaq(FaqSearchQry qry) {
        return faqService.searchFaq(qry);
    }
}