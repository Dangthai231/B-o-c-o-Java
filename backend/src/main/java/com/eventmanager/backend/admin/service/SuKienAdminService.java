package com.eventmanager.backend.admin.service;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.common.HangSo;
import com.eventmanager.backend.admin.common.LoiNghiepVu;
import com.eventmanager.backend.entity.DangKySuKien;
import com.eventmanager.backend.entity.DanhGiaSuKien;
import com.eventmanager.backend.entity.PhanCong;
import com.eventmanager.backend.entity.SuKien;
import com.eventmanager.backend.entity.TaiKhoan;
import com.eventmanager.backend.repository.DangKySuKienRepository;
import com.eventmanager.backend.repository.DanhGiaSuKienRepository;
import com.eventmanager.backend.repository.PhanCongRepository;
import com.eventmanager.backend.repository.SuKienRepository;
import com.eventmanager.backend.repository.TaiKhoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Ctr_TaoSuKien, Ctr_CapNhatSuKien, Ctr_DuyetKeHoach, Ctr_TheoDoiSuKien,
 * Ctr_PhanCongCanBo, Ctr_TraCuuSuKien.
 */
@Service
public class SuKienAdminService {

    private static final String DA_HUY_DANG_KY = "Đã hủy";
    private static final String DA_THAM_GIA = "Đã tham gia";
    private static final String VANG_MAT = "Vắng mặt";

    private final SuKienRepository suKienRepo;
    private final DangKySuKienRepository dangKyRepo;
    private final DanhGiaSuKienRepository danhGiaRepo;
    private final PhanCongRepository phanCongRepo;
    private final TaiKhoanRepository taiKhoanRepo;

    public SuKienAdminService(SuKienRepository suKienRepo, DangKySuKienRepository dangKyRepo,
                              DanhGiaSuKienRepository danhGiaRepo, PhanCongRepository phanCongRepo,
                              TaiKhoanRepository taiKhoanRepo) {
        this.suKienRepo = suKienRepo;
        this.dangKyRepo = dangKyRepo;
        this.danhGiaRepo = danhGiaRepo;
        this.phanCongRepo = phanCongRepo;
        this.taiKhoanRepo = taiKhoanRepo;
    }

    // ==================== TRA CỨU / DANH SÁCH ====================

    /**
     * Danh sách sự kiện, lọc theo từ khóa (tên, mã, địa điểm), trạng thái diễn ra, trạng thái duyệt.
     * Số sự kiện của Liên chi đoàn không nhiều nên lọc trực tiếp trong Java cho dễ đọc.
     */
    public List<Map<String, Object>> danhSach(String tuKhoa, String trangThai, String trangThaiDuyet) {
        String kw = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        List<Map<String, Object>> kq = new ArrayList<>();
        List<SuKien> ds = new ArrayList<>(suKienRepo.findAll());
        ds.sort(Comparator.comparing(SuKien::getThoiGianBatDau,
                Comparator.nullsLast(Comparator.reverseOrder())));

        for (SuKien sk : ds) {
            String timTrong = (DuLieu.khongNull(sk.getTenSuKien()) + " " + DuLieu.khongNull(sk.getMaSuKien())
                    + " " + DuLieu.khongNull(sk.getDiaDiem())).toLowerCase();
            if (!kw.isEmpty() && !timTrong.contains(kw)) continue;
            if (!DuLieu.rong(trangThai) && !trangThai.equals(sk.getTrangThai())) continue;
            if (!DuLieu.rong(trangThaiDuyet) && !trangThaiDuyet.equals(trangThaiDuyet(sk))) continue;
            kq.add(sangMap(sk));
        }
        return kq;
    }

    public Map<String, Object> chiTiet(Long id) {
        SuKien sk = layTheoId(id);
        Map<String, Object> m = sangMap(sk);
        m.put("moTa", DuLieu.khongNull(sk.getMoTa()));
        m.put("lyDoTuChoi", DuLieu.khongNull(sk.getLyDoTuChoi()));
        return m;
    }

    // ==================== TẠO / CẬP NHẬT ====================

