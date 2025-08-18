package com.leyue.smartcs.web.knowledge;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.KnowledgeBaseService;
import com.leyue.smartcs.dto.knowledge.*;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库管理Controller
 */
@RestController
@RequestMapping("/api/admin/knowledge-base")
public class AdminKnowledgeBaseController {
    
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    
    /**
     * 创建知识库
     */
    @PostMapping
    public SingleResponse<KnowledgeBaseDTO> createKnowledgeBase(@RequestBody @Valid KnowledgeBaseCreateCmd cmd) {
        return knowledgeBaseService.createKnowledgeBase(cmd);
    }
    
    /**
     * 更新知识库
     */
    @PutMapping
    public Response updateKnowledgeBase(@RequestBody @Valid KnowledgeBaseUpdateCmd cmd) {
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
    public PageResponse<KnowledgeBaseDTO> listKnowledgeBases(@Valid KnowledgeBaseListQry qry) {
        return knowledgeBaseService.listKnowledgeBases(qry);
    }

    /**
     * 文本检索
     */
    @PostMapping("/search/text")
    public MultiResponse<EmbeddingWithScore> searchByText(@RequestBody @Valid KnowledgeSearchQry qry) {
        return knowledgeBaseService.searchByText(qry);
    }

    /**
     * 通用文档分块
     */
    @PostMapping("/chunk/general")
    public MultiResponse<ChunkDTO> generalChunk(@RequestBody @Valid KnowledgeGeneralChunkCmd cmd) {
        return knowledgeBaseService.generalChunk(cmd);
    }

    /**
     * 父子文档分块
     */
    @PostMapping("/chunk/parent-child")
    public MultiResponse<ChunkDTO> parentChildChunk(@RequestBody @Valid KnowledgeParentChildChunkCmd cmd) {
        return knowledgeBaseService.parentChildChunk(cmd);
    }

    /**
     * 获取知识库设置
     */
    @GetMapping("/{id}/settings")
    public SingleResponse<KnowledgeBaseSettingsDTO> getKnowledgeBaseSettings(@PathVariable Long id) {
        return knowledgeBaseService.getKnowledgeBaseSettings(id);
    }

    /**
     * 更新知识库设置
     */
    @PutMapping("/{id}/settings")
    public Response updateKnowledgeBaseSettings(@PathVariable Long id, 
                                               @RequestBody @Valid KnowledgeBaseSettingsUpdateCmd cmd) {
        // 验证路径ID与请求体ID是否一致
        if (!id.equals(cmd.getId())) {
            return Response.buildFailure("PARAMETER_MISMATCH", "路径参数ID与请求体ID不匹配");
        }
        return knowledgeBaseService.updateKnowledgeBaseSettings(cmd);
    }

    /**
     * 召回测试
     */
    @PostMapping("/{id}/recall-test")
    public MultiResponse<RecallTestResultDTO> recallTest(@PathVariable Long id, 
                                                          @RequestBody @Valid RecallTestQry qry) {
        return knowledgeBaseService.recallTest(qry);
    }
} 