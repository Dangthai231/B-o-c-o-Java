package com.eventmanager.backend;

import com.eventmanager.backend.controller.DangKyController;
import com.eventmanager.backend.entity.DangKySuKien;
import com.eventmanager.backend.entity.DanhGiaSuKien;
import com.eventmanager.backend.entity.SinhVien;
import com.eventmanager.backend.entity.SuKien;
import com.eventmanager.backend.repository.DangKySuKienRepository;
import com.eventmanager.backend.repository.DanhGiaSuKienRepository;
import com.eventmanager.backend.repository.SinhVienRepository;
import com.eventmanager.backend.repository.SuKienRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tạo dữ liệu mẫu cho phần Sinh viên: sự kiện, hồ sơ sinh viên, lượt đăng ký và đánh giá.
 * Chạy kiểu "thiếu gì thêm nấy": dòng nào đã có (theo mã sự kiện, cặp sự kiện + sinh viên)
 * thì bỏ qua, nên DB cũ cũng được bổ sung mà không bị trùng.
 * Thời gian tính theo ngày chạy để luôn có sự kiện đã diễn ra và sắp diễn ra.
 */
@Component
public class SuKienSeeder implements CommandLineRunner {

    private static final String DA_THAM_GIA = "Đã tham gia";
    private static final String VANG_MAT = "Vắng mặt";
    private static final int SO_SINH_VIEN = 14;

    private final SuKienRepository suKienRepo;
    private final SinhVienRepository sinhVienRepo;
    private final DangKySuKienRepository dangKyRepo;
    private final DanhGiaSuKienRepository danhGiaRepo;

    private LocalDateTime now;
    private int soSuKienMoi, soDangKyMoi, soDanhGiaMoi;

    public SuKienSeeder(SuKienRepository suKienRepo, SinhVienRepository sinhVienRepo,
                        DangKySuKienRepository dangKyRepo, DanhGiaSuKienRepository danhGiaRepo) {
        this.suKienRepo = suKienRepo;
        this.sinhVienRepo = sinhVienRepo;
        this.dangKyRepo = dangKyRepo;
        this.danhGiaRepo = danhGiaRepo;
    }

