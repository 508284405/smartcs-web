import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeGeneralChunkCmd;
import com.leyue.smartcs.knowledge.executor.command.KnowledgeGeneralChunkCmdExe;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
import com.leyue.smartcs.knowledge.util.TextPreprocessor;
import dev.langchain4j.data.document.Document;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.Resource;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

public class KnowledgeGeneralChunkCmdExeTest {

    /**
     * 简单的解析器，仅返回读取到的文本内容。
     */
    static class PlainParser implements DocumentParser {
        @Override
        public List<Document> parse(Resource resource, String fileName) throws java.io.IOException {
            return List.of(Document.from("sample text"));
        }

        @Override
        public String[] getSupportedTypes() { return new String[]{"txt"}; }

        @Override
        public boolean supports(String extension) { return true; }
    }

    /** 简单的工厂，始终返回 {@link PlainParser}. */
    static class PlainFactory extends DocumentParserFactory {
        public PlainFactory() { super(List.of(new PlainParser())); initializeParsers(); }
    }

    static class DummyModelBeanService extends ModelBeanManagerService {
        DummyModelBeanService() { super(null); }
        @Override
        public Object getFirstModelBean() { return null; }
    }

    @Test
    public void testExecute() {
        KnowledgeGeneralChunkCmd cmd = new KnowledgeGeneralChunkCmd();
        try {
            Path tmp = Files.createTempFile("sample", ".txt");
            Files.writeString(tmp, "sample text data");
            cmd.setFileUrl(tmp.toUri().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        cmd.setChunkSize(50);
        cmd.setOverlapSize(0);

        KnowledgeGeneralChunkCmdExe exe = new KnowledgeGeneralChunkCmdExe(
                new TextPreprocessor(),
                new DummyModelBeanService(),
                new PlainFactory());

        List<ChunkDTO> result = exe.execute(cmd).getData();
        Assert.assertFalse(result.isEmpty());
    }
}

