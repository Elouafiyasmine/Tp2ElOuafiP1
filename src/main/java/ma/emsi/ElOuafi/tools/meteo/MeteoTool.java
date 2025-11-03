package ma.emsi.ElOuafi.tools.meteo;

import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.Scanner;


public class MeteoTool {

    @Tool("Fetches the current weather for a specific city. Use this tool when the user asks about the weather, mentions rain, or wonders if they should take an umbrella.")
    public String getWeather(String city) {
        try {
            // Build the API request to wttr.in (simple public weather endpoint)
            String endpoint = "https://wttr.in/" + city + "?format=3";
            HttpURLConnection connection = (HttpURLConnection) new URI(endpoint).toURL().openConnection();
            connection.setRequestMethod("GET");

            // Read response directly from the input stream
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String result = scanner.useDelimiter("\\A").next();
                return "Current weather in " + city + ": " + result;
            }

        } catch (IOException e) {
            return "⚠️ Unable to fetch weather data for " + city + ". Cause: " + e.getMessage();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid city name or URI: " + city, e);
        }
    }
}
