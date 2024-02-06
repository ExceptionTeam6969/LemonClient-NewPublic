package dev.lemonclient.utils.misc;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JSONUtils {
    private static String readAll(Reader rd) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        int cp;
        while ((cp = rd.read()) != -1) {
            stringBuilder.append((char) cp);
        }

        return stringBuilder.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            return new JSONObject(readAll(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))));
        }
    }
}
