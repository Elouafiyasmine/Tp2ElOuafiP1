package ma.emsi.ElOuafi;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import dev.langchain4j.service.AiServices;

import ma.emsi.ElOuafi.tools.meteo.MeteoTool;

import java.time.Duration;
import java.util.Scanner;

public class Test6 {

    /** Interface handled by the AI assistant */
    interface WeatherAssistant {
        String reply(String question);
    }

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("⚠️ Missing environment variable: GEMINI_KEY");
            return;
        }

        // Configure Gemini chat model
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.0-flash")
                .temperature(0.3)
                .responseFormat(ResponseFormat.TEXT)
                .timeout(Duration.ofSeconds(90))
                .logRequestsAndResponses(true)
                .build();

        // Build the weather assistant with a connected tool
        WeatherAssistant assistant = AiServices.builder(WeatherAssistant.class)
                .chatModel(model)
                .tools(new MeteoTool())
                .build();

        // Choose test mode: automated or interactive
        // runAutomatic(assistant);
        runInteractive(assistant);
    }

    /** Automatic test mode with fixed queries */
    private static void runAutomatic(WeatherAssistant assistant) {
        ask(assistant, "What's the weather like in Paris?");
        ask(assistant, "I'm planning to go to Casablanca today. Should I bring an umbrella?");
        ask(assistant, "Can you tell me the weather in Zqxyz-ville?");
        ask(assistant, "Explain the difference between compilation and interpretation.");
    }

    /** Interactive console mode */
    private static void runInteractive(WeatherAssistant assistant) {
        System.out.println(">>> Interactive mode: ask any question (type 'exit' to quit).");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nYour question > ");
                String question = scanner.nextLine();

                if (question == null || question.isBlank()) continue;
                if ("exit".equalsIgnoreCase(question.trim())) {
                    System.out.println("👋 Conversation ended.");
                    break;
                }

                String response = assistant.reply(question);
                System.out.println("Assistant: " + response);
            }
        }
    }

    /** Helper method used in automatic test mode */
    private static void ask(WeatherAssistant assistant, String question) {
        System.out.println("==================================================");
        System.out.println("Question: " + question);
        String response = assistant.reply(question);
        System.out.println("Assistant: " + response);
    }
}

