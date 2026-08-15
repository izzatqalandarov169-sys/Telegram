package org.telegram.ui;

import android.content.Context;
import android.net.Uri;

import org.telegram.messenger.UserConfig;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public final class CustomGiftApi {
    public static final String BASE_URL = "https://messenger-clone-zbef.onrender.com";
    private CustomGiftApi() {}

    public static String get(String path) throws Exception {
        return request("GET", path, null, null);
    }

    public static String postJson(String path, String json) throws Exception {
        return request("POST", path, "application/json", json.getBytes("UTF-8"));
    }

    private static String request(String method, String path, String contentType, byte[] body) throws Exception {
        URL url = new URL(BASE_URL + (path.startsWith("/") ? path : "/" + path));
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.setUseCaches(false);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("X-App-User-Id", String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()));
        if (body != null) {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", contentType);
            c.setFixedLengthStreamingMode(body.length);
            try (DataOutputStream out = new DataOutputStream(c.getOutputStream())) {
                out.write(body);
            }
        }
        int code = c.getResponseCode();
        InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        if (in == null) return "";
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            return sb.toString();
        } finally {
            c.disconnect();
        }
    }

    public static String uploadVideo(Context context, Uri uri, String title, long stars) throws Exception {
        String boundary = "----TelegramCustomGift" + UUID.randomUUID();
        URL url = new URL(BASE_URL + "/api/gifts");
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(60000);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        c.setRequestProperty("X-App-User-Id", String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()));

        try (DataOutputStream out = new DataOutputStream(c.getOutputStream())) {
            writeField(out, boundary, "title", title);
            writeField(out, boundary, "stars", String.valueOf(stars));
            writeField(out, boundary, "creator_id", String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()));
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"video\"; filename=\"gift.mp4\"\r\n");
            out.writeBytes("Content-Type: video/mp4\r\n\r\n");
            try (InputStream in = context.getContentResolver().openInputStream(uri)) {
                if (in == null) throw new IllegalStateException("Video faylini ochib bo'lmadi");
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            }
            out.writeBytes("\r\n--" + boundary + "--\r\n");
            out.flush();
        }
        int code = c.getResponseCode();
        InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        if (in == null) return "";
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            return sb.toString();
        } finally {
            c.disconnect();
        }
    }

    private static void writeField(DataOutputStream out, String boundary, String name, String value) throws Exception {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        out.writeBytes(value == null ? "" : value);
        out.writeBytes("\r\n");
    }
}
