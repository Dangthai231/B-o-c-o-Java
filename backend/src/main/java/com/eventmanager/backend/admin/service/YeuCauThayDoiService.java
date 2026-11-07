package com.eventmanager.backend.admin.service;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.common.HangSo;
import com.eventmanager.backend.admin.common.LoiNghiepVu;
import com.eventmanager.backend.entity.SinhVien;
import com.eventmanager.backend.entity.TaiKhoan;
import com.eventmanager.backend.entity.YeuCauThayDoi;
import com.eventmanager.backend.repository.SinhVienRepository;
import com.eventmanager.backend.repository.TaiKhoanRepository;
import com.eventmanager.backend.repository.YeuCauThayDoiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ctr_DuyetThayDoi: Admin xem yêu cầu thay đổi thông tin, so sánh "hiện tại" và "đề xuất",
 * rồi Chấp nhận (ghi thông tin mới vào CSDL) hoặc Từ chối (bắt buộc nhập lý do).
 */
@Service
public class YeuCauThayDoiService {

    private final YeuCauThayDoiRepository yeuCauRepo;
    private final TaiKhoanRepository taiKhoanRepo;
    private final SinhVienRepository sinhVienRepo;

    public YeuCauThayDoiService(YeuCauThayDoiRepository yeuCauRepo, TaiKhoanRepository taiKhoanRepo,
                                SinhVienRepository sinhVienRepo) {
        this.yeuCauRepo = yeuCauRepo;
        this.taiKhoanRepo = taiKhoanRepo;
        this.sinhVienRepo = sinhVienRepo;
    }

    /** Bước 2-5: lấy danh sách yêu cầu. trangThai rỗng = tất cả. */
    public List<Map<String, Object>> danhSach(String trangThai) {
        List<YeuCauThayDoi> ds = DuLieu.rong(trangThai)
                ? yeuCauRepo.findAllByOrderByNgayGuiDesc()
                : yeuCauRepo.findByTrangThaiOrderByNgayGuiDesc(trangThai.trim());
        return ds.stream().map(this::tomTat).toList();
    }

    /** Bước 8-13: thông tin hiện tại và thông tin đề xuất, đặt cạnh nhau để so sánh. */
    public Map<String, Object> chiTiet(Long id) {
        YeuCauThayDoi yc = layTheoId(id);
        TaiKhoan tk = taiKhoanRepo.findByTenDangNhap(yc.getTenDangNhap()).orElse(null);
        SinhVien sv = sinhVienRepo.findByTenDangNhap(yc.getTenDangNhap()).orElse(null);

        String hoTenCu = tk != null && !DuLieu.rong(tk.getHoTen()) ? tk.getHoTen() : (sv != null ? sv.getHoTen() : "");
        String emailCu = tk != null && !DuLieu.rong(tk.getEmail()) ? tk.getEmail() : (sv != null ? sv.getEmail() : "");
        String sdtCu = tk != null && !DuLieu.rong(tk.getSoDienThoai()) ? tk.getSoDienThoai() : (sv != null ? sv.getSoDienThoai() : "");
        String lopCu = sv != null ? sv.getLop() : "";

        // Mỗi dòng: tên trường - giá trị hiện tại - giá trị đề xuất (trường nào không đổi thì bỏ qua)
        List<Map<String, Object>> soSanh = new ArrayList<>();
        themDong(soSanh, "Họ và tên", hoTenCu, yc.getHoTenMoi());
        themDong(soSanh, "Email", emailCu, yc.getEmailMoi());
        themDong(soSanh, "Số điện thoại", sdtCu, yc.getSoDienThoaiMoi());
        themDong(soSanh, "Lớp", lopCu, yc.getLopMoi());

        Map<String, Object> kq = tomTat(yc);
        kq.put("soSanh", soSanh);
        kq.put("lyDo", DuLieu.khongNull(yc.getLyDo()));
        kq.put("lyDoTuChoi", DuLieu.khongNull(yc.getLyDoTuChoi()));
        kq.put("ngayXuLy", DuLieu.dinhDang(yc.getNgayXuLy()));
        return kq;
    }

    /** Bước 16-23: Chấp nhận -> ghi thông tin mới vào tai_khoan + sinh_vien, đổi trạng thái "Đã duyệt". */
    @Transactional
    public void chapNhan(Long id) {
        YeuCauThayDoi yc = layChoDuyet(id);
        TaiKhoan tk = taiKhoanRepo.findByTenDangNhap(yc.getTenDangNhap())
                .orElseThrow(() -> new LoiNghiepVu("Tài khoản gửi yêu cầu không còn tồn tại"));

        if (!DuLieu.rong(yc.getEmailMoi())) {
            taiKhoanRepo.findByEmailIgnoreCase(yc.getEmailMoi()).ifPresent(khac -> {
                if (!khac.getId().equals(tk.getId())) {
                    throw new LoiNghiepVu("Email đề xuất đã được tài khoản khác sử dụng, hãy từ chối yêu cầu này");
                }
            });
        }

        if (!DuLieu.rong(yc.getHoTenMoi())) tk.setHoTen(yc.getHoTenMoi());
        if (!DuLieu.rong(yc.getEmailMoi())) tk.setEmail(yc.getEmailMoi());
        if (!DuLieu.rong(yc.getSoDienThoaiMoi())) tk.setSoDienThoai(yc.getSoDienThoaiMoi());
        taiKhoanRepo.save(tk);

        SinhVien sv = sinhVienRepo.findByTenDangNhap(yc.getTenDangNhap()).orElse(null);
        if (sv != null) {
            if (!DuLieu.rong(yc.getHoTenMoi())) sv.setHoTen(yc.getHoTenMoi());
            if (!DuLieu.rong(yc.getEmailMoi())) sv.setEmail(yc.getEmailMoi());
            if (!DuLieu.rong(yc.getSoDienThoaiMoi())) sv.setSoDienThoai(yc.getSoDienThoaiMoi());
            if (!DuLieu.rong(yc.getLopMoi())) sv.setLop(yc.getLopMoi());
            sinhVienRepo.save(sv);
        }

        yc.setTrangThai(HangSo.DA_DUYET);
        yc.setNgayXuLy(LocalDateTime.now());
        yeuCauRepo.save(yc);
    }

