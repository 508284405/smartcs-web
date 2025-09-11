package com.leyue.smartcs.knowledge.service;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ContentService;
import com.leyue.smartcs.dto.knowledge.ContentDTO;
import com.leyue.smartcs.dto.knowledge.ContentCreateCmd;
import com.leyue.smartcs.dto.knowledge.ContentUpdateCmd;
import com.leyue.smartcs.dto.knowledge.ContentStatusUpdateCmd;
import com.leyue.smartcs.dto.knowledge.ContentListQry;
import com.leyue.smartcs.dto.knowledge.DocumentSearchRequest;
import com.leyue.smartcs.dto.knowledge.DocumentSearchResultDTO;
import com.leyue.smartcs.dto.knowledge.DocumentProcessCmd;
import com.leyue.smartcs.dto.knowledge.DocumentProcessResultDTO;
import com.leyue.smartcs.dto.knowledge.UrlDocumentImportCmd;
import com.leyue.smartcs.knowledge.executor.command.ContentCreateCmdExe;
import com.leyue.smartcs.knowledge.executor.command.ContentUpdateCmdExe;
import com.leyue.smartcs.knowledge.executor.command.ContentStatusUpdateCmdExe;
import com.leyue.smartcs.knowledge.executor.command.ContentDeleteCmdExe;
import com.leyue.smartcs.knowledge.executor.query.ContentGetQryExe;
import com.leyue.smartcs.knowledge.executor.query.ContentListQryExe;
import com.leyue.smartcs.knowledge.executor.command.ContentParsingCmdExe;
import com.leyue.smartcs.knowledge.executor.query.DocumentVectorSearchQryExe;
import com.leyue.smartcs.knowledge.executor.command.DocumentProcessCmdExe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 内容管理服务实现
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentCreateCmdExe contentCreateCmdExe;
    
    @Autowired
    private ContentUpdateCmdExe contentUpdateCmdExe;
    
    @Autowired
    private ContentStatusUpdateCmdExe contentStatusUpdateCmdExe;
    
    @Autowired
    private ContentDeleteCmdExe contentDeleteCmdExe;
    
    @Autowired
    private ContentGetQryExe contentGetQryExe;
    
    @Autowired
    private ContentListQryExe contentListQryExe;
    
    @Autowired
    private ContentParsingCmdExe contentParsingCmdExe;

    @Autowired
    private DocumentVectorSearchQryExe documentVectorSearchQryExe;
    
    @Autowired
    private DocumentProcessCmdExe documentProcessCmdExe;

    @Autowired
    private com.leyue.smartcs.knowledge.executor.command.UrlDocumentImportCmdExe urlDocumentImportCmdExe;
    

    @Override
    public SingleResponse<ContentDTO> createContent(ContentCreateCmd cmd) {
        return contentCreateCmdExe.execute(cmd);
    }

    @Override
    public Response updateContent(ContentUpdateCmd cmd) {
        return contentUpdateCmdExe.execute(cmd);
    }

    @Override
    public Response updateContentStatus(ContentStatusUpdateCmd cmd) {
        return contentStatusUpdateCmdExe.execute(cmd);
    }

    @Override
    public SingleResponse<ContentDTO> getContent(Long id) {
        return contentGetQryExe.execute(id);
    }

    @Override
    public Response deleteContent(Long id) {
        return contentDeleteCmdExe.execute(id);
    }

    @Override
    public PageResponse<ContentDTO> listContents(ContentListQry qry) {
        return contentListQryExe.execute(qry);
    }

    @Override
    public Response triggerContentParsing(Long contentId) {
        return contentParsingCmdExe.execute(contentId);
    }

    @Override
    public MultiResponse<DocumentSearchResultDTO> vectorSearch(DocumentSearchRequest request) {
        return documentVectorSearchQryExe.execute(request);
    }
    
    @Override
    public SingleResponse<DocumentProcessResultDTO> processDocument(DocumentProcessCmd cmd) {
        return documentProcessCmdExe.execute(cmd);
    }

    @Override
    public SingleResponse<DocumentProcessResultDTO> importByUrl(UrlDocumentImportCmd cmd) {
        return urlDocumentImportCmdExe.execute(cmd);
    }
}
