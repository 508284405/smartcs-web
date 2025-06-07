package com.leyue.smartcs.web.knowledge;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ChunkService;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.ChunkCreateCmd;
import com.leyue.smartcs.dto.knowledge.ChunkListQry;
import com.leyue.smartcs.dto.knowledge.ChunkUpdateCmd;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 运营端切片管理Controller
 */
@RestController
@RequestMapping("/api/admin/chunk")
@RequiredArgsConstructor
public class AdminChunkController {
    
    private final ChunkService chunkService;
    
    /**
     * 创建切片
     */
    @PostMapping
    public SingleResponse<ChunkDTO> createChunk(@RequestBody @Valid ChunkCreateCmd cmd) {
        return chunkService.createChunk(cmd);
    }
    
    /**
     * 更新切片
     */
    @PutMapping
    public Response updateChunk(@RequestBody @Valid ChunkUpdateCmd cmd) {
        return chunkService.updateChunk(cmd);
    }
    
    /**
     * 查询切片详情
     */
    @GetMapping("/{id}")
    public SingleResponse<ChunkDTO> getChunk(@PathVariable Long id) {
        return chunkService.getChunk(id);
    }
    
    /**
     * 删除切片
     */
    @DeleteMapping("/{id}")
    public Response deleteChunk(@PathVariable Long id) {
        return chunkService.deleteChunk(id);
    }
    
    /**
     * 查询切片列表
     */
    @GetMapping
    public PageResponse<ChunkDTO> listChunks(@Valid ChunkListQry qry) {
        return chunkService.listChunks(qry);
    }
    
    /**
     * 切片向量化存储
     */
    @PostMapping("/{id}/vectorize")
    public Response vectorizeChunk(@PathVariable Long id) {
        return chunkService.vectorizeChunk(id);
    }
} 