    @Transactional
    public Map<String, Object> taoMoi(Map<String, Object> body, String nguoiTao) {
        SuKien sk = new SuKien();
        ganVaKiemTra(sk, body, true);
        sk.setMaSuKien(sinhMaSuKien());
        sk.setTrangThai(HangSo.SK_SAP_DIEN_RA);
        // Admin tự tạo thì không cần duyệt kế hoạch
        sk.setTrangThaiDuyet(HangSo.DA_DUYET);
        sk.setNguoiTao(DuLieu.rong(nguoiTao) ? HangSo.ADMIN.toLowerCase() : nguoiTao);
        return sangMap(suKienRepo.save(sk));
    }

    @Transactional
    public Map<String, Object> capNhat(Long id, Map<String, Object> body) {
        SuKien sk = layTheoId(id);
        ganVaKiemTra(sk, body, false);

        String trangThai = DuLieu.chuoi(body, "trangThai");
        if (!trangThai.isEmpty()) {
            if (!HangSo.DS_TRANG_THAI_SU_KIEN.contains(trangThai)) {
                throw new LoiNghiepVu("Trạng thái sự kiện không hợp lệ");
            }
            sk.setTrangThai(trangThai);
        }
        long daDangKy = soDangKy(sk.getId());
        if (sk.getSoLuongToiDa() != null && sk.getSoLuongToiDa() < daDangKy) {
            throw new LoiNghiepVu("Số lượng tối đa không được nhỏ hơn số người đã đăng ký (" + daDangKy + ")");
        }
        return sangMap(suKienRepo.save(sk));
    }

    // ==================== DUYỆT KẾ HOẠCH ====================

    @Transactional
    public void duyetKeHoach(Long id) {
        SuKien sk = layChoDuyet(id);
        sk.setTrangThaiDuyet(HangSo.DA_DUYET);
        sk.setLyDoTuChoi(null);
        suKienRepo.save(sk);
    }

    @Transactional
    public void tuChoiKeHoach(Long id, String lyDo) {
        if (DuLieu.rong(lyDo)) {
            throw new LoiNghiepVu("Vui lòng nhập lý do từ chối kế hoạch");
        }
        SuKien sk = layChoDuyet(id);
        sk.setTrangThaiDuyet(HangSo.TU_CHOI);
        sk.setLyDoTuChoi(lyDo.trim());
        suKienRepo.save(sk);
    }

    // ==================== THEO DÕI SỰ KIỆN ====================

    /** Thông tin và tiến độ: số đăng ký / tối đa, đã tham gia, vắng, đánh giá, cán bộ phụ trách. */
    public Map<String, Object> theoDoi(Long id) {
        SuKien sk = layTheoId(id);
        List<DangKySuKien> dsDangKy = dangKyRepo.findBySuKienId(id);
        long dangKy = dsDangKy.stream().filter(d -> !DA_HUY_DANG_KY.equals(d.getTrangThai())).count();
        long thamGia = dsDangKy.stream().filter(d -> DA_THAM_GIA.equals(d.getTrangThai())).count();
        long vang = dsDangKy.stream().filter(d -> VANG_MAT.equals(d.getTrangThai())).count();
        long huy = dsDangKy.stream().filter(d -> DA_HUY_DANG_KY.equals(d.getTrangThai())).count();

        List<DanhGiaSuKien> dsDanhGia = danhGiaRepo.findBySuKienIdOrderByNgayDanhGiaDesc(id);
        double diemTb = dsDanhGia.stream().filter(d -> d.getSoSao() != null)
                .mapToInt(DanhGiaSuKien::getSoSao).average().orElse(0);

        Map<String, Object> kq = chiTiet(id);
        kq.put("soDangKy", dangKy);
        kq.put("soThamGia", thamGia);
        kq.put("soVangMat", vang);
        kq.put("soHuy", huy);
        kq.put("soDanhGia", dsDanhGia.size());
        kq.put("diemTrungBinh", Math.round(diemTb * 10) / 10.0);
        kq.put("tienDoThoiGian", moTaThoiGian(sk));
        kq.put("phanCong", dsPhanCong(id));
        return kq;
    }

    // ==================== PHÂN CÔNG CÁN BỘ ====================

