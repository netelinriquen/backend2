package com.inkflow.api;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class SupabaseService {
    private static final String SUPABASE_URL = "https://iksathqjjsdswcnlrsqm.supabase.co";
    private static final String API_KEY = System.getenv("SUPABASE_API_KEY");
    
    public static void testConnection() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + "/rest/v1/"))
                .header("apikey", API_KEY)
                .GET()
                .build();
                
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Supabase API conectada! Status: " + response.statusCode());
        } catch (Exception e) {
            System.out.println("Erro API Supabase: " + e.getMessage());
        }
    }
}