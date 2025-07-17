package com.leyue.smartcs.rag.vdb.factory;

import com.leyue.smartcs.domain.rag.model.Dataset;
import com.leyue.smartcs.rag.vdb.KeywordStore;
import com.leyue.smartcs.rag.vdb.memory.InMemoryKeywordStore;
import org.springframework.stereotype.Component;

/**
 * 关键词存储工厂
 */
@Component
public class KeywordStoreFactory {
    
    public KeywordStore get(Dataset dataset) {
        // 根据 dataset 配置返回不同实现
        // 目前仅支持内存
        return new InMemoryKeywordStore();
    }
} 