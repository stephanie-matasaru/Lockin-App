package com.lockin.server.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiService {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String SERVER_URL = "http://localhost:8080/api"; // Your server address

    public static void login(String username, String password) {
        String jsonInputString = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    System.out.println("Server said: " + responseBody);

                })
                .exceptionally(e -> {
                    System.err.println("Error: " + e.getMessage());
                    return null;
                });
    }
}