    public List<Map<String, Object>> dsPhanCong(Long suKienId) {
        List<Map<String, Object>> kq = new ArrayList<>();
        for (PhanCong pc : phanCongRepo.findBySuKienIdOrderByNgayPhanCongAsc(suKienId)) {
            TaiKhoan tk = taiKhoanRepo.findByTenDangNhap(pc.getTenDangNhap()).orElse(null);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", pc.getId());
            m.put("tenDangNhap", pc.getTenDangNhap());
            m.put("hoTen", tk != null ? DuLieu.khongNull(tk.getHoTen()) : pc.getTenDangNhap());
            m.put("email", tk != null ? DuLieu.khongNull(tk.getEmail()) : "");
            m.put("vaiTroPhuTrach", DuLieu.khongNull(pc.getVaiTroPhuTrach()));
            m.put("ngayPhanCong", DuLieu.dinhDang(pc.getNgayPhanCong()));
            kq.add(m);
        }
        return kq;
    }

    @Transactional
    public void phanCong(Long suKienId, String tenDangNhap, String vaiTroPhuTrach) {
        SuKien sk = layTheoId(suKienId);
        if (HangSo.SK_DA_HUY.equals(sk.getTrangThai()) || HangSo.SK_DA_KET_THUC.equals(sk.getTrangThai())) {
            throw new LoiNghiepVu("Không thể phân công cho sự kiện đã kết thúc hoặc đã hủy");
        }
        TaiKhoan tk = taiKhoanRepo.findByTenDangNhap(tenDangNhap)
                .orElseThrow(() -> LoiNghiepVu.khongTimThay("cán bộ"));
        if (!HangSo.CAN_BO.equals(tk.getVaiTro())) {
            throw new LoiNghiepVu("Chỉ phân công được tài khoản có vai trò Cán bộ sự kiện");
        }
        if (HangSo.TK_BI_KHOA.equals(tk.getTrangThai())) {
            throw new LoiNghiepVu("Tài khoản cán bộ này đang bị khóa");
        }
        if (phanCongRepo.existsBySuKienIdAndTenDangNhap(suKienId, tenDangNhap)) {
            throw new LoiNghiepVu("Cán bộ này đã được phân công cho sự kiện");
        }
        if (DuLieu.rong(vaiTroPhuTrach)) {
            throw new LoiNghiepVu("Vui lòng nhập nhiệm vụ phụ trách");
        }
        PhanCong pc = new PhanCong();
        pc.setSuKienId(suKienId);
        pc.setTenDangNhap(tenDangNhap);
        pc.setVaiTroPhuTrach(vaiTroPhuTrach.trim());
        pc.setNgayPhanCong(LocalDateTime.now());
        phanCongRepo.save(pc);
    }

    @Transactional
    public void huyPhanCong(Long phanCongId) {
        if (!phanCongRepo.existsById(phanCongId)) {
            throw LoiNghiepVu.khongTimThay("phân công");
        }
        phanCongRepo.deleteById(phanCongId);
    }

    // ==================== HÀM PHỤ ====================

    public SuKien layTheoId(Long id) {
        return suKienRepo.findById(id).orElseThrow(() -> LoiNghiepVu.khongTimThay("sự kiện"));
    }

    /** Sự kiện cũ (chưa có cột duyệt) được coi là "Đã duyệt". */
    public static String trangThaiDuyet(SuKien sk) {
        return DuLieu.rong(sk.getTrangThaiDuyet()) ? HangSo.DA_DUYET : sk.getTrangThaiDuyet();
    }

    private SuKien layChoDuyet(Long id) {
        SuKien sk = layTheoId(id);
        if (!HangSo.CHO_DUYET.equals(trangThaiDuyet(sk))) {
            throw new LoiNghiepVu("Kế hoạch này không ở trạng thái chờ duyệt");
        }
        return sk;
    }

    private long soDangKy(Long suKienId) {
        return dangKyRepo.countBySuKienIdAndTrangThaiNot(suKienId, DA_HUY_DANG_KY);
    }