    @Override
    public void run(String... args) {
        now = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
        soSuKienMoi = soDangKyMoi = soDanhGiaMoi = 0;

        taoHoSoSinhVien();

        // ---------- Sự kiện đã diễn ra ----------
        SuKien sk8 = taoSuKien("SK008", "Hội trại Liên chi đoàn CNTT", "Khuôn viên EPU", -45, 300, "Đã kết thúc",
                "Hội trại 2 ngày gồm cắm trại, trò chơi lớn, thi nấu ăn và đêm lửa trại giữa các chi đoàn.");
        SuKien sk9 = taoSuKien("SK009", "Tọa đàm Kỹ năng viết CV và phỏng vấn IT", "Hội trường tầng 5 nhà M", -30, 120, "Đã kết thúc",
                "Chuyên gia tuyển dụng chia sẻ cách viết CV, chuẩn bị portfolio và trả lời phỏng vấn kỹ thuật.");
        SuKien sk10 = taoSuKien("SK010", "Ngày hội việc làm ngành CNTT", "Sảnh nhà A - EPU", -14, 500, "Đã kết thúc",
                "Gặp gỡ hơn 20 doanh nghiệp công nghệ, phỏng vấn thực tập và việc làm tại chỗ.");
        SuKien sk1 = taoSuKien("SK001", "Ngày hội hiến máu tình nguyện", "Sảnh nhà A - EPU", -20, 200, "Đã kết thúc",
                "Chương trình hiến máu \"Giọt hồng EPU\" phối hợp cùng Viện Huyết học - Truyền máu Trung ương.");
        SuKien sk2 = taoSuKien("SK002", "Hội thảo AI và ứng dụng", "Hội trường tầng 5 nhà M", -7, 150, "Đã kết thúc",
                "Giới thiệu xu hướng trí tuệ nhân tạo, mô hình ngôn ngữ lớn và ứng dụng trong ngành điện.");
        SuKien sk11 = taoSuKien("SK011", "Chủ nhật xanh - dọn vệ sinh khuôn viên", "Khuôn viên EPU", -3, 80, "Đã kết thúc",
                "Đoàn viên cùng dọn vệ sinh, trồng cây xanh quanh khuôn viên trường.");

        // ---------- Sự kiện sắp diễn ra ----------
        taoSuKien("SK007", "Đêm nhạc chào tân sinh viên", "Sân trường EPU", 3, 2, "Sắp diễn ra",
                "Đêm nhạc chào đón tân sinh viên khóa mới (chỉ 2 chỗ để thử trường hợp hết chỗ).");
        SuKien sk3 = taoSuKien("SK003", "Giải bóng đá Liên chi đoàn CNTT", "Sân bóng EPU", 5, 120, "Sắp diễn ra",
                "Giải bóng đá nam giữa các chi đoàn khoa CNTT, đăng ký theo cá nhân để cổ vũ hoặc thi đấu.");
        SuKien sk12 = taoSuKien("SK012", "Seminar An toàn thông tin cho sinh viên", "Phòng hội thảo nhà E", 7, 100, "Sắp diễn ra",
                "Nhận diện lừa đảo trực tuyến, bảo vệ tài khoản và dữ liệu cá nhân.");
        SuKien sk4 = taoSuKien("SK004", "Workshop Lập trình Java Spring Boot", "Phòng máy B305", 10, 40, "Sắp diễn ra",
                "Thực hành xây dựng REST API với Spring Boot, JPA và MySQL.");
        taoSuKien("SK015", "Tham quan doanh nghiệp phần mềm", "Khu CNC Hòa Lạc", 12, 30, "Đã hủy",
                "Chuyến tham quan bị hủy do doanh nghiệp thay đổi lịch đón đoàn.");
        SuKien sk13 = taoSuKien("SK013", "Cuộc thi Hackathon EPU", "Phòng máy B301-B305", 14, 50, "Sắp diễn ra",
                "Thi lập trình 24 giờ theo đội 3-5 người, chủ đề chuyển đổi số ngành năng lượng.");
        SuKien sk5 = taoSuKien("SK005", "Chiến dịch Mùa hè xanh - tập huấn", "Hội trường nhà E", 18, 100, "Sắp diễn ra",
                "Tập huấn kỹ năng cho tình nguyện viên trước chiến dịch Mùa hè xanh.");
        SuKien sk6 = taoSuKien("SK006", "Cuộc thi Olympic Tin học", "Phòng máy B301-B303", 25, 60, "Sắp diễn ra",
                "Vòng loại cấp trường, chọn đội tuyển tham dự Olympic Tin học sinh viên toàn quốc.");
        SuKien sk14 = taoSuKien("SK014", "Giao lưu văn nghệ chào mừng 20/11", "Hội trường nhà E", 30, 250, "Sắp diễn ra",
                "Chương trình văn nghệ tri ân thầy cô nhân ngày Nhà giáo Việt Nam.");

        // hocsinh01 dùng để demo: đã tham gia 2 sự kiện cũ (đánh giá được), đăng ký 1 sự kiện sắp tới (hủy được)
        taoDangKy(sk1, "hocsinh01", DA_THAM_GIA);
        taoDangKy(sk2, "hocsinh01", DA_THAM_GIA);
        taoDangKy(sk4, "hocsinh01", DangKyController.DA_DANG_KY);
        // ...và đã đánh giá 2 sự kiện khác (để bảng "Đánh giá bạn đã gửi" có dữ liệu).
        // SK002 cố ý để chưa đánh giá, dùng thử chức năng gửi đánh giá.
        taoDangKy(sk9, "hocsinh01", DA_THAM_GIA);
        taoDangKy(sk10, "hocsinh01", DA_THAM_GIA);
        luuDanhGia(sk9, "hocsinh01", 4, "Học được cách viết CV và chuẩn bị phỏng vấn, diễn giả chia sẻ rất thực tế.", 1);
        luuDanhGia(sk10, "hocsinh01", 5, "Gặp được nhiều doanh nghiệp, mình đã nộp CV thực tập ngay tại sự kiện!", 2);

        // Các sinh viên còn lại: trạng thái sinh theo công thức cố định để lần chạy nào cũng giống nhau
        List<SuKien> daDienRa = List.of(sk8, sk9, sk10, sk1, sk2, sk11);
        List<SuKien> sapDienRa = List.of(sk3, sk12, sk4, sk13, sk5, sk6, sk14);
        for (int i = 2; i <= SO_SINH_VIEN; i++) {
            String sv = tenDangNhap(i);
            for (int j = 0; j < daDienRa.size(); j++) {
                if ((i + j) % 3 != 0) {
                    taoDangKy(daDienRa.get(j), sv, (i * (j + 1)) % 5 == 0 ? VANG_MAT : DA_THAM_GIA);
                }
            }
            for (int j = 0; j < sapDienRa.size(); j++) {
                if ((i + j) % 3 == 0) {
                    taoDangKy(sapDienRa.get(j), sv, (i + 2 * j) % 5 == 0 ? DangKyController.DA_HUY : DangKyController.DA_DANG_KY);
                }
            }
        }

        // Đánh giá: khoảng một nửa số lượt "Đã tham gia" (trừ hocsinh01 để còn tự thử gửi đánh giá)
        for (int i = 2; i <= SO_SINH_VIEN; i++) {
            for (int j = 0; j < daDienRa.size(); j++) {
                if ((i + j) % 2 == 0) {
                    taoDanhGia(daDienRa.get(j), tenDangNhap(i), 3 * i + j);
                }
            }
        }

        System.out.println("[SuKienSeeder] Đã thêm " + soSuKienMoi + " sự kiện, " + soDangKyMoi
                + " lượt đăng ký, " + soDanhGiaMoi + " đánh giá mẫu.");
    }

