# Phần Quản trị viên (Admin) – Hệ thống quản lý sự kiện LCĐ CNS (EPU)

## 1. Luồng dữ liệu (giống phần Sinh viên)

```
Swing (FE)  --HTTP/JSON-->  Controller  -->  Service (kiểm tra nghiệp vụ)  -->  Repository (JPA)  -->  MySQL
QuanLyTaiKhoanPanel   GET /api/admin/tai-khoan   AdminTaiKhoanController   TaiKhoanAdminService   TaiKhoanRepository   tai_khoan
```

- FE **không** kết nối thẳng DB, chỉ gọi API qua `ApiClient` (gói `Admin/common/Api.java` bọc thêm DELETE + chạy nền).
- BE chia 3 lớp: **Controller** nhận yêu cầu → **Service** kiểm tra đúng/sai → **Repository** đọc/ghi DB.
- Lỗi nghiệp vụ: Service `throw new LoiNghiepVu("...")` → `AdminExceptionHandler` trả `{"message": "..."}` → FE hiện thông báo.

## 2. Cấu trúc thư mục mới

```
backend/.../backend/admin/
    common/      HangSo (hằng số), LoiNghiepVu, AdminExceptionHandler, DuLieu (hàm phụ)
    service/     TaiKhoanAdmin, YeuCauThayDoi, PhanQuyen, SuKienAdmin, ThongBaoAdmin, TongQuan
    controller/  AdminTaiKhoan, AdminPhanQuyen, AdminSuKien, AdminThongBao, AdminTongQuan, YeuCauThayDoi
    AdminSeeder  dữ liệu mẫu (chạy sau DataSeeder, SuKienSeeder; "thiếu gì thêm nấy")
backend/.../entity/      + Quyen, VaiTroQuyen, TaiKhoanQuyen, YeuCauThayDoi, PhanCong

frontend/.../gui/Admin/
    common/      Theme (màu, font), Icons, UI (nút, ô nhập, thẻ, tab, phân trang), BangDep (bảng), HopThoai, Api, TrangAdmin
    DashboardAdmin            khung chính: menu trái + thanh trên + vùng nội dung
    TongQuanPanel, QuanLySuKienPanel (+SuKienDialogs), QuanLyThongBaoPanel,
    QuanLyTaiKhoanPanel (+TaiKhoanDialogs), PhanQuyenPanel, BaoCaoPanel, CaiDatPanel
```

## 3. Use case → API → màn hình

| Use case (draw.io) | API | Màn hình |
|---|---|---|
| Tra cứu tài khoản | `GET /api/admin/tai-khoan?tuKhoa=&vaiTro=&trangThai=&trang=0&kichThuoc=10` | Quản lý tài khoản |
| Tạo tài khoản | `POST /api/admin/tai-khoan` | nút "+ Tạo tài khoản" |
| Quản lý trạng thái TK | `PUT /api/admin/tai-khoan/{id}/trang-thai` `{trangThai: BI_KHOA/HOAT_DONG}` | nút ổ khóa |
| Duyệt thay đổi thông tin | `GET /api/admin/yeu-cau`, `PUT .../{id}/chap-nhan`, `PUT .../{id}/tu-choi` | tab "Duyệt thay đổi thông tin" |
| Cấp quyền | `PUT /api/admin/phan-quyen/tai-khoan/{id}/quyen` `{dsMaQuyen: [...]}` | Phân quyền – tab 1 |
| Thay đổi quyền (vai trò) | `PUT /api/admin/phan-quyen/tai-khoan/{id}/vai-tro` | Phân quyền – nút "Đổi vai trò" |
| Tra cứu quyền | `GET /api/admin/phan-quyen/tra-cuu?maQuyen=` | Phân quyền – tab 2 |
| Tạo / Cập nhật sự kiện | `POST /api/admin/su-kien`, `PUT /api/admin/su-kien/{id}` | Quản lý sự kiện |
| Duyệt kế hoạch sự kiện | `PUT /api/admin/su-kien/{id}/duyet`, `.../tu-choi` | tab "Kế hoạch chờ duyệt" |
| Theo dõi sự kiện | `GET /api/admin/su-kien/{id}/theo-doi` | nút "Theo dõi" |
| Phân công cán bộ | `GET/POST /api/admin/su-kien/{id}/phan-cong`, `DELETE /api/admin/su-kien/phan-cong/{id}` | nút hình 2 người |
| Tạo / Duyệt / Đăng / Cập nhật / Gỡ thông báo | `POST /api/admin/thong-bao`, `PUT .../{id}`, `.../duyet`, `.../tu-choi`, `.../dang`, `.../go` | Quản lý thông báo |
| Tổng quan, Báo cáo | `GET /api/admin/tong-quan`, `GET /api/admin/bao-cao` | Tổng quan, Báo cáo |
| Đổi mật khẩu | `PUT /api/admin/doi-mat-khau` | Cài đặt |

## 4. Quy tắc nghiệp vụ (kiểm tra ở backend)

- Tài khoản: tên đăng nhập 3-30 ký tự không dấu, không trùng; mật khẩu ≥ 6 ký tự; email đúng dạng, không trùng; SĐT 10 số bắt đầu bằng 0.
- Không khóa / xóa được Quản trị viên; hệ thống luôn còn ≥ 1 Quản trị viên; tài khoản đã đăng ký sự kiện thì chỉ được khóa, không xóa.
- **Tài khoản bị khóa không đăng nhập được** (`AuthController` trả 403).
- Từ chối (yêu cầu, kế hoạch, thông báo) bắt buộc nhập lý do.
- Sự kiện: kết thúc phải sau bắt đầu; tạo mới thì bắt đầu ở tương lai; số lượng tối đa không nhỏ hơn số đã đăng ký.
- Thông báo: Chờ duyệt → Đã duyệt → Đã đăng → Đã gỡ; bị từ chối, sửa lại sẽ về Chờ duyệt. "Đã gửi" của phần Cán bộ được hiểu là Chờ duyệt.
- Quyền thực tế = quyền mặc định của vai trò + quyền Admin cấp riêng.

## 5. File của nhóm có chỉnh (chỉ THÊM, không xóa)

- `entity/TaiKhoan, SuKien, ThongBao`: thêm cột (cho phép NULL → dữ liệu cũ không hỏng).
- `repository/TaiKhoan, ThongBao, DangKySuKien`: thêm hàm truy vấn.
- `controller/AuthController`: chặn đăng nhập khi tài khoản bị khóa.
- `gui/Login/Login`: hiện "Tài khoản đã bị khóa" khi nhận 403; truyền tên đăng nhập vào `DashboardAdmin`.

## 6. Chạy thử

1. Bật MySQL (DB `event_management_db`). Chạy `BackendApplication` → bảng mới tự tạo (`ddl-auto=update`), `AdminSeeder` thêm dữ liệu mẫu.
2. Chạy `Login`, đăng nhập `admin` / `admin123`.
3. Thử: khóa `canbo15` (đã khóa sẵn) → đăng nhập bằng `canbo15/canbo123` sẽ bị chặn.
4. Kiểm tra DB: `SELECT ten_dang_nhap, trang_thai FROM tai_khoan;`, `SELECT * FROM yeu_cau_thay_doi;`, `SELECT * FROM phan_cong;`
