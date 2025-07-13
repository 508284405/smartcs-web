package com.leyue.smartcs.web.knowledge;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ContentService;
import com.leyue.smartcs.dto.knowledge.ContentDTO;
import com.leyue.smartcs.dto.knowledge.ContentCreateCmd;
import com.leyue.smartcs.dto.knowledge.ContentUpdateCmd;
import com.leyue.smartcs.dto.knowledge.ContentStatusUpdateCmd;
import com.leyue.smartcs.dto.knowledge.DocumentSearchRequest;
import com.leyue.smartcs.dto.knowledge.DocumentSearchResultDTO;

import jakarta.validation.Valid;

import com.leyue.smartcs.dto.knowledge.ContentListQry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 内容管理Controller
 */
@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {
    
    @Autowired
    private ContentService contentService;
    
    /**
     * 创建内容
     */
    @PostMapping
    public SingleResponse<ContentDTO> createContent(@RequestBody @Valid ContentCreateCmd cmd) {
        return contentService.createContent(cmd);
    }
    
    /**
     * 更新内容
     */
    @PutMapping
    public Response updateContent(@RequestBody @Valid ContentUpdateCmd cmd) {
        return contentService.updateContent(cmd);
    }
    
    /**
     * 更新内容状态
     */
    @PutMapping("/status")
    public Response updateContentStatus(@RequestBody @Valid ContentStatusUpdateCmd cmd) {
        return contentService.updateContentStatus(cmd);
    }
    
    /**
     * 查询内容详情
     */
    @GetMapping("/{id}")
    public SingleResponse<ContentDTO> getContent(@PathVariable Long id) {
        return contentService.getContent(id);
    }
    
    /**
     * 删除内容
     */
    @DeleteMapping("/{id}")
    public Response deleteContent(@PathVariable Long id) {
        return contentService.deleteContent(id);
    }
    
    /**
     * 查询内容列表
     */
    @GetMapping
    public PageResponse<ContentDTO> listContents(@Valid ContentListQry qry) {
        return contentService.listContents(qry);
    }
    
    /**
     * 触发内容解析,指定内容(将内容解析成文本),并向量存储Redis
     */
    @PostMapping("/{id}/parse")
    public Response triggerContentParsing(@PathVariable Long id) {
        return contentService.triggerContentParsing(id);
    }

    /**
     * 向量搜索文档内容
     */
    @PostMapping("/search")
    public MultiResponse<DocumentSearchResultDTO> vectorSearch(@RequestBody @Valid DocumentSearchRequest request) {
        return contentService.vectorSearch(request);
    }
} 