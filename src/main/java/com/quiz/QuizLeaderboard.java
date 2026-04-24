package com.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class QuizLeaderboard {

    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final String my_registration_number = "RA2311030010031"; // <-- Replace with your actual registration number
    private static final int polls_totally = 10;
    private static final int delay_in_ms = 5000; // 5 seconds between polls

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // Key: "roundId|participant" → score (deduplicated)
        Map<String, Integer> deduplicatedEvents = new LinkedHashMap<>();

        System.out.println("=== Starting Quiz Leaderboard System ===\n");

        //Polling the API 10 times (poll index is from 0 to 9)
        for (int poll = 0; poll < polls_totally; poll++) {
            System.out.printf("Polling %d/%d (poll=%d)...%n", poll + 1, polls_totally, poll);

            String url = BASE_URL + "/quiz/messages?regNo=" + my_registration_number + "&poll=" + poll;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("  ERROR: HTTP " + response.statusCode() + " - " + response.body());
                continue;
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode events = root.get("events");

            if (events == null || !events.isArray()) {
                System.out.println("  No events found in this poll.");
            } else {
                int newCount = 0, dupCount = 0;

                for (JsonNode event : events) {
                    String roundId = event.get("roundId").asText();
                    String participant = event.get("participant").asText();
                    int score = event.get("score").asInt();

                    // Deduplication key: roundId + participant
                    String key = roundId + "|" + participant;

                    if (!deduplicatedEvents.containsKey(key)) {
                        deduplicatedEvents.put(key, score);
                        newCount++;
                    } else {
                        dupCount++; // silently ignore the duplicate
                    }
                }

                System.out.printf("  Events received: %d | New: %d | Duplicates ignored: %d%n",
                        events.size(), newCount, dupCount);
            }

            // we waiting 5 seconds between the polls (except after the last poll)
            if (poll < polls_totally - 1) {
                System.out.println("  Waiting 5 seconds...\n");
                Thread.sleep(delay_in_ms);
            }
        }

        // Aggregating the scores per participant
        Map<String, Integer> scoreMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : deduplicatedEvents.entrySet()) {
            String participant = entry.getKey().split("\\|")[1];
            scoreMap.merge(participant, entry.getValue(), Integer::sum);
        }

        //Sort the leaderboard by totalScore in a descending order
        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>(scoreMap.entrySet());
        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        // Print the leaderboard after sorting in descending order
        System.out.println("\n=== Final Leaderboard ===");
        int totalScore = 0;
        for (Map.Entry<String, Integer> entry : leaderboard) {
            System.out.printf("  %-20s : %d%n", entry.getKey(), entry.getValue());
            totalScore += entry.getValue();
        }
        System.out.println("  -------------------------");
        System.out.println("  TOTAL SCORE (all users) : " + totalScore);

        // Building a submission payload
        ObjectNode payload = mapper.createObjectNode();
        payload.put("regNo", my_registration_number);
        ArrayNode leaderboardArray = mapper.createArrayNode();
        for (Map.Entry<String, Integer> entry : leaderboard) {
            ObjectNode participant = mapper.createObjectNode();
            participant.put("participant", entry.getKey());
            participant.put("totalScore", entry.getValue());
            leaderboardArray.add(participant);
        }
        payload.set("leaderboard", leaderboardArray);

        String payloadJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        System.out.println("\n=== Submitting Leaderboard ===");
        System.out.println("Payload:\n" + payloadJson);

        // Step 7: Submit leaderboard once
        HttpRequest submitRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                .build();

        HttpResponse<String> submitResponse = client.send(submitRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("\n=== Submission Response ===");
        System.out.println("HTTP Status : " + submitResponse.statusCode());
        JsonNode result = mapper.readTree(submitResponse.body());
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        boolean isCorrect = result.has("isCorrect") && result.get("isCorrect").asBoolean();
        System.out.println("\n" + (isCorrect ? "✅ SUCCESS! Leaderboard is correct." : "❌ INCORRECT. Check your logic."));
    }
}
