package com.leyue.smartcs.knowledge.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * OSS文件下载工具类
 * 负责从OSS服务下载文件到本地临时目录
 */
@Component
@Slf4j
public class OssFileDownloader {
    
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    
    /**
     * 下载OSS文件到本地临时目录
     * @param ossUrl OSS文件URL
     * @return 下载到本地的文件
     * @throws IOException 下载过程中可能出现的IO异常
     */
    public File download(String ossUrl) throws IOException {
        log.info("开始下载OSS文件: {}", ossUrl);
        
        // 生成唯一的临时文件名
        String tempFileName = UUID.randomUUID().toString();
        String fileExt = getFileExtension(ossUrl);
        if (!fileExt.isEmpty()) {
            tempFileName += "." + fileExt;
        }
        
        // 创建临时文件
        Path tempFilePath = Path.of(TEMP_DIR, tempFileName);
        File tempFile = tempFilePath.toFile();
        
        // 建立HTTP连接下载文件
        URL url = new URL(ossUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }
        
        log.info("OSS文件下载完成，保存到临时文件: {}", tempFile.getAbsolutePath());
        
        // 注册JVM退出时删除临时文件
        tempFile.deleteOnExit();
        
        return tempFile;
    }
    
    /**
     * 从URL中提取文件扩展名
     * @param url 文件URL
     * @return 文件扩展名（不含点号）
     */
    private String getFileExtension(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        // 去除可能的URL参数
        String urlWithoutParams = url.split("\\?")[0];
        
        int lastDotPos = urlWithoutParams.lastIndexOf('.');
        if (lastDotPos == -1 || lastDotPos == urlWithoutParams.length() - 1) {
            return "";
        }
        
        return urlWithoutParams.substring(lastDotPos + 1).toLowerCase();
    }
} 