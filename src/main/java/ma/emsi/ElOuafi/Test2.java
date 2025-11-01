package ma.emsi.ElOuafi;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.time.Duration;
import java.util.Map;
public class Test2 {
    public static void main(String[] args) {
        PromptTemplate template = PromptTemplate.from(
                """
                        tu es un traducteur qui est capable de traduire tout les langues,vous devez traduire n'importe que l'utilisateur ecrit : {{text}} en langue choisi : {{langue}}
                        """
        );
        Prompt prompt = template.apply(Map.of("text","Hi!!! who are you ?", "langue", "français"));
        String cle = System.getenv("GEMINI_KEY");
        ChatModel modele =
                GoogleAiGeminiChatModel.builder()
                        .apiKey(cle)
                        .modelName("gemini-2.5-flash")
                        .temperature(0.8)
                        .timeout(Duration.ofSeconds(60))
                        .responseFormat(ResponseFormat.JSON)
                        .build();
        String reponse = modele.chat(prompt.text());
        System.out.println(reponse);
    }
}