    private void taoHoSoSinhVien() {
        if (sinhVienRepo.count() > 0) return;
        String[] hoTen = {"Nguyễn Văn An", "Trần Thị Bình", "Lê Hoàng Cường", "Phạm Thị Dung", "Võ Minh Đức",
                "Bùi Thị Em", "Đặng Văn Phúc", "Hoàng Thị Giang", "Ngô Văn Hùng", "Vũ Thị Hương",
                "Đỗ Văn Khánh", "Nguyễn Thị Lan", "Trần Văn Minh", "Lê Thị Mai"};
        for (int i = 1; i <= hoTen.length; i++) {
            SinhVien sv = new SinhVien();
            sv.setTenDangNhap(tenDangNhap(i));
            sv.setMaSinhVien("2281" + String.format("%06d", 10000 + i));
            sv.setHoTen(hoTen[i - 1]);
            sv.setNgaySinh(LocalDate.of(2004, (i % 12) + 1, (i % 28) + 1));
            sv.setLop("D17CNPM" + ((i % 3) + 1));
            sv.setEmail(tenDangNhap(i) + "@epu.edu.vn");
            sv.setSoDienThoai("09012345" + String.format("%02d", i));
            sinhVienRepo.save(sv);
        }
        System.out.println("[SuKienSeeder] Đã tạo hồ sơ cho 14 sinh viên.");
    }

    // Đã có sự kiện cùng mã thì dùng lại, không ghi đè
    private SuKien taoSuKien(String ma, String ten, String diaDiem, int soNgayTuHomNay, int soLuong,
                             String trangThai, String moTa) {
        return suKienRepo.findByMaSuKien(ma).orElseGet(() -> {
            LocalDateTime batDau = now.plusDays(soNgayTuHomNay);
            SuKien sk = new SuKien();
            sk.setMaSuKien(ma);
            sk.setTenSuKien(ten);
            sk.setMoTa(moTa);
            sk.setDiaDiem(diaDiem);
            sk.setThoiGianBatDau(batDau);
            sk.setThoiGianKetThuc(batDau.plusHours(4));
            sk.setSoLuongToiDa(soLuong);
            sk.setDonViToChuc("Liên Chi đoàn CNTT");
            sk.setTrangThai(trangThai);
            soSuKienMoi++;
            return suKienRepo.save(sk);
        });
    }

