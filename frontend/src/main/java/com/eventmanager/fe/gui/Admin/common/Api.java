package com.eventmanager.fe.gui.Admin.common;

import com.eventmanager.fe.api.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Gọi API cho phần Admin. Dùng lại ApiClient (GET/POST/PUT) có sẵn của nhóm,
 * bổ sung DELETE và các hàm đọc JSON an toàn (không lo null).
 *
 * QUAN TRỌNG: gọi mạng phải chạy ở luồng nền (SwingWorker), nếu không giao diện sẽ bị "đơ".
 */
public final class Api {

    private Api() {
    }

    public interface Viec<T> {
        T chay() throws ApiClient.ApiException;
    }

    /** Chạy việc ở luồng nền; xong thì gọi thanhCong trên luồng giao diện, lỗi thì hiện hộp thoại. */
    public static <T> void chayNen(Component cha, Viec<T> viec, Consumer<T> thanhCong) {
        chayNen(cha, viec, thanhCong, () -> { });
    }

    /** Chặn bấm lặp trong lúc một thao tác ghi dữ liệu của trang đang chạy. */
    public static <T> void chayThaoTac(Component cha, Viec<T> viec, Consumer<T> thanhCong) {
        if (!(cha instanceof TrangAdmin trang)) {
            chayNen(cha, viec, thanhCong);
            return;
        }
        if (Boolean.TRUE.equals(trang.getClientProperty("admin.dangLuu"))) return;
        trang.putClientProperty("admin.dangLuu", true);
        chayNen(cha, viec, thanhCong, () -> trang.putClientProperty("admin.dangLuu", false));
    }

    private static <T> void chayNen(Component cha, Viec<T> viec, Consumer<T> thanhCong, Runnable ketThuc) {
        // Chỉ các trang Admin dùng vùng trạng thái; các màn hình khác giữ cách báo lỗi cũ.
        TrangAdmin trang = cha instanceof TrangAdmin ? (TrangAdmin) cha
                : (TrangAdmin) SwingUtilities.getAncestorOfClass(TrangAdmin.class, cha);
        if (trang != null) trang.batDauTai();
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return viec.chay();
            }

            @Override
            protected void done() {
                String loi = null;
                try {
                    thanhCong.accept(get());
                } catch (ExecutionException e) {
                    loi = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
                    if (trang == null) HopThoai.loi(cha, loi);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    loi = "Yêu cầu bị gián đoạn";
                } finally {
                    if (trang != null) trang.ketThucTai(loi);
                    ketThuc.run();
                }
            }
        }.execute();
    }

    public static JsonElement get(String path) throws ApiClient.ApiException {
        return ApiClient.get(path);
    }

    public static JsonElement post(String path, JsonObject body) throws ApiClient.ApiException {
        return ApiClient.post(path, body);
    }

    public static JsonElement put(String path, JsonObject body) throws ApiClient.ApiException {
        return ApiClient.put(path, body == null ? new JsonObject() : body);
    }

    /** ApiClient của nhóm chưa có DELETE -> bổ sung ở đây. */
    public static JsonElement delete(String path) throws ApiClient.ApiException {
        try {
            HttpResponse<String> res = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
                    .send(HttpRequest.newBuilder(URI.create(ApiClient.BASE_URL + path)).DELETE()
                            .timeout(Duration.ofSeconds(10)).build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonElement json = res.body() == null || res.body().isBlank() ? JsonNull.INSTANCE
                    : com.google.gson.JsonParser.parseString(res.body());
            if (res.statusCode() >= 200 && res.statusCode() < 300) return json;
            throw new ApiClient.ApiException(json.isJsonObject() && json.getAsJsonObject().has("message")
                    ? json.getAsJsonObject().get("message").getAsString() : "Lỗi máy chủ (HTTP " + res.statusCode() + ")");
        } catch (java.io.IOException e) {
            throw new ApiClient.ApiException("Không kết nối được máy chủ. Hãy chạy backend (cổng 8080) trước.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiClient.ApiException("Yêu cầu bị gián đoạn");
        }
    }

    public static String e(String s) {
        return ApiClient.encode(s);
    }

    // ---------------- Đọc JSON an toàn ----------------

    public static String s(JsonObject o, String key) {
        return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    public static long n(JsonObject o, String key) {
        try {
            return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsLong() : 0;
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    public static double d(JsonObject o, String key) {
        try {
            return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsDouble() : 0;
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    public static JsonArray arr(JsonObject o, String key) {
        return o != null && o.has(key) && o.get(key).isJsonArray() ? o.getAsJsonArray(key) : new JsonArray();
    }

    public static JsonObject obj(JsonObject o, String key) {
        return o != null && o.has(key) && o.get(key).isJsonObject() ? o.getAsJsonObject(key) : new JsonObject();
    }

    public static JsonArray arr(JsonElement e) {
        return e != null && e.isJsonArray() ? e.getAsJsonArray() : new JsonArray();
    }

    public static JsonObject obj(JsonElement e) {
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : new JsonObject();
    }

    /** Tạo nhanh body JSON: Api.body("hoTen", "An", "email", "a@x.vn") */
    public static JsonObject body(String... capKhoaGiaTri) {
        JsonObject o = new JsonObject();
        for (int i = 0; i + 1 < capKhoaGiaTri.length; i += 2) o.addProperty(capKhoaGiaTri[i], capKhoaGiaTri[i + 1]);
        return o;
    }
}
