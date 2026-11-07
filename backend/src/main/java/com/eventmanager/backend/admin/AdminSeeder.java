package com.eventmanager.backend.admin;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.common.HangSo;
import com.eventmanager.backend.entity.*;
import com.eventmanager.backend.repository.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Bổ sung dữ liệu mẫu cho phần Admin. Chạy SAU khi DataSeeder và SuKienSeeder chạy xong
 * (ApplicationReadyEvent luôn đến sau các CommandLineRunner).
 * Kiểu "thiếu gì thêm nấy": đã có thì bỏ qua -> chạy lại nhiều lần không bị trùng.
 */
@Component
public class AdminSeeder {

    private final TaiKhoanRepository taiKhoanRepo;
    private final SinhVienRepository sinhVienRepo;
    private final QuyenRepository quyenRepo;
    private final VaiTroQuyenRepository vaiTroQuyenRepo;
    private final YeuCauThayDoiRepository yeuCauRepo;
    private final SuKienRepository suKienRepo;
    private final ThongBaoRepository thongBaoRepo;
    private final PhanCongRepository phanCongRepo;

    public AdminSeeder(TaiKhoanRepository taiKhoanRepo, SinhVienRepository sinhVienRepo, QuyenRepository quyenRepo,
                       VaiTroQuyenRepository vaiTroQuyenRepo, YeuCauThayDoiRepository yeuCauRepo,
                       SuKienRepository suKienRepo, ThongBaoRepository thongBaoRepo, PhanCongRepository phanCongRepo) {
        this.taiKhoanRepo = taiKhoanRepo;
        this.sinhVienRepo = sinhVienRepo;
        this.quyenRepo = quyenRepo;
        this.vaiTroQuyenRepo = vaiTroQuyenRepo;
        this.yeuCauRepo = yeuCauRepo;
        this.suKienRepo = suKienRepo;
        this.thongBaoRepo = thongBaoRepo;
        this.phanCongRepo = phanCongRepo;
    }

