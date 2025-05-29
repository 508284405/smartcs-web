package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.knowledge.ContentDTO;
import com.leyue.smartcs.dto.knowledge.ContentCreateCmd;
import com.leyue.smartcs.dto.knowledge.ContentUpdateCmd;
import com.leyue.smartcs.dto.knowledge.ContentListQry;

/**
 * 内容管理服务接口
 */
public interface ContentService {
    
    /**
     * 创建内容
     * @param cmd 创建命令
     * @return 创建的内容
     */
    SingleResponse<ContentDTO> createContent(ContentCreateCmd cmd);
    
    /**
     * 更新内容
     * @param cmd 更新命令
     * @return 更新结果
     */
    Response updateContent(ContentUpdateCmd cmd);
    
    /**
     * 根据ID查询内容
     * @param id 内容ID
     * @return 内容信息
     */
    SingleResponse<ContentDTO> getContent(Long id);
    
    /**
     * 删除内容
     * @param id 内容ID
     * @return 删除结果
     */
    Response deleteContent(Long id);
    
    /**
     * 查询内容列表
     * @param qry 查询条件
     * @return 内容列表
     */
    PageResponse<ContentDTO> listContents(ContentListQry qry);
    
    /**
     * 触发内容解析
     * @param contentId 内容ID
     * @return 操作结果
     */
    Response triggerContentParsing(Long contentId);
    
    /**
     * 触发内容向量化
     * @param contentId 内容ID
     * @return 操作结果
     */
    Response triggerContentVectorization(Long contentId, String strategyName);
} 