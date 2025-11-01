package ma.emsi.ElOuafi;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.CosineSimilarity;

import java.time.Duration;

public class Test3 {
    public static void main(String[] args) {
        String cle = System.getenv("GEMINI_KEY");
        if (cle == null || cle.isBlank()) {
            System.err.println("⚠️ Variable d'environnement GEMINI_KEY introuvable.");
            System.exit(1);
        }

        EmbeddingModel modele = GoogleAiEmbeddingModel.builder()
                .apiKey(cle)
                // Choisis l’un des deux modèles ci-dessous :
                .modelName("text-embedding-004")               // Recommandé, multilingue
                // .modelName("text-multilingual-embedding-002") // Option multilingue dédiée
                .taskType(GoogleAiEmbeddingModel.TaskType.SEMANTIC_SIMILARITY)
                // .outputDimensionality(768) // Optionnel : 256, 512 ou 768. Omettre pour la valeur par défaut.
                .timeout(Duration.ofSeconds(60))
                .build();

        String phrase1 = "Bonjour, qui a gagné le match hier?";
        String phrase2 = "Salut! oui c'est le maroc !!!!!";

        try {
            Response<Embedding> rep1 = modele.embed(phrase1);
            Response<Embedding> rep2 = modele.embed(phrase2);

            Embedding emb1 = rep1.content();
            Embedding emb2 = rep2.content();

            double similarite = CosineSimilarity.between(emb1, emb2);
            System.out.println("Similarité cosinus : " + similarite);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à l'API Gemini: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
