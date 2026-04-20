package me.txmc.jellyfinauth;

import lombok.Getter;
import okhttp3.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Getter
public class JellyfinAPI {
    private final String baseUrl;
    private final String apiKey;
    private final OkHttpClient client;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final SecureRandom random = new SecureRandom();

    public JellyfinAPI(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private Request.Builder authHeader(Request.Builder request) {
        return request.addHeader("Authorization", "MediaBrowser Token=\"" + apiKey + "\"");
    }

    public String createUser(String username) throws IOException {
        String json = String.format("{\"Name\":\"%s\"}", username);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = authHeader(new Request.Builder())
                .url(baseUrl + "Users/New")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                return extractUserId(responseBody);
            } else {
                throw new IOException("Failed to create user: " + response.code() + " - " + response.message());
            }
        }
    }

    private String extractUserId(String json) {
        int idIndex = json.indexOf("\"Id\":\"");
        if (idIndex == -1) idIndex = json.indexOf("\"Id\":\"");
        if (idIndex != -1) {
            int start = idIndex + 6;
            int end = json.indexOf("\"", start);
            if (end != -1) return json.substring(start, end);
        }
        return null;
    }

    public void setPassword(String userId, String password) throws IOException {
        String json = String.format("{\"Id\":\"%s\",\"CurrentPw\":\"\",\"NewPw\":\"%s\"}", userId, password);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = authHeader(new Request.Builder())
                .url(baseUrl + "Users/" + userId + "/Password")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to set password: " + response.code() + " - " + response.message());
            }
        }
    }

    public boolean userExists(String username) throws IOException {
        Request request = authHeader(new Request.Builder())
                .url(baseUrl + "Users")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                return responseBody.contains("\"Name\":\"" + username + "\"");
            }
            return false;
        }
    }

    public String generatePassword() {
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, 12);
    }
}