import com.hua.HuacaiEasyApplication;
import com.hua.ai.domain.Report;
import com.hua.ai.service.DialogueService;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = HuacaiEasyApplication.class)
public class ChatTest {

    @Autowired
    private DialogueService dialogueService;

    @Test
    void chat() {
        String msg = dialogueService.chat("我想在java和python中选择java");
        System.out.println(msg);
        String newMsg = dialogueService.chat("我之前在java和python中选择了哪个");
        System.out.println(newMsg);
    }

    @Test
    void getReport() {
        String msg = "我是一个学习开发知识的学生,我叫小明, 我想知道花菜编程网有什么";
        Report report = dialogueService.getReport(msg);
        System.out.println(report);
    }

    @Test
    void getChatRag() {
        Result<String> res = dialogueService.getChatRag("花菜编程网的用户群体分析是什么");
        System.out.println("RAG检索到的文档" + res.sources()); //RAG检索到的文档
        System.out.println("=============================================================="); //RAG检索到的文档
        System.out.println("AI的回答" + res.content()); //AI的回答
    }

}