    /** Kiểm tra dữ liệu nhập (bước "Kiểm tra dữ liệu" trong biểu đồ) rồi gán vào entity. */
    private void ganVaKiemTra(SuKien sk, Map<String, Object> body, boolean laTaoMoi) {
        String ten = DuLieu.chuoi(body, "tenSuKien");
        String diaDiem = DuLieu.chuoi(body, "diaDiem");
        if (ten.isEmpty()) throw new LoiNghiepVu("Tên sự kiện không được để trống");
        if (ten.length() > 200) throw new LoiNghiepVu("Tên sự kiện tối đa 200 ký tự");
        if (diaDiem.isEmpty()) throw new LoiNghiepVu("Địa điểm không được để trống");

        LocalDateTime batDau = DuLieu.docNgayGio(DuLieu.chuoi(body, "thoiGianBatDau"), "Thời gian bắt đầu");
        LocalDateTime ketThuc = DuLieu.docNgayGio(DuLieu.chuoi(body, "thoiGianKetThuc"), "Thời gian kết thúc");
        if (!ketThuc.isAfter(batDau)) {
            throw new LoiNghiepVu("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if (laTaoMoi && batDau.isBefore(LocalDateTime.now())) {
            throw new LoiNghiepVu("Thời gian bắt đầu phải ở tương lai");
        }
        int soLuong = DuLieu.docSo(DuLieu.chuoi(body, "soLuongToiDa"), "Số lượng tối đa");
        if (soLuong <= 0) throw new LoiNghiepVu("Số lượng tối đa phải lớn hơn 0");

        sk.setTenSuKien(ten);
        sk.setDiaDiem(diaDiem);
        sk.setThoiGianBatDau(batDau);
        sk.setThoiGianKetThuc(ketThuc);
        sk.setSoLuongToiDa(soLuong);
        sk.setDonViToChuc(DuLieu.chuoi(body, "donViToChuc"));
        sk.setMoTa(DuLieu.chuoi(body, "moTa"));
    }

    private String sinhMaSuKien() {
        Set<String> daCo = new HashSet<>();
        suKienRepo.findAll().forEach(s -> daCo.add(s.getMaSuKien()));
        int so = 1;
        while (daCo.contains(String.format("SK%03d", so))) so++;
        return String.format("SK%03d", so);
    }

    private String moTaThoiGian(SuKien sk) {
        LocalDateTime now = LocalDateTime.now();
        if (HangSo.SK_DA_HUY.equals(sk.getTrangThai())) return "Sự kiện đã hủy";
        if (sk.getThoiGianBatDau() == null || sk.getThoiGianKetThuc() == null) return "";
        if (now.isBefore(sk.getThoiGianBatDau())) {
            long ngay = Duration.between(now, sk.getThoiGianBatDau()).toDays();
            return ngay == 0 ? "Diễn ra trong hôm nay" : "Còn " + ngay + " ngày nữa diễn ra";
        }
        if (now.isAfter(sk.getThoiGianKetThuc())) return "Đã diễn ra xong";
        return "Đang diễn ra";
    }

    private Map<String, Object> sangMap(SuKien sk) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", sk.getId());
        m.put("maSuKien", DuLieu.khongNull(sk.getMaSuKien()));
        m.put("tenSuKien", DuLieu.khongNull(sk.getTenSuKien()));
        m.put("diaDiem", DuLieu.khongNull(sk.getDiaDiem()));
        m.put("thoiGianBatDau", DuLieu.dinhDang(sk.getThoiGianBatDau()));
        m.put("thoiGianKetThuc", DuLieu.dinhDang(sk.getThoiGianKetThuc()));
        m.put("soLuongToiDa", sk.getSoLuongToiDa() == null ? 0 : sk.getSoLuongToiDa());
        m.put("soDangKy", soDangKy(sk.getId()));
        m.put("soCanBo", phanCongRepo.countBySuKienId(sk.getId()));
        m.put("donViToChuc", DuLieu.khongNull(sk.getDonViToChuc()));
        m.put("trangThai", DuLieu.khongNull(sk.getTrangThai()));
        m.put("trangThaiDuyet", trangThaiDuyet(sk));
        m.put("nguoiTao", DuLieu.khongNull(sk.getNguoiTao()));
        return m;
    }
}