    private static final String[] HO_TEN_CAN_BO = {
            "Nguyễn Hải Nam", "Phạm Huy Hoàng", "Dương Anh Tú", "Nguyễn Văn Linh", "Hoàng Thị Huế",
            "Lê Văn Đức", "Trần Thị Mai", "Lê Minh Bảo", "Phạm Thị Dung", "Đỗ Quang Huy",
            "Vũ Thu Trang", "Bùi Đức Mạnh", "Ngô Thanh Hương", "Đinh Công Sơn", "Mai Phương Thảo"};

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        try {
            taoDanhMucQuyen();
            boSungThongTinTaiKhoan();
            taoYeuCauMau();
            taoKeHoachChoDuyet();
            taoThongBaoMau();
            taoPhanCongMau();
            System.out.println("[AdminSeeder] Đã kiểm tra / bổ sung dữ liệu mẫu cho phần Admin.");
        } catch (Exception e) {
            System.out.println("[AdminSeeder] Lỗi khi tạo dữ liệu mẫu: " + e.getMessage());
        }
    }

    // ---------- 1. Danh mục quyền + quyền mặc định theo vai trò ----------
    private void taoDanhMucQuyen() {
        Object[][] ds = {
                // maQuyen, tenQuyen, nhóm, mô tả, vai trò mặc định
                {"QL_TAI_KHOAN", "Quản lý tài khoản", "Tài khoản", "Tạo, tra cứu, khóa / mở khóa tài khoản", List.of(HangSo.ADMIN)},
                {"PHAN_QUYEN", "Phân quyền người dùng", "Tài khoản", "Cấp quyền, thay đổi quyền, tra cứu quyền", List.of(HangSo.ADMIN)},
                {"QL_SU_KIEN", "Quản lý sự kiện", "Sự kiện", "Tạo và cập nhật thông tin sự kiện", List.of(HangSo.ADMIN)},
                {"DUYET_KE_HOACH", "Duyệt kế hoạch sự kiện", "Sự kiện", "Duyệt hoặc từ chối kế hoạch do cán bộ lập", List.of(HangSo.ADMIN)},
                {"LAP_KE_HOACH", "Lập kế hoạch sự kiện", "Sự kiện", "Lập kế hoạch, phân công nhiệm vụ, theo dõi tiến độ", List.of(HangSo.CAN_BO)},
                {"DIEM_DANH", "Điểm danh người tham gia", "Sự kiện", "Điểm danh, quản lý người tham gia sự kiện", List.of(HangSo.CAN_BO)},
                {"QL_TAI_LIEU", "Quản lý tài liệu sự kiện", "Sự kiện", "Tải lên, cập nhật tài liệu sự kiện", List.of(HangSo.CAN_BO)},
                {"TAO_THONG_BAO", "Tạo thông báo", "Thông báo", "Soạn và cập nhật thông báo", List.of(HangSo.ADMIN, HangSo.CAN_BO)},
                {"DUYET_THONG_BAO", "Duyệt và đăng thông báo", "Thông báo", "Duyệt, đăng và gỡ thông báo", List.of(HangSo.ADMIN)},
                {"XEM_BAO_CAO", "Xem báo cáo thống kê", "Báo cáo", "Xem số liệu tổng hợp các sự kiện", List.of(HangSo.ADMIN, HangSo.CAN_BO)},
                {"DANG_KY_SU_KIEN", "Đăng ký tham gia sự kiện", "Sinh viên", "Tra cứu, đăng ký, hủy đăng ký sự kiện", List.of(HangSo.HOC_SINH)},
                {"DANH_GIA_SU_KIEN", "Đánh giá sự kiện", "Sinh viên", "Đánh giá và phản hồi sự kiện đã tham gia", List.of(HangSo.HOC_SINH)},
        };
        boolean chuaCoVaiTroQuyen = vaiTroQuyenRepo.count() == 0;
        for (Object[] r : ds) {
            String ma = (String) r[0];
            if (quyenRepo.findByMaQuyen(ma).isEmpty()) {
                Quyen q = new Quyen();
                q.setMaQuyen(ma);
                q.setTenQuyen((String) r[1]);
                q.setNhom((String) r[2]);
                q.setMoTa((String) r[3]);
                quyenRepo.save(q);
            }
            if (chuaCoVaiTroQuyen) {
                @SuppressWarnings("unchecked")
                List<String> vaiTro = (List<String>) r[4];
                for (String vt : vaiTro) {
                    VaiTroQuyen v = new VaiTroQuyen();
                    v.setVaiTro(vt);
                    v.setMaQuyen(ma);
                    vaiTroQuyenRepo.save(v);
                }
            }
        }
    }

    // ---------- 2. Điền họ tên, email, mã số, trạng thái cho tài khoản cũ ----------
    private void boSungThongTinTaiKhoan() {
        boolean daCoTaiKhoanKhoa = taiKhoanRepo.countByTrangThai(HangSo.TK_BI_KHOA) > 0;
        LocalDateTime goc = LocalDateTime.now().minusDays(50).withHour(8).withMinute(30).withSecond(0).withNano(0);

        List<TaiKhoan> ds = taiKhoanRepo.findAll();
        for (int i = 0; i < ds.size(); i++) {
            TaiKhoan tk = ds.get(i);
            boolean thayDoi = false;
            SinhVien sv = sinhVienRepo.findByTenDangNhap(tk.getTenDangNhap()).orElse(null);

            if (DuLieu.rong(tk.getHoTen())) {
                tk.setHoTen(goiYHoTen(tk, sv));
                thayDoi = true;
            }
            if (DuLieu.rong(tk.getEmail())) {
                tk.setEmail(sv != null && !DuLieu.rong(sv.getEmail()) ? sv.getEmail() : tk.getTenDangNhap() + "@epu.edu.vn");
                thayDoi = true;
            }
            if (DuLieu.rong(tk.getMaSo())) {
                tk.setMaSo(goiYMaSo(tk, sv));
                thayDoi = true;
            }
            if (DuLieu.rong(tk.getSoDienThoai()) && sv != null && !DuLieu.rong(sv.getSoDienThoai())) {
                tk.setSoDienThoai(sv.getSoDienThoai());
                thayDoi = true;
            }
            if (DuLieu.rong(tk.getTrangThai())) {
                tk.setTrangThai(HangSo.TK_HOAT_DONG);
                // Dữ liệu mẫu: khóa sẵn 1 tài khoản cán bộ để thấy trạng thái "Bị khóa"
                if (!daCoTaiKhoanKhoa && "canbo15".equals(tk.getTenDangNhap())) {
                    tk.setTrangThai(HangSo.TK_BI_KHOA);
                }
                thayDoi = true;
            }
            if (tk.getNgayTao() == null) {
                tk.setNgayTao(goc.plusDays(i).plusMinutes(i * 37L % 300));
                thayDoi = true;
            }
            if (thayDoi) taiKhoanRepo.save(tk);
        }
    }

    private String goiYHoTen(TaiKhoan tk, SinhVien sv) {
        if (sv != null && !DuLieu.rong(sv.getHoTen())) return sv.getHoTen();
        if (HangSo.ADMIN.equals(tk.getVaiTro())) return "Trần Ngọc Anh";
        String ten = tk.getTenDangNhap();
        if (ten.matches("canbo\\d+")) {
            int so = Integer.parseInt(ten.substring(5));
            if (so >= 1 && so <= HO_TEN_CAN_BO.length) return HO_TEN_CAN_BO[so - 1];
        }
        return ten;
    }

    private String goiYMaSo(TaiKhoan tk, SinhVien sv) {
        if (sv != null && !DuLieu.rong(sv.getMaSinhVien())) return sv.getMaSinhVien();
        if (HangSo.ADMIN.equals(tk.getVaiTro())) return "NV001";
        return String.format("%s%03d", HangSo.CAN_BO.equals(tk.getVaiTro()) ? "CB" : "SV", tk.getId());
    }

    // ---------- 3. Yêu cầu thay đổi thông tin mẫu ----------
    private void taoYeuCauMau() {
        if (yeuCauRepo.count() > 0) return;
        Object[][] ds = {
                {"hocsinh02", "Trần Thị Bình An", "", "0912345602", "", "Bổ sung tên đệm theo giấy khai sinh, đính kèm ảnh CCCD", 2},
                {"hocsinh03", "", "cuong.lehoang@epu.edu.vn", "", "", "Đổi sang email trường cấp", 1},
                {"hocsinh05", "", "", "0987654305", "D19CNPM3", "Chuyển lớp theo quyết định của khoa", 0},
        };
        LocalDateTime now = LocalDateTime.now();
        for (Object[] r : ds) {
            if (taiKhoanRepo.findByTenDangNhap((String) r[0]).isEmpty()) continue;
            YeuCauThayDoi yc = new YeuCauThayDoi();
            yc.setTenDangNhap((String) r[0]);
            yc.setHoTenMoi((String) r[1]);
            yc.setEmailMoi((String) r[2]);
            yc.setSoDienThoaiMoi((String) r[3]);
            yc.setLopMoi((String) r[4]);
            yc.setLyDo((String) r[5]);
            yc.setTrangThai(HangSo.CHO_DUYET);
            yc.setNgayGui(now.minusDays((Integer) r[6]).minusHours(3));
            yeuCauRepo.save(yc);
        }
    }

    // ---------- 4. Kế hoạch sự kiện do cán bộ lập, đang chờ Admin duyệt ----------
    private void taoKeHoachChoDuyet() {
        LocalDateTime base = LocalDateTime.now().plusDays(20).withHour(8).withMinute(0).withSecond(0).withNano(0);
        Object[][] ds = {
                {"SK101", "Ngày hội Tân sinh viên CNTT 2026", "Sân trường cơ sở 1", 0, 400,
                        "Chào đón tân sinh viên: giới thiệu CLB, gian hàng chi đoàn, giao lưu văn nghệ.", "canbo01"},
                {"SK102", "Cuộc thi Lập trình Java mở rộng", "Phòng máy tầng 3 nhà B", 9, 120,
                        "Thi lập trình theo đội 3 người, đề bài thuật toán và xây dựng ứng dụng nhỏ.", "canbo02"},
        };
        for (Object[] r : ds) {
            if (suKienRepo.findByMaSuKien((String) r[0]).isPresent()) continue;
            SuKien sk = new SuKien();
            sk.setMaSuKien((String) r[0]);
            sk.setTenSuKien((String) r[1]);
            sk.setDiaDiem((String) r[2]);
            sk.setThoiGianBatDau(base.plusDays((Integer) r[3]));
            sk.setThoiGianKetThuc(base.plusDays((Integer) r[3]).plusHours(4));
            sk.setSoLuongToiDa((Integer) r[4]);
            sk.setMoTa((String) r[5]);
            sk.setDonViToChuc("BCH Liên chi đoàn CNTT");
            sk.setTrangThai(HangSo.SK_SAP_DIEN_RA);
            sk.setTrangThaiDuyet(HangSo.CHO_DUYET);
            sk.setNguoiTao((String) r[6]);
            suKienRepo.save(sk);
        }
    }

    // ---------- 5. Thông báo mẫu ở các trạng thái ----------
    private void taoThongBaoMau() {
        Object[][] ds = {
                {"TB-AD01", "Lịch họp BCH Liên chi đoàn tháng 11", "Kính mời các đồng chí BCH họp giao ban vào 14h thứ Sáu tại phòng Đoàn.", "Ban tổ chức", HangSo.CHO_DUYET, "canbo01"},
                {"TB-AD02", "Mở đăng ký Hội thao sinh viên CNTT", "Các chi đoàn đăng ký đội tham gia trước ngày 25. Mỗi chi đoàn tối đa 2 đội.", "Sinh viên", HangSo.DA_DUYET, "canbo03"},
                {"TB-AD03", "Kết quả cuộc thi Ý tưởng sáng tạo", "BTC công bố danh sách đội đạt giải và lịch trao giải tại hội trường.", "Tất cả người tham gia", HangSo.TB_DA_DANG, "admin"},
        };
        for (Object[] r : ds) {
            if (thongBaoRepo.existsByMaThongBao((String) r[0])) continue;
            ThongBao tb = new ThongBao();
            tb.setMaThongBao((String) r[0]);
            tb.setTieuDe((String) r[1]);
            tb.setNoiDung((String) r[2]);
            tb.setDoiTuong((String) r[3]);
            tb.setTrangThai((String) r[4]);
            tb.setNguoiTao((String) r[5]);
            tb.setNgayGui(LocalDate.now().minusDays(2));
            if (HangSo.TB_DA_DANG.equals(r[4])) tb.setNgayDang(LocalDateTime.now().minusDays(1));
            thongBaoRepo.save(tb);
        }
    }

    // ---------- 6. Phân công mẫu cho sự kiện sắp diễn ra đầu tiên ----------
    private void taoPhanCongMau() {
        if (phanCongRepo.count() > 0) return;
        LocalDateTime now = LocalDateTime.now();
        SuKien sk = suKienRepo.findAll().stream()
                .filter(s -> s.getThoiGianBatDau() != null && s.getThoiGianBatDau().isAfter(now))
                .filter(s -> DuLieu.rong(s.getTrangThaiDuyet()) || HangSo.DA_DUYET.equals(s.getTrangThaiDuyet()))
                .findFirst().orElse(null);
        if (sk == null) return;
        for (Map.Entry<String, String> e : Map.of("canbo01", "Trưởng ban tổ chức", "canbo02", "Hậu cần").entrySet()) {
            if (taiKhoanRepo.findByTenDangNhap(e.getKey()).isEmpty()) continue;
            PhanCong pc = new PhanCong();
            pc.setSuKienId(sk.getId());
            pc.setTenDangNhap(e.getKey());
            pc.setVaiTroPhuTrach(e.getValue());
            pc.setNgayPhanCong(now.minusDays(1));
            phanCongRepo.save(pc);
        }
    }
}
