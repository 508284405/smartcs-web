import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeParentChildChunkCmd;
import com.leyue.smartcs.knowledge.executor.command.KnowledgeParentChildChunkCmdExe;
import com.leyue.smartcs.knowledge.util.TextPreprocessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class KnowledgeParentChildChunkCmdExeTest {

    @Test
    public void testExecute() {
        KnowledgeParentChildChunkCmd cmd = new KnowledgeParentChildChunkCmd();
        try {
            Path tmp = Files.createTempFile("sample", ".txt");
            Files.writeString(tmp, "sample text data");
            cmd.setFileUrl(tmp.toUri().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        cmd.setParentChunkSize(100);
        cmd.setChildChunkSize(50);
        cmd.setRemoveAllUrls(false);

        KnowledgeParentChildChunkCmdExe exe = new KnowledgeParentChildChunkCmdExe(new TextPreprocessor());
        List<ChunkDTO> chunks = exe.execute(cmd).getData();
        Assert.assertFalse(chunks.isEmpty());
    }
}

