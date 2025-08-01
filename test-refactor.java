import com.leyue.smartcs.knowledge.parser.impl.LangChain4jDocumentParserAdapter;
import com.leyue.smartcs.knowledge.chunking.LangChain4jChunkingStrategy;
import com.leyue.smartcs.knowledge.model.ChunkingStrategyConfig;

// 简单的测试文件验证编译
public class TestRefactor {
    public static void main(String[] args) {
        System.out.println("重构测试");
        
        // 测试适配器实例化
        LangChain4jDocumentParserAdapter adapter = new LangChain4jDocumentParserAdapter();
        System.out.println("LangChain4j适配器创建成功");
        System.out.println("支持的类型: " + adapter.getSupportedTypes());
        
        // 测试分块策略实例化
        LangChain4jChunkingStrategy strategy = new LangChain4jChunkingStrategy();
        System.out.println("LangChain4j分块策略创建成功");
        
        // 测试配置对象
        ChunkingStrategyConfig config = ChunkingStrategyConfig.builder()
            .chunkSize(1000)
            .overlapSize(200)
            .build();
        System.out.println("分块配置创建成功: " + config);
        System.out.println("配置有效性: " + config.isValid());
    }
}