    /** Bước 24-31: Từ chối, bắt buộc có lý do. */
    @Transactional
    public void tuChoi(Long id, String lyDo) {
        if (DuLieu.rong(lyDo)) {
            throw new LoiNghiepVu("Vui lòng nhập lý do từ chối");
        }
        YeuCauThayDoi yc = layChoDuyet(id);
        yc.setTrangThai(HangSo.TU_CHOI);
        yc.setLyDoTuChoi(lyDo.trim());
        yc.setNgayXuLy(LocalDateTime.now());
        yeuCauRepo.save(yc);
    }

    /** Cho phía người dùng gửi yêu cầu (phần Sinh viên có thể gọi API này). */
    @Transactional
    public Map<String, Object> guiYeuCau(Map<String, Object> body) {
        String tenDangNhap = DuLieu.chuoi(body, "tenDangNhap");
        if (taiKhoanRepo.findByTenDangNhap(tenDangNhap).isEmpty()) {
            throw LoiNghiepVu.khongTimThay("tài khoản");
        }
        if (yeuCauRepo.existsByTenDangNhapAndTrangThai(tenDangNhap, HangSo.CHO_DUYET)) {
            throw new LoiNghiepVu("Bạn đang có một yêu cầu chờ duyệt, vui lòng đợi kết quả");
        }
        String email = DuLieu.chuoi(body, "emailMoi");
        String sdt = DuLieu.chuoi(body, "soDienThoaiMoi");
        if (!email.isEmpty() && !email.matches(HangSo.REGEX_EMAIL)) {
            throw new LoiNghiepVu("Email không đúng định dạng");
        }
        if (!sdt.isEmpty() && !sdt.matches(HangSo.REGEX_SDT)) {
            throw new LoiNghiepVu("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0");
        }
        YeuCauThayDoi yc = new YeuCauThayDoi();
        yc.setTenDangNhap(tenDangNhap);
        yc.setHoTenMoi(DuLieu.chuoi(body, "hoTenMoi"));
        yc.setEmailMoi(email);
        yc.setSoDienThoaiMoi(sdt);
        yc.setLopMoi(DuLieu.chuoi(body, "lopMoi"));
        yc.setLyDo(DuLieu.chuoi(body, "lyDo"));
        yc.setTrangThai(HangSo.CHO_DUYET);
        yc.setNgayGui(LocalDateTime.now());
        return tomTat(yeuCauRepo.save(yc));
    }

    // ==================== HÀM PHỤ ====================

    private YeuCauThayDoi layTheoId(Long id) {
        return yeuCauRepo.findById(id).orElseThrow(() -> LoiNghiepVu.khongTimThay("yêu cầu"));
    }

    private YeuCauThayDoi layChoDuyet(Long id) {
        YeuCauThayDoi yc = layTheoId(id);
        if (!HangSo.CHO_DUYET.equals(yc.getTrangThai())) {
            throw new LoiNghiepVu("Yêu cầu này đã được xử lý trước đó");
        }
        return yc;
    }

    private void themDong(List<Map<String, Object>> ds, String truong, String cu, String moi) {
        if (DuLieu.rong(moi)) return;
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("truong", truong);
        d.put("hienTai", DuLieu.khongNull(cu));
        d.put("deXuat", moi);
        ds.add(d);
    }

    private Map<String, Object> tomTat(YeuCauThayDoi yc) {
        TaiKhoan tk = taiKhoanRepo.findByTenDangNhap(yc.getTenDangNhap()).orElse(null);
        List<String> truongDoi = new ArrayList<>();
        if (!DuLieu.rong(yc.getHoTenMoi())) truongDoi.add("Họ tên");
        if (!DuLieu.rong(yc.getEmailMoi())) truongDoi.add("Email");
        if (!DuLieu.rong(yc.getSoDienThoaiMoi())) truongDoi.add("SĐT");
        if (!DuLieu.rong(yc.getLopMoi())) truongDoi.add("Lớp");

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", yc.getId());
        m.put("tenDangNhap", yc.getTenDangNhap());
        m.put("hoTen", tk != null ? DuLieu.khongNull(tk.getHoTen()) : "");
        m.put("maSo", tk != null ? DuLieu.khongNull(tk.getMaSo()) : "");
        m.put("noiDungThayDoi", String.join(", ", truongDoi));
        m.put("trangThai", yc.getTrangThai());
        m.put("ngayGui", DuLieu.dinhDang(yc.getNgayGui()));
        return m;
    }
}
