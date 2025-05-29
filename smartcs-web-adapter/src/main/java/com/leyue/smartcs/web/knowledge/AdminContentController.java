package com.leyue.smartcs.web.knowledge;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ContentService;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.dto.knowledge.ContentDTO;
import com.leyue.smartcs.dto.knowledge.ContentCreateCmd;
import com.leyue.smartcs.dto.knowledge.ContentUpdateCmd;
import com.leyue.smartcs.dto.knowledge.ContentListQry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 内容管理Controller
 */
@RestController
@RequestMapping("/admin/content")
public class AdminContentController {
    
    @Autowired
    private ContentService contentService;
    
    /**
     * 创建内容
     */
    @PostMapping
    public SingleResponse<ContentDTO> createContent(@RequestBody ContentCreateCmd cmd) {
        return contentService.createContent(cmd);
    }
    
    /**
     * 更新内容
     */
    @PutMapping("/{id}")
    public Response updateContent(@PathVariable Long id, @RequestBody ContentUpdateCmd cmd) {
        cmd.setId(id);
        return contentService.updateContent(cmd);
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
    public PageResponse<ContentDTO> listContents(ContentListQry qry) {
        return contentService.listContents(qry);
    }
    
    /**
     * 触发内容解析,指定内容(将内容解析成文本)
     */
    @PostMapping("/{id}/parse")
    public Response triggerContentParsing(@PathVariable Long id) {
        return contentService.triggerContentParsing(id);
    }
    
    /**
     * 触发内容向量化(先将文本分段，然后向量存储)
     */
    @PostMapping("/{id}/vectorize")
    public Response triggerContentVectorization(@PathVariable Long id,@RequestParam String strategyName) {
        return contentService.triggerContentVectorization(id,strategyName);
    }
} 