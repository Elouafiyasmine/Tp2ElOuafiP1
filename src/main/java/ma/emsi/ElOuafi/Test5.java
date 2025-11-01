package ma.emsi.ElOuafi;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;

import dev.langchain4j.service.AiServices;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class Test5 {

    interface Assistant {
        String respond(String input);
    }

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ Missing environment variable: GEMINI_KEY");
            return;
        }

        // Initialize Chat Model (LLM)
        ChatModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.0-flash")
                .temperature(0.3)
                .responseFormat(ResponseFormat.TEXT)
                .timeout(Duration.ofSeconds(60))
                .build();

        // Initialize Embedding Model
        EmbeddingModel embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-004")
                .build();

        // Load PDF document
        String pdfName = "support.pdf";
        Document source = FileSystemDocumentLoader.loadDocument(pdfName);

        // Create an in-memory vector index
        EmbeddingStore<TextSegment> vectorStore = new InMemoryEmbeddingStore<>();

        // Define the document splitting strategy
        DocumentSplitter chunker = DocumentSplitters.recursive(750, 100);

        // Convert text chunks into embeddings and index them
        EmbeddingStoreIngestor pipeline = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .documentSplitter(chunker)
                .embeddingStore(vectorStore)
                .build();
        pipeline.ingest(source);

        // Build a retriever to get the top relevant segments
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(vectorStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();

        // Create an AI assistant with short-term memory and retrieval
        Assistant aiAssistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(retriever)
                .build();

        // Run interactive chat loop
        startChat(aiAssistant, retriever);
    }

    /** Starts a conversation loop in console mode. */
    private static void startChat(Assistant assistant, ContentRetriever retriever) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("==================================================");
                System.out.print("Ask something (or type 'exit' to quit): ");
                String userInput = scanner.nextLine().trim();
                System.out.println("==================================================");

                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("👋 Session ended.");
                    break;
                }
                if (userInput.isEmpty()) continue;

                // Try retrieving relevant document snippets
                try {
                    List<Content> results = retriever.retrieve(Query.from(userInput));
                    System.out.println("\n--- Retrieved Context ---");
                    for (Content content : results) {
                        TextSegment seg = content.textSegment();
                        System.out.println("* META: " + seg.metadata());
                        System.out.println("  TEXT: " + truncate(seg.text(), 200));
                    }
                    System.out.println("-------------------------\n");
                } catch (Exception ex) {
                    System.out.println("(Info) Context retrieval failed: " + ex.getMessage());
                }

                // Generate assistant response
                String output = assistant.respond(userInput);
                System.out.println("🤖 Assistant: " + output);
            }
        }
    }

    /** Utility: shorten long text for display */
    private static String truncate(String text, int max) {
        return (text == null || text.length() <= max)
                ? text
                : text.substring(0, max) + " [...]";
    }
}
