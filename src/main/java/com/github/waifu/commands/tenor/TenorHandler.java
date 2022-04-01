package com.github.waifu.commands.tenor;

import org.json.JSONObject;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class TenorHandler {

    private static final String API_KEY = "2WFV9G1IPF7I";

    /**
     * Get Search Result GIFs
     */
    public static void getSearchResults(String searchTerm,
                                        Consumer<JSONObject> responseHandler,
                                        Consumer<Exception> errorHandler) {
        searchTerm = searchTerm.replace(" ", "%20");
        final String url = String.format("https://g.tenor.com/v1/random?q=%1$s&key=%2$s&limit=1", searchTerm, API_KEY);
        new Thread(() -> {
            try {
                responseHandler.accept(get(url));
            }
            catch (Exception e) {
                errorHandler.accept(e);
            }
        }).start();
    }

    /**
     * Construct and run a GET request
     */
    private static JSONObject get(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try (AutoCloseable conn = connection::disconnect){
            // Get request
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Handle failure
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                String error = String.format("HTTP Code: '%1$s' from '%2$s'", statusCode, url);
                throw new ConnectException(error);
            }

            return parser(connection);
        }
    }

    /**
     * Parse the response into JSONObject
     */
    private static JSONObject parser(HttpURLConnection connection) throws Exception {
        char[] buffer = new char[4096];
        int n;
        try (InputStream stream = new BufferedInputStream(connection.getInputStream())) {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            StringWriter writer = new StringWriter();
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            return new JSONObject(writer.toString());
        }
    }
}
