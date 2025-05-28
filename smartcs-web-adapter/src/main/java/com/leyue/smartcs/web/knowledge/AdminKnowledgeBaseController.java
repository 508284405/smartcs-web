package com.leyue.smartcs.web.knowledge;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.KnowledgeBaseService;
import com.leyue.smartcs.dto.knowledge.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库管理Controller
 */
@RestController
@RequestMapping("/admin/knowledge-base")
public class AdminKnowledgeBaseController {
    
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    
    /**
     * 创建知识库
     */
    @PostMapping
    public SingleResponse<KnowledgeBaseDTO> createKnowledgeBase(@RequestBody KnowledgeBaseCreateCmd cmd) {
        return knowledgeBaseService.createKnowledgeBase(cmd);
    }
    
    /**
     * 更新知识库
     */
    @PutMapping("/{id}")
    public Response updateKnowledgeBase(@PathVariable Long id, @RequestBody KnowledgeBaseUpdateCmd cmd) {
        cmd.setId(id);
        return knowledgeBaseService.updateKnowledgeBase(cmd);
    }
    
    /**
     * 查询知识库详情
     */
    @GetMapping("/{id}")
    public SingleResponse<KnowledgeBaseDTO> getKnowledgeBase(@PathVariable Long id) {
        return knowledgeBaseService.getKnowledgeBase(id);
    }
    
    /**
     * 删除知识库
     */
    @DeleteMapping("/{id}")
    public Response deleteKnowledgeBase(@PathVariable Long id) {
        return knowledgeBaseService.deleteKnowledgeBase(id);
    }
    
    /**
     * 查询知识库列表
     */
    @GetMapping
    public PageResponse<KnowledgeBaseDTO> listKnowledgeBases(KnowledgeBaseListQry qry) {
        return knowledgeBaseService.listKnowledgeBases(qry);
    }


    /**
     * 文本检索
     */
    @PostMapping("/search/text")
    public MultiResponse<EmbeddingWithScore> searchByText(@RequestBody KnowledgeSearchQry qry) {
        return knowledgeBaseService.searchByText(qry);
    }
} 