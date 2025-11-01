package ma.emsi.ElOuafi;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;

import dev.langchain4j.service.AiServices;

import java.time.Duration;

public class Test4 {

    interface Assistant { String chat(String userMessage); }

    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("⚠️ GEMINI_KEY manquante"); return;
        }

        ChatModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.0-flash")
                .temperature(0.2)
                .responseFormat(ResponseFormat.TEXT)
                .timeout(Duration.ofSeconds(60))
                .build();

        EmbeddingModel embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-004")
                .build();


        Document document = FileSystemDocumentLoader.loadDocument("infos.txt");

        // In-memory vector store
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        DocumentSplitter splitter = DocumentSplitters.recursive(800, 100);

        // Ingestion with splitter + model
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .documentSplitter(splitter)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

        // Retriever: requires embeddingStore + embeddingModel
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(retriever)
                .build();

        String question = "Pierre appelle son chat. Qu'est-ce qu'il pourrait dire ?";

        System.out.println("Réponse du LLM : " + assistant.chat(question));
    }
}