    // Bỏ qua nếu sinh viên đã có đăng ký với sự kiện này, hoặc sự kiện đã đủ chỗ
    private void taoDangKy(SuKien sk, String tenDangNhap, String trangThai) {
        if (dangKyRepo.findBySuKienIdAndTenDangNhap(sk.getId(), tenDangNhap).isPresent()) return;
        boolean giuCho = !DangKyController.DA_HUY.equals(trangThai);
        if (giuCho && sk.getSoLuongToiDa() != null
                && dangKyRepo.countBySuKienIdAndTrangThaiNot(sk.getId(), DangKyController.DA_HUY) >= sk.getSoLuongToiDa()) {
            return;
        }

        DangKySuKien dk = new DangKySuKien();
        dk.setSuKienId(sk.getId());
        dk.setTenDangNhap(tenDangNhap);
        // Đăng ký trước ngày diễn ra vài ngày, và không muộn hơn hôm qua
        LocalDateTime ngay = sk.getThoiGianBatDau().minusDays(3 + Math.abs(tenDangNhap.hashCode()) % 5);
        dk.setNgayDangKy(ngay.isAfter(now.minusDays(1)) ? now.minusDays(1) : ngay);
        dk.setTrangThai(trangThai);
        dangKyRepo.save(dk);
        soDangKyMoi++;
    }

    private static final String[][] NHAN_XET = {
            {},
            {},
            {"Chương trình hơi dài, phần hỏi đáp chưa được nhiều.", "Âm thanh hội trường chưa tốt, khó nghe."},
            {"Nội dung ổn, mong lần sau tổ chức đúng giờ hơn.", "Khá bổ ích nhưng chỗ ngồi hơi chật."},
            {"Nội dung hay, ban tổ chức nhiệt tình.", "Bổ ích, mình học được nhiều điều mới."},
            {"Rất tuyệt vời, mong có thêm nhiều sự kiện như vậy!", "Tổ chức chu đáo, diễn giả rất có tâm."},
    };

    // Chỉ đánh giá khi sinh viên thực sự "Đã tham gia" sự kiện
    private void taoDanhGia(SuKien sk, String tenDangNhap, int hat) {
        int soSao = hat % 7 == 0 ? 2 : 3 + hat % 3; // phần lớn 3-5 sao, thỉnh thoảng 2 sao
        luuDanhGia(sk, tenDangNhap, soSao, NHAN_XET[soSao][hat % 2], 1 + hat % 2);
    }

    // Lưu 1 đánh giá, gửi sau ngày diễn ra vài ngày; bỏ qua nếu đã có hoặc sinh viên không "Đã tham gia"
    private void luuDanhGia(SuKien sk, String tenDangNhap, int soSao, String noiDung, int sauSoNgay) {
        DangKySuKien dk = dangKyRepo.findBySuKienIdAndTenDangNhap(sk.getId(), tenDangNhap).orElse(null);
        if (dk == null || !DA_THAM_GIA.equals(dk.getTrangThai())) return;
        if (danhGiaRepo.findBySuKienIdAndTenDangNhap(sk.getId(), tenDangNhap).isPresent()) return;

        DanhGiaSuKien dg = new DanhGiaSuKien();
        dg.setSuKienId(sk.getId());
        dg.setTenDangNhap(tenDangNhap);
        dg.setSoSao(soSao);
        dg.setNoiDung(noiDung);
        LocalDateTime ngay = sk.getThoiGianBatDau().plusDays(sauSoNgay);
        dg.setNgayDanhGia(ngay.isAfter(now) ? now : ngay);
        danhGiaRepo.save(dg);
        soDanhGiaMoi++;
    }

    private static String tenDangNhap(int i) {
        return "hocsinh" + String.format("%02d", i);
    }
}
