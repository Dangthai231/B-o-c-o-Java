package com.eventmanager.backend.admin.service;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.common.HangSo;
import com.eventmanager.backend.admin.common.LoiNghiepVu;
import com.eventmanager.backend.entity.SinhVien;
import com.eventmanager.backend.entity.TaiKhoan;
import com.eventmanager.backend.repository.DangKySuKienRepository;
import com.eventmanager.backend.repository.SinhVienRepository;
import com.eventmanager.backend.repository.TaiKhoanQuyenRepository;
import com.eventmanager.backend.repository.TaiKhoanRepository;
import com.eventmanager.backend.repository.YeuCauThayDoiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ctr_Taotaikhoan + Ctr_Tracuu + Ctr_QuanLyTrangThaiTK trong biểu đồ:
 * tạo tài khoản, tra cứu tài khoản, khóa / mở khóa, cập nhật, đặt lại mật khẩu, xóa.
 */
@Service
public class TaiKhoanAdminService {

    private final TaiKhoanRepository taiKhoanRepo;
    private final SinhVienRepository sinhVienRepo;
    private final DangKySuKienRepository dangKyRepo;
    private final TaiKhoanQuyenRepository taiKhoanQuyenRepo;
    private final YeuCauThayDoiRepository yeuCauRepo;

    public TaiKhoanAdminService(TaiKhoanRepository taiKhoanRepo, SinhVienRepository sinhVienRepo,
                                DangKySuKienRepository dangKyRepo, TaiKhoanQuyenRepository taiKhoanQuyenRepo,
                                YeuCauThayDoiRepository yeuCauRepo) {
        this.taiKhoanRepo = taiKhoanRepo;
        this.sinhVienRepo = sinhVienRepo;
        this.dangKyRepo = dangKyRepo;
        this.taiKhoanQuyenRepo = taiKhoanQuyenRepo;
        this.yeuCauRepo = yeuCauRepo;
    }

    // ==================== 1. TRA CỨU ====================

    /** Danh sách có phân trang. trang bắt đầu từ 0. */
    public Map<String, Object> timKiem(String tuKhoa, String vaiTro, String trangThai, int trang, int kichThuoc) {
        int size = Math.max(1, Math.min(kichThuoc, 100));
        Page<TaiKhoan> page = taiKhoanRepo.timKiem(
                tuKhoa == null ? "" : tuKhoa.trim(),
                vaiTro == null ? "" : vaiTro.trim(),
                trangThai == null ? "" : trangThai.trim(),
                PageRequest.of(Math.max(trang, 0), size, Sort.by("id").ascending()));

        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("noiDung", page.getContent().stream().map(this::sangMap).toList());
        kq.put("tongSo", page.getTotalElements());
        kq.put("trang", page.getNumber());
        kq.put("tongTrang", page.getTotalPages());
        kq.put("kichThuoc", size);
        return kq;
    }

    /** Số liệu cho 4 thẻ phía trên màn hình. */
    public Map<String, Object> thongKe() {
        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("tongTaiKhoan", taiKhoanRepo.count());
        kq.put("dangHoatDong", taiKhoanRepo.countByTrangThai(HangSo.TK_HOAT_DONG));
        kq.put("biKhoa", taiKhoanRepo.countByTrangThai(HangSo.TK_BI_KHOA));
        kq.put("choDuyet", yeuCauRepo.countByTrangThai(HangSo.CHO_DUYET));
        return kq;
    }

    public Map<String, Object> chiTiet(Long id) {
        return sangMap(layTheoId(id));
    }

    // ==================== 2. TẠO TÀI KHOẢN ====================

