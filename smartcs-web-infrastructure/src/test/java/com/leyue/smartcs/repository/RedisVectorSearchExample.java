package com.leyue.smartcs.repository;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.*;
import redis.clients.jedis.search.schemafields.SchemaField;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.TextField;
import redis.clients.jedis.search.schemafields.VectorField;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisVectorSearchExample {

    // 将 long[] 转换为 float[]，然后编码为小端字节数组
    public static byte[] longsToFloatsByteString(long[] input) {
        float[] floats = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            floats[i] = input[i];
        }

        byte[] bytes = new byte[Float.BYTES * floats.length];
        ByteBuffer
                .wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asFloatBuffer()
                .put(floats);
        return bytes;
    }

    public static void main(String[] args) throws Exception {
        HostAndPort hostAndPort = new HostAndPort("14.103.133.201", 6379);
        // 初始化 Jedis 客户端
        DefaultJedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                .database(0)
                .password("521qq508284405")
                .timeoutMillis(3000).build();
        UnifiedJedis jedis = new UnifiedJedis(hostAndPort,jedisClientConfig);

        // 删除已存在的索引（如果有）
        try {
            jedis.ftDropIndex("vector_idx");
        } catch (JedisDataException e) {
            // 忽略异常
        }

        SchemaField[] schema = {
                TextField.of("content"),
                TagField.of("genre"),
                VectorField.builder()
                        .fieldName("embedding")
                        .algorithm(VectorAlgorithm.HNSW)
                        .attributes(
                                Map.of(
                                        "TYPE", "FLOAT32",
                                        "DIM", 768,
                                        "DISTANCE_METRIC", "L2"
                                )
                        )
                        .build()
        };

        jedis.ftCreate("vector_idx",
                FTCreateParams.createParams()
                        .addPrefix("doc:")
                        .on(IndexDataType.HASH),
                schema
        );


        // 初始化分词器
        HuggingFaceTokenizer sentenceTokenizer = HuggingFaceTokenizer.newInstance(
                "sentence-transformers/all-mpnet-base-v2",
                Map.of("maxLength", "768", "modelMaxLength", "768")
        );

        String sentence1 = "That is a very happy person";
        jedis.hset("doc:1", Map.of("content", sentence1, "genre", "persons"));
        jedis.hset(
                "doc:1".getBytes(),
                "embedding".getBytes(),
                longsToFloatsByteString(sentenceTokenizer.encode(sentence1).getIds())
        );

        String sentence2 = "That is a happy dog";
        jedis.hset("doc:2", Map.of("content", sentence2, "genre", "pets"));
        jedis.hset(
                "doc:2".getBytes(),
                "embedding".getBytes(),
                longsToFloatsByteString(sentenceTokenizer.encode(sentence2).getIds())
        );

        String sentence3 = "Today is a sunny day";
        jedis.hset("doc:3", Map.of("content", sentence3, "genre", "weather"));
        jedis.hset(
                "doc:3".getBytes(),
                "embedding".getBytes(),
                longsToFloatsByteString(sentenceTokenizer.encode(sentence3).getIds())
        );

        // 搜索
        String sentence = "That is a happy person";

        int K = 3;
        Query q = new Query("*=>[KNN $K @embedding $BLOB AS distance]")
                .returnFields("content", "distance","score")
                .addParam("K", K)
                .addParam("BLOB", longsToFloatsByteString(sentenceTokenizer.encode(sentence).getIds()))
                .setSortBy("distance", true)
                .dialect(2);

        List<Document> docs = jedis.ftSearch("vector_idx", q).getDocuments();

        for (Document doc: docs) {
            System.out.println(
                    String.format(
                            "ID: %s, Distance: %s, Content: %s, score: %s",
                            doc.getId(),
                            doc.get("distance"),
                            doc.get("content"),
                            doc.getScore()
                    )
            );
        }
        jedis.close();
    }
}