package com.leyue.smartcs.knowledge.gateway.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.knowledge.IndexInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.schemafields.SchemaField;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于Jedis的搜索网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class JedisSearchGatewayImpl implements SearchGateway {

    private final UnifiedJedis unifiedJedis;
    @Override
    public boolean indexDocument(String index, Long id, Object source) {
        try {
            String documentKey = index + ":" + id;
            Map<String, String> documentMap = JSONObject.parseObject(JSONObject.toJSONString(source), new TypeReference<Map<String, String>>() {
            });

            // 使用Jedis存储为哈希 (RediSearch会根据索引配置自动索引)
            unifiedJedis.hset(documentKey, documentMap);
            log.info("成功索引文档: index={}, id={}", index, id);
            return true;
        } catch (Exception e) {
            log.error("索引文档失败: index={}, id={}", index, id, e);
            return false;
        }
    }

    @Override
    public boolean deleteDocument(String index, Long id) {
        try {
            // 使用Jedis删除哈希
            String documentKey = index + ":" + id;
            unifiedJedis.del(documentKey);

            log.info("成功删除文档: index={}, id={}", index, id);
            return true;
        } catch (Exception e) {
            log.error("删除文档失败: index={}, id={}", index, id, e);
            return false;
        }
    }

    /**
     * 转义RediSearch查询中的特殊字符
     *
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    private String escapeQueryChars(String input) {
        if (input == null) {
            return "";
        }
        // 转义RediSearch查询中的特殊字符
        return input.replaceAll("([,\\\\/\\!\\{\\}\\[\\]\\(\\)\\\"\\^~\\*:])", "\\\\$1");
    }

    // ========== 索引管理相关方法 ==========

    @Override
    public void createIndex(String index, Object... fieldIndex) {
        try {
            log.info("尝试创建全文索引: {}", index);

            // 使用 Jedis FT.CREATE 命令创建索引
            // 注意：这里假设 fieldIndex 数组包含的是用于构建 FT.CREATE 命令的参数
            // 实际实现可能需要根据具体的参数类型进行调整
            unifiedJedis.ftCreate(index, (SchemaField[]) fieldIndex);

            log.info("成功创建全文索引: {}", index);
        } catch (Exception e) {
            // 检查是否是索引已存在的异常，如果是则视为成功
            if (e.getMessage() != null && e.getMessage().contains("Index already exists")) {
                log.warn("全文索引已存在，无需重复创建: {}", index);
            } else {
                log.error("创建全文索引失败: {}", index, e);
            }
        }
    }

    @Override
    public IndexInfoDTO getIndexInfo(String indexName) {
        try {
            // 使用 Jedis FT.INFO 命令获取索引信息
            Map<String, Object> indexInfo = unifiedJedis.ftInfo(indexName);
            if (indexInfo == null || indexInfo.isEmpty()) {
                log.error("索引不存在: {}", indexName);
                return null;
            }
            return JSONObject.parseObject(JSONObject.toJSONString(indexInfo), IndexInfoDTO.class);
        } catch (Exception e) {
            log.error("获取索引信息失败: {}", indexName, e);
            return null;
        }
    }

    @Override
    public boolean deleteIndex(String indexName) {
        try {
            // 使用 Jedis FT.DROPINDEX 命令删除索引
            unifiedJedis.ftDropIndex(indexName);
            log.info("成功删除索引: {}", indexName);
            return true;
        } catch (Exception e) {
            log.error("删除索引失败: {}", indexName, e);
            return false;
        }
    }

    @Override
    public Set<String> listIndexes() {
        try {
            // 使用 Jedis FT._LIST 命令获取索引列表
            return unifiedJedis.ftList();
        } catch (Exception e) {
            log.error("获取索引列表失败", e);
            return Set.of();
        }
    }
} 