    @Transactional
    public Map<String, Object> taoMoi(Map<String, Object> body) {
        String tenDangNhap = DuLieu.chuoi(body, "tenDangNhap");
        String matKhau = DuLieu.chuoi(body, "matKhau");
        String vaiTro = DuLieu.chuoi(body, "vaiTro");

        // Bước 6: kiểm tra trường bắt buộc và định dạng
        if (!tenDangNhap.matches(HangSo.REGEX_TEN_DANG_NHAP)) {
            throw new LoiNghiepVu("Tên đăng nhập gồm 3-30 ký tự: chữ không dấu, số, dấu chấm hoặc gạch dưới");
        }
        if (matKhau.length() < 6) {
            throw new LoiNghiepVu("Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (!HangSo.DS_VAI_TRO.contains(vaiTro)) {
            throw new LoiNghiepVu("Vai trò không hợp lệ");
        }
        kiemTraThongTinCaNhan(body, null);

        // Bước 7: kiểm tra tên đăng nhập / email đã tồn tại
        if (taiKhoanRepo.existsByTenDangNhap(tenDangNhap)) {
            throw new LoiNghiepVu("Tên đăng nhập \"" + tenDangNhap + "\" đã tồn tại");
        }

        // Bước 11: lưu vào CSDL
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(tenDangNhap);
        tk.setMatKhau(matKhau);
        tk.setVaiTro(vaiTro);
        ganThongTinCaNhan(tk, body);
        tk.setTrangThai(HangSo.TK_HOAT_DONG);
        tk.setNgayTao(LocalDateTime.now());
        taiKhoanRepo.save(tk);

        dongBoHoSoSinhVien(tk);
        return sangMap(tk);
    }

    // ==================== 3. CẬP NHẬT THÔNG TIN ====================

    @Transactional
    public Map<String, Object> capNhat(Long id, Map<String, Object> body) {
        TaiKhoan tk = layTheoId(id);
        kiemTraThongTinCaNhan(body, tk.getId());
        ganThongTinCaNhan(tk, body);
        taiKhoanRepo.save(tk);
        dongBoHoSoSinhVien(tk);
        return sangMap(tk);
    }

    // ==================== 4. KHÓA / MỞ KHÓA ====================

    @Transactional
    public Map<String, Object> doiTrangThai(Long id, String trangThaiMoi) {
        TaiKhoan tk = layTheoId(id);
        if (!HangSo.TK_HOAT_DONG.equals(trangThaiMoi) && !HangSo.TK_BI_KHOA.equals(trangThaiMoi)) {
            throw new LoiNghiepVu("Trạng thái không hợp lệ");
        }
        if (HangSo.TK_BI_KHOA.equals(trangThaiMoi) && HangSo.ADMIN.equals(tk.getVaiTro())) {
            throw new LoiNghiepVu("Không thể khóa tài khoản Quản trị viên");
        }
        tk.setTrangThai(trangThaiMoi);
        taiKhoanRepo.save(tk);
        return sangMap(tk);
    }

    // ==================== 5. ĐẶT LẠI MẬT KHẨU / ĐỔI MẬT KHẨU / XÓA ====================

    @Transactional
    public void datLaiMatKhau(Long id, String matKhauMoi) {
        if (matKhauMoi == null || matKhauMoi.trim().length() < 6) {
            throw new LoiNghiepVu("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        TaiKhoan tk = layTheoId(id);
        tk.setMatKhau(matKhauMoi.trim());
        taiKhoanRepo.save(tk);
    }

    @Transactional
    public void doiMatKhau(String tenDangNhap, String matKhauCu, String matKhauMoi) {
        TaiKhoan tk = taiKhoanRepo.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> LoiNghiepVu.khongTimThay("tài khoản"));
        if (!tk.getMatKhau().equals(matKhauCu)) {
            throw new LoiNghiepVu("Mật khẩu hiện tại không đúng");
        }
        if (matKhauMoi == null || matKhauMoi.trim().length() < 6) {
            throw new LoiNghiepVu("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        if (matKhauMoi.equals(matKhauCu)) {
            throw new LoiNghiepVu("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        tk.setMatKhau(matKhauMoi.trim());
        taiKhoanRepo.save(tk);
    }

    @Transactional
    public void xoa(Long id) {
        TaiKhoan tk = layTheoId(id);
        if (HangSo.ADMIN.equals(tk.getVaiTro())) {
            throw new LoiNghiepVu("Không thể xóa tài khoản Quản trị viên");
        }
        if (dangKyRepo.existsByTenDangNhap(tk.getTenDangNhap())) {
            throw new LoiNghiepVu("Tài khoản đã có dữ liệu đăng ký sự kiện. Hãy khóa tài khoản thay vì xóa.");
        }
        taiKhoanQuyenRepo.deleteByTaiKhoanId(tk.getId());
        taiKhoanRepo.delete(tk);
    }

    // ==================== HÀM PHỤ ====================

    public TaiKhoan layTheoId(Long id) {
        return taiKhoanRepo.findById(id).orElseThrow(() -> LoiNghiepVu.khongTimThay("tài khoản"));
    }

    /** idBoQua: khi cập nhật thì email trùng với chính mình vẫn hợp lệ. */
    private void kiemTraThongTinCaNhan(Map<String, Object> body, Long idBoQua) {
        String hoTen = DuLieu.chuoi(body, "hoTen");
        String email = DuLieu.chuoi(body, "email");
        String sdt = DuLieu.chuoi(body, "soDienThoai");

        if (hoTen.isEmpty()) {
            throw new LoiNghiepVu("Họ và tên không được để trống");
        }
        if (!email.matches(HangSo.REGEX_EMAIL)) {
            throw new LoiNghiepVu("Email không đúng định dạng (VD: ten@epu.edu.vn)");
        }
        if (!sdt.isEmpty() && !sdt.matches(HangSo.REGEX_SDT)) {
            throw new LoiNghiepVu("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0");
        }
        taiKhoanRepo.findByEmailIgnoreCase(email).ifPresent(khac -> {
            if (!khac.getId().equals(idBoQua)) {
                throw new LoiNghiepVu("Email \"" + email + "\" đã được dùng cho tài khoản khác");
            }
        });
    }

    private void ganThongTinCaNhan(TaiKhoan tk, Map<String, Object> body) {
        tk.setHoTen(DuLieu.chuoi(body, "hoTen"));
        tk.setEmail(DuLieu.chuoi(body, "email"));
        tk.setMaSo(DuLieu.chuoi(body, "maSo"));
        tk.setSoDienThoai(DuLieu.chuoi(body, "soDienThoai"));
    }

    /** Tài khoản sinh viên thì giữ hồ sơ bảng sinh_vien khớp với tài khoản (phần Sinh viên đang đọc bảng này). */
    private void dongBoHoSoSinhVien(TaiKhoan tk) {
        if (!HangSo.HOC_SINH.equals(tk.getVaiTro())) {
            return;
        }
        SinhVien sv = sinhVienRepo.findByTenDangNhap(tk.getTenDangNhap()).orElseGet(() -> {
            SinhVien moi = new SinhVien();
            moi.setTenDangNhap(tk.getTenDangNhap());
            return moi;
        });
        sv.setHoTen(tk.getHoTen());
        sv.setEmail(tk.getEmail());
        sv.setMaSinhVien(tk.getMaSo());
        sv.setSoDienThoai(tk.getSoDienThoai());
        sinhVienRepo.save(sv);
    }

    /** Đổi entity -> Map để trả JSON. KHÔNG bao giờ trả mật khẩu ra ngoài. */
    public Map<String, Object> sangMap(TaiKhoan tk) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", tk.getId());
        m.put("tenDangNhap", tk.getTenDangNhap());
        m.put("hoTen", DuLieu.khongNull(tk.getHoTen()));
        m.put("email", DuLieu.khongNull(tk.getEmail()));
        m.put("maSo", DuLieu.khongNull(tk.getMaSo()));
        m.put("soDienThoai", DuLieu.khongNull(tk.getSoDienThoai()));
        m.put("vaiTro", tk.getVaiTro());
        m.put("tenVaiTro", HangSo.tenVaiTro(tk.getVaiTro()));
        m.put("trangThai", tk.getTrangThai() == null ? HangSo.TK_HOAT_DONG : tk.getTrangThai());
        m.put("ngayTao", DuLieu.dinhDang(tk.getNgayTao()));
        return m;
    }

    public List<Map<String, Object>> dsCanBoHoatDong() {
        return taiKhoanRepo.findByVaiTroOrderByHoTenAsc(HangSo.CAN_BO).stream()
                .filter(t -> !HangSo.TK_BI_KHOA.equals(t.getTrangThai()))
                .map(this::sangMap)
                .toList();
    }
}
