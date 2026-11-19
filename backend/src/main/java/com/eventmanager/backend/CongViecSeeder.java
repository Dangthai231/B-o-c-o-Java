package com.eventmanager.backend;

import com.eventmanager.backend.entity.CongViec;
import com.eventmanager.backend.entity.SuKien;
import com.eventmanager.backend.repository.CongViecRepository;
import com.eventmanager.backend.repository.SuKienRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tự động tạo dữ liệu mẫu cho bảng cong_viec nếu bảng đang trống,
 * giúp cán bộ có sẵn công việc để theo dõi, phân công và cập nhật trạng thái.
 */
@Component
@Order(50)
public class CongViecSeeder implements CommandLineRunner {

    private final CongViecRepository congViecRepo;
    private final SuKienRepository suKienRepo;

    public CongViecSeeder(CongViecRepository congViecRepo, SuKienRepository suKienRepo) {
        this.congViecRepo = congViecRepo;
        this.suKienRepo = suKienRepo;
    }

    @Override
    public void run(String... args) {
        if (congViecRepo.count() > 0) {
            return;
        }

        List<SuKien> dsSuKien = suKienRepo.findAll();
        SuKien sk1 = dsSuKien.size() > 0 ? dsSuKien.get(0) : null;
        SuKien sk2 = dsSuKien.size() > 1 ? dsSuKien.get(1) : sk1;
        SuKien sk3 = dsSuKien.size() > 2 ? dsSuKien.get(2) : sk1;

        LocalDate today = LocalDate.now();
        List<CongViec> list = new ArrayList<>();

        list.add(tao("CV001", "Lập kịch bản chương trình chi tiết",
                "Xây dựng timeline từng tiết mục, phân bổ thời lượng MC và khách mời",
                sk1, "Nguyễn Văn Thái (canbo01)", "Cao", today.plusDays(3), 80, "Đang thực hiện"));

        list.add(tao("CV002", "Chuẩn bị âm thanh, ánh sáng hội trường",
                "Kiểm tra micro không dây, loa hội trường và chạy thử slide trình chiếu",
                sk1, "Trần Minh Quân (canbo02)", "Cao", today.plusDays(1), 100, "Hoàn thành"));

        list.add(tao("CV003", "Thiết kế backdrop và ấn phẩm truyền thông",
                "Hoàn thiện poster, standee, banner fanpage Liên chi đoàn CNTT",
                sk2, "Lê Thị Hương (canbo03)", "Trung bình", today.plusDays(5), 55, "Đang thực hiện"));

        list.add(tao("CV004", "Chốt danh sách sinh viên đăng ký tham gia",
                "Xuất danh sách điểm danh QR và gửi email xác nhận cho sinh viên",
                sk2, "Nguyễn Văn Thái (canbo01)", "Cao", today.minusDays(1), 65, "Đang thực hiện"));

        list.add(tao("CV005", "Liên hệ diễn giả và khách mời",
                "Gửi thư mời chính thức và xác nhận lịch trình đón tiếp đại biểu",
                sk3, "Phạm Đức Anh (canbo04)", "Cao", today.plusDays(7), 100, "Hoàn thành"));

        list.add(tao("CV006", "Dự trù kinh phí và hậu cần nước uống",
                "Lập bảng dự trù chi phí in ấn, nước suối, tea-break trình BCH duyệt",
                sk3, "Hoàng Thu Trang (canbo05)", "Trung bình", today.plusDays(4), 0, "Chưa bắt đầu"));

        list.add(tao("CV007", "Phân công tình nguyện viên đón tiếp",
                "Bố trí 10 CTV trực bàn check-in, hướng dẫn chỗ ngồi cho đại biểu",
                sk1, "Vũ Quang Huy (canbo06)", "Thấp", today.plusDays(6), 25, "Tạm hoãn"));

        list.add(tao("CV008", "Tổng hợp báo cáo sau sự kiện",
                "Thu thập ảnh tư liệu, biên bản điểm danh và phiếu đánh giá sinh viên",
                sk2, "Nguyễn Văn Thái (canbo01)", "Trung bình", today.plusDays(10), 0, "Chưa bắt đầu"));

        congViecRepo.saveAll(list);
        System.out.println("[CongViecSeeder] Đã tạo " + list.size() + " công việc mẫu.");
    }

    private CongViec tao(String ma, String ten, String moTa, SuKien sk,
                         String nhanSu, String uuTien, LocalDate hanChot,
                         int tienDo, String trangThai) {
        CongViec cv = new CongViec();
        cv.setMaCv(ma);
        cv.setTenCongViec(ten);
        cv.setMoTa(moTa);
        if (sk != null) {
            cv.setSuKienId(sk.getId());
            cv.setTenSuKien(sk.getTenSuKien());
        } else {
            cv.setTenSuKien("Sự kiện Liên chi đoàn CNTT");
        }
        cv.setNhanSu(nhanSu);
        cv.setMucDoUuTien(uuTien);
        cv.setHanChot(hanChot);
        cv.setTienDo(tienDo);
        cv.setTrangThai(trangThai);
        return cv;
    }
}
