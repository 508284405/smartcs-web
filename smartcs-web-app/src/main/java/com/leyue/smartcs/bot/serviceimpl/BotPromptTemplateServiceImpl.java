package com.leyue.smartcs.bot.serviceimpl;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.bot.api.BotPromptTemplateService;
import com.leyue.smartcs.bot.convertor.PromptTemplateConvertor;
import com.leyue.smartcs.bot.dto.BotPromptTemplateCreateCmd;
import com.leyue.smartcs.bot.dto.BotPromptTemplateDTO;
import com.leyue.smartcs.bot.dto.BotPromptTemplatePageQry;
import com.leyue.smartcs.bot.dto.BotPromptTemplateUpdateCmd;
import com.leyue.smartcs.domain.bot.PromptTemplate;
import com.leyue.smartcs.domain.bot.gateway.PromptTemplateGateway;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bot Prompt模板服务实现
 */
@Service
public class BotPromptTemplateServiceImpl implements BotPromptTemplateService {
    
    @Resource
    private PromptTemplateGateway promptTemplateGateway;
    @Resource
    private PromptTemplateConvertor promptTemplateConvertor;
    
    @Override
    public SingleResponse<PageResponse<BotPromptTemplateDTO>> pageQuery(BotPromptTemplatePageQry qry) {
        PageResponse<PromptTemplate> domainPage = promptTemplateGateway.pageQuery(qry);
        
        List<BotPromptTemplateDTO> dtoList = domainPage.getData().stream()
                .map(promptTemplateConvertor::toDTO)
                .collect(Collectors.toList());
        
        PageResponse<BotPromptTemplateDTO> result = PageResponse.of(
                dtoList, 
                domainPage.getTotalCount(), 
                domainPage.getPageSize(), 
                domainPage.getPageIndex());
        
        return SingleResponse.of(result);
    }
    
    @Override
    public SingleResponse<Long> create(BotPromptTemplateCreateCmd cmd) {
        // 检查模板标识唯一性
        Optional<PromptTemplate> existingTemplate = promptTemplateGateway.findByTemplateKey(cmd.getTemplateKey());
        if (existingTemplate.isPresent()) {
            throw new BizException("模板标识已存在: " + cmd.getTemplateKey());
        }
        
        // 创建新模板
        PromptTemplate template = new PromptTemplate();
        template.setTemplateKey(cmd.getTemplateKey());
        template.setTemplateContent(cmd.getTemplateContent());
        template.setCreatedAt(System.currentTimeMillis());
        template.setUpdatedAt(System.currentTimeMillis());
        template.setIsDeleted(0);
        
        PromptTemplate savedTemplate = promptTemplateGateway.save(template);
        return SingleResponse.of(savedTemplate.getId());
    }
    
    @Override
    public Response update(BotPromptTemplateUpdateCmd cmd) {
        // 查询模板是否存在
        Optional<PromptTemplate> templateOpt = promptTemplateGateway.findById(cmd.getId());
        if (!templateOpt.isPresent()) {
            throw new BizException("模板不存在: " + cmd.getId());
        }
        
        PromptTemplate template = templateOpt.get();
        // 仅更新模板内容
        template.setTemplateContent(cmd.getTemplateContent());
        template.setUpdatedAt(System.currentTimeMillis());
        
        promptTemplateGateway.save(template);
        return Response.buildSuccess();
    }
    
    @Override
    public Response delete(Long id) {
        // 查询模板是否存在
        Optional<PromptTemplate> templateOpt = promptTemplateGateway.findById(id);
        if (templateOpt.isEmpty()) {
            throw new BizException("模板不存在: " + id);
        }
        
        PromptTemplate template = templateOpt.get();
        // 检查模板是否被使用
        boolean inUse = promptTemplateGateway.isTemplateInUse(template.getTemplateKey());
        if (inUse) {
            throw new BizException("模板正在被机器人使用，无法删除");
        }

        promptTemplateGateway.delete(id);
        return Response.buildSuccess();
    }
    
    @Override
    public SingleResponse<BotPromptTemplateDTO> getById(Long id) {
        Optional<PromptTemplate> templateOpt = promptTemplateGateway.findById(id);
        if (templateOpt.isEmpty()) {
            throw new BizException("模板不存在: " + id);
        }
        
        PromptTemplate template = templateOpt.get();
        BotPromptTemplateDTO dto = promptTemplateConvertor.toDTO(template);
        return SingleResponse.of(dto);
    }
    
    @Override
    public MultiResponse<BotPromptTemplateDTO> listTemplates(String templateKey, String context) {
        List<PromptTemplate> templates = promptTemplateGateway.findByCriteria(templateKey, context);
        
        List<BotPromptTemplateDTO> dtoList = templates.stream()
                .map(promptTemplateConvertor::toDTO)
                .collect(Collectors.toList());
        
        return MultiResponse.of(dtoList);
    }
} 