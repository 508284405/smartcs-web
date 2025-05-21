package com.leyue.smartcs.web.knowledge;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.KnowledgeService;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.leyue.smartcs.dto.knowledge.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 知识域REST接口
 */
@RestController
@RequestMapping("/api/admin/knowledge")
@RequiredArgsConstructor
@Slf4j
public class AdminKnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 创建/更新FAQ
     */
    @PostMapping("/faq")
    public SingleResponse<FaqDTO> addFaq(@RequestBody FaqAddCmd cmd) {
        log.info("创建/更新FAQ请求: {}", cmd);
        return knowledgeService.addFaq(cmd);
    }

    /**
     * 删除FAQ
     */
    @DeleteMapping("/faq/{id}")
    public Response deleteFaq(@PathVariable("id") Long id) {
        log.info("删除FAQ请求, ID: {}", id);
        SingleClientObject<Long> cmd = SingleClientObject.of(id);
        return knowledgeService.deleteFaq(cmd);
    }

    /**
     * 查询FAQ列表
     */
    @GetMapping("/faq")
    public PageResponse<FaqDTO> listFaqs(KnowledgeSearchQry qry) {
        return knowledgeService.listFaqs(qry);
    }

    /**
     * 上传文档
     */
    @PostMapping("/doc")
    public SingleResponse<DocDTO> addDoc(@RequestBody DocAddCmd cmd) {
        log.info("上传文档请求: {}", cmd);
        return knowledgeService.addDoc(cmd);
    }

    /**
     * 触发文档向量生成
     */
    @PostMapping("/doc/{id}/trigger-embedding")
    public Response triggerDocEmbedding(@PathVariable("id") Long id) {
        log.info("触发文档向量生成请求, ID: {}", id);
        SingleClientObject<Long> cmd = SingleClientObject.of(id);
        return knowledgeService.triggerDocEmbedding(cmd);
    }

    /**
     * 查询文档列表
     */
    @GetMapping("/doc")
    public PageResponse<DocDTO> listDocs(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {

        log.info("查询文档列表请求: keyword={}, page={}, size={}", keyword, page, size);

        KnowledgeSearchQry qry = new KnowledgeSearchQry();
        qry.setKeyword(keyword);
        qry.setPageIndex(page);
        qry.setPageSize(size);

        return knowledgeService.listDocs(qry);
    }
//
//    /**
//     * 查询文档段落向量
//     */
//    @GetMapping("/doc/{id}/embeddings")
//    public MultiResponse<EmbeddingDTO> listEmbeddingsByDoc(@PathVariable("id") Long id) {
//        log.info("查询文档段落向量请求, ID: {}", id);
//        SingleClientObject<Long> cmd = SingleClientObject.of(id);
//        return knowledgeService.listEmbeddingsByDoc(cmd);
//    }
//
//    /**
//     * 批量添加向量
//     */
//    @PostMapping("/embeddings/batch")
//    public Response addEmbeddings(@RequestBody EmbeddingAddCmd cmd) {
//        log.info("批量添加向量请求: docId={}, items={}", cmd.getDocId(), cmd.getItems().size());
//        return knowledgeService.addEmbeddings(cmd);
//    }
//
//    /**
//     * 向量检索
//     */
//    @PostMapping("/search/vector")
//    public MultiResponse<KnowledgeSearchResult> searchByVector(@RequestBody KnowledgeSearchQry qry) {
//        log.info("向量检索请求: modelType={}, k={}", qry.getModelType(), qry.getK());
//        return knowledgeService.searchByVector(qry);
//    }

    /**
     * 文本检索
     */
    @PostMapping("/search/text")
    public MultiResponse<KnowledgeSearchResult> searchByText(@RequestBody KnowledgeSearchQry qry) {
        log.info("文本检索请求: keyword={}, k={}", qry.getKeyword(), qry.getK());
        return knowledgeService.searchByText(qry);
    }

    /**
     * 创建Redisearch索引
     */
    @PostMapping("/index")
    public Response createIndex(@RequestBody CreateIndexCmd cmd) {
        log.info("创建Redisearch索引请求: indexName={}, fields={}", cmd.getIndexName(), cmd.getSchema().size());
        return knowledgeService.createIndex(cmd);
    }

    /**
     * 查询索引信息
     */
    @GetMapping("/index")
    public SingleResponse<IndexInfoDTO> getIndexInfo(GetIndexInfoQry qry) {
        return knowledgeService.getIndexInfo(qry);
    }

    /**
     * 删除索引
     */
    @DeleteMapping("/index")
    public Response deleteIndex(@RequestBody DeleteIndexCmd cmd) {
        return knowledgeService.deleteIndex(cmd);
    }

    /**
     * 获取所有RediSearch索引列表
     */
    @GetMapping("/indexes")
    public MultiResponse<String> listIndexes() {
        log.info("获取所有RediSearch索引列表请求");
        return knowledgeService.listIndexes();
    }
} 