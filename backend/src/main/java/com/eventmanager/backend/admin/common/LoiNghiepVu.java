package com.eventmanager.backend.admin.common;

/**
 * Lỗi do người dùng nhập sai / vi phạm quy tắc nghiệp vụ.
 * Service chỉ cần "throw new LoiNghiepVu(...)", AdminExceptionHandler sẽ tự trả về JSON {"message": "..."}
 * -> FE (ApiClient) đọc trường "message" và hiện hộp thoại báo lỗi.
 */
public class LoiNghiepVu extends RuntimeException {

    private final int maHttp;

    public LoiNghiepVu(String message) {
        this(400, message);
    }

    public LoiNghiepVu(int maHttp, String message) {
        super(message);
        this.maHttp = maHttp;
    }

    public int getMaHttp() {
        return maHttp;
    }

    public static LoiNghiepVu khongTimThay(String doiTuong) {
        return new LoiNghiepVu(404, "Không tìm thấy " + doiTuong);
    }
}
