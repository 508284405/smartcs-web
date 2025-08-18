package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.knowledge.DocumentProcessCmd;
import com.leyue.smartcs.dto.knowledge.DocumentProcessResultDTO;
import com.leyue.smartcs.dto.knowledge.UrlDocumentImportCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * 通过URL导入文档执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UrlDocumentImportCmdExe {

  private final DocumentProcessCmdExe documentProcessCmdExe;

  public SingleResponse<DocumentProcessResultDTO> execute(UrlDocumentImportCmd cmd) {
    validateUrl(cmd.getUrl());

    DocumentProcessCmd processCmd = new DocumentProcessCmd();
    processCmd.setKnowledgeBaseId(cmd.getKnowledgeBaseId());
    if (cmd.getModelId() == null) {
      throw new BizException("MODEL_ID_REQUIRED", "模型ID不能为空");
    }
    processCmd.setModelId(cmd.getModelId());

    processCmd.setTitle(resolveTitle(cmd));
    processCmd.setFileUrl(cmd.getUrl());
    processCmd.setFileType(cmd.getFileType());
    processCmd.setFileSize(cmd.getFileSize());
    processCmd.setOriginalFileName(cmd.getOriginalFileName());
    processCmd.setSource("import");

    // 分段与检索设置
    processCmd.setSegmentMode(cmd.getSegmentMode() == null || cmd.getSegmentMode().isEmpty() ? "general" : cmd.getSegmentMode());
    processCmd.setSegmentSettings(cmd.getSegmentSettings());
    processCmd.setParentChildSettings(cmd.getParentChildSettings());
    if (cmd.getIndexMethod() != null) {
      processCmd.setIndexMethod(cmd.getIndexMethod());
    }
    if (cmd.getRetrievalSettings() != null) {
      processCmd.setRetrievalSettings(cmd.getRetrievalSettings());
    }

    log.info("通过URL导入文档: url={}, kbId={}, title={}",
        cmd.getUrl(), cmd.getKnowledgeBaseId(), processCmd.getTitle());
    return documentProcessCmdExe.execute(processCmd);
  }

  private void validateUrl(String url) {
    if (url == null || url.isEmpty()) {
      throw new BizException("URL_INVALID", "URL不能为空");
    }
    URI uri = URI.create(url);
    String scheme = uri.getScheme();
    if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
      throw new BizException("URL_INVALID", "仅支持http/https协议");
    }
    String host = uri.getHost();
    if (host == null) {
      throw new BizException("URL_INVALID", "无效的URL主机");
    }
    if (isPrivateOrLoopback(host)) {
      throw new BizException("URL_FORBIDDEN", "禁止访问内网或回环地址");
    }
  }

  private boolean isPrivateOrLoopback(String host) {
    try {
      InetAddress address = InetAddress.getByName(host);
      return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress();
    } catch (UnknownHostException e) {
      return false;
    }
  }

  private String resolveTitle(UrlDocumentImportCmd cmd) {
    if (cmd.getTitle() != null && !cmd.getTitle().isEmpty()) {
      return cmd.getTitle();
    }
    String name = cmd.getOriginalFileName();
    if (name == null || name.isEmpty()) {
      String path = URI.create(cmd.getUrl()).getPath();
      int idx = path.lastIndexOf('/');
      name = idx >= 0 ? path.substring(idx + 1) : path;
    }
    if (name == null || name.isEmpty()) {
      return "导入文档";
    }
    int dot = name.lastIndexOf('.');
    return dot > 0 ? name.substring(0, dot) : name;
  }
}


