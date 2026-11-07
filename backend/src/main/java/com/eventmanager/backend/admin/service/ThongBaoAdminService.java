package com.eventmanager.backend.admin.service;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.common.HangSo;
import com.eventmanager.backend.admin.common.LoiNghiepVu;
import com.eventmanager.backend.entity.ThongBao;
import com.eventmanager.backend.repository.ThongBaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Ctr_TaoThongBao, Ctr_DuyetTB, Ctr_DangTB, Ctr_CapNhatTB, Ctr_QuanLyTB.
 *
 * Vòng đời một thông báo:
 *   Chờ duyệt --(Duyệt)--> Đã duyệt --(Đăng)--> Đã đăng --(Gỡ)--> Đã gỡ
 *        \--(Từ chối)--> Từ chối --(Sửa lại)--> Chờ duyệt
 */
@Service
public class ThongBaoAdminService {

    private final ThongBaoRepository repo;

    public ThongBaoAdminService(ThongBaoRepository repo) {
        this.repo = repo;
    }

    /** "Đã gửi" là trạng thái phần Cán bộ đang dùng -> Admin hiểu là đang chờ duyệt. */
    public static String trangThaiChuan(ThongBao tb) {
        String tt = tb.getTrangThai();
        if (DuLieu.rong(tt) || HangSo.TB_DA_GUI.equals(tt)) return HangSo.CHO_DUYET;
        return tt;
    }

    public List<Map<String, Object>> danhSach(String trangThai, String tuKhoa) {
        String kw = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        List<ThongBao> ds = new ArrayList<>(repo.findAll());
        ds.sort(Comparator.comparing(ThongBao::getId).reversed());

        List<Map<String, Object>> kq = new ArrayList<>();
        for (ThongBao tb : ds) {
            if (!DuLieu.rong(trangThai) && !trangThai.equals(trangThaiChuan(tb))) continue;
            String timTrong = (DuLieu.khongNull(tb.getTieuDe()) + " " + DuLieu.khongNull(tb.getMaThongBao())
                    + " " + DuLieu.khongNull(tb.getDoiTuong())).toLowerCase();
            if (!kw.isEmpty() && !timTrong.contains(kw)) continue;
            kq.add(sangMap(tb));
        }
        return kq;
    }

    /** Đếm theo từng trạng thái -> hiển thị số trên các tab. */
    public Map<String, Long> demTheoTrangThai() {
        Map<String, Long> dem = new LinkedHashMap<>();
        for (String tt : List.of(HangSo.CHO_DUYET, HangSo.DA_DUYET, HangSo.TB_DA_DANG, HangSo.TU_CHOI, HangSo.TB_DA_GO)) {
            dem.put(tt, 0L);
        }
        for (ThongBao tb : repo.findAll()) {
            dem.merge(trangThaiChuan(tb), 1L, Long::sum);
        }
        return dem;
    }

    public Map<String, Object> chiTiet(Long id) {
        return sangMap(layTheoId(id));
    }

    @Transactional
    public Map<String, Object> taoMoi(Map<String, Object> body, String nguoiTao) {
        ThongBao tb = new ThongBao();
        ganVaKiemTra(tb, body);
        tb.setMaThongBao(sinhMa());
        tb.setTrangThai(HangSo.CHO_DUYET);
        tb.setNgayGui(LocalDate.now());
        tb.setNguoiTao(DuLieu.rong(nguoiTao) ? "admin" : nguoiTao);
        return sangMap(repo.save(tb));
    }

    @Transactional
    public Map<String, Object> capNhat(Long id, Map<String, Object> body) {
        ThongBao tb = layTheoId(id);
        if (HangSo.TB_DA_GO.equals(trangThaiChuan(tb))) {
            throw new LoiNghiepVu("Thông báo đã gỡ, không thể chỉnh sửa");
        }
        ganVaKiemTra(tb, body);
        // Thông báo bị từ chối, sửa xong sẽ quay lại hàng chờ duyệt
        if (HangSo.TU_CHOI.equals(trangThaiChuan(tb))) {
            tb.setTrangThai(HangSo.CHO_DUYET);
            tb.setLyDoTuChoi(null);
        }
        return sangMap(repo.save(tb));
    }

    @Transactional
    public void duyet(Long id) {
        ThongBao tb = layVaKiemTra(id, HangSo.CHO_DUYET, "Chỉ duyệt được thông báo đang chờ duyệt");
        tb.setTrangThai(HangSo.DA_DUYET);
        repo.save(tb);
    }

    @Transactional
    public void tuChoi(Long id, String lyDo) {
        if (DuLieu.rong(lyDo)) throw new LoiNghiepVu("Vui lòng nhập lý do từ chối");
        ThongBao tb = layVaKiemTra(id, HangSo.CHO_DUYET, "Chỉ từ chối được thông báo đang chờ duyệt");
        tb.setTrangThai(HangSo.TU_CHOI);
        tb.setLyDoTuChoi(lyDo.trim());
        repo.save(tb);
    }

    @Transactional
    public void dang(Long id) {
        ThongBao tb = layVaKiemTra(id, HangSo.DA_DUYET, "Chỉ đăng được thông báo đã duyệt");
        tb.setTrangThai(HangSo.TB_DA_DANG);
        tb.setNgayDang(LocalDateTime.now());
        repo.save(tb);
    }

    @Transactional
    public void go(Long id) {
        ThongBao tb = layVaKiemTra(id, HangSo.TB_DA_DANG, "Chỉ gỡ được thông báo đang đăng");
        tb.setTrangThai(HangSo.TB_DA_GO);
        repo.save(tb);
    }

    // ==================== HÀM PHỤ ====================

    private ThongBao layTheoId(Long id) {
        return repo.findById(id).orElseThrow(() -> LoiNghiepVu.khongTimThay("thông báo"));
    }

    private ThongBao layVaKiemTra(Long id, String trangThaiCan, String loi) {
        ThongBao tb = layTheoId(id);
        if (!trangThaiCan.equals(trangThaiChuan(tb))) throw new LoiNghiepVu(loi);
        return tb;
    }

    private void ganVaKiemTra(ThongBao tb, Map<String, Object> body) {
        String tieuDe = DuLieu.chuoi(body, "tieuDe");
        String noiDung = DuLieu.chuoi(body, "noiDung");
        String doiTuong = DuLieu.chuoi(body, "doiTuong");
        if (tieuDe.isEmpty()) throw new LoiNghiepVu("Tiêu đề không được để trống");
        if (tieuDe.length() > 200) throw new LoiNghiepVu("Tiêu đề tối đa 200 ký tự");
        if (noiDung.isEmpty()) throw new LoiNghiepVu("Nội dung không được để trống");
        if (doiTuong.isEmpty()) throw new LoiNghiepVu("Vui lòng chọn đối tượng nhận");
        tb.setTieuDe(tieuDe);
        tb.setNoiDung(noiDung);
        tb.setDoiTuong(doiTuong);
    }

    private String sinhMa() {
        int so = (int) repo.count() + 1;
        while (repo.existsByMaThongBao(String.format("TB%03d", so))) so++;
        return String.format("TB%03d", so);
    }

    private Map<String, Object> sangMap(ThongBao tb) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", tb.getId());
        m.put("maThongBao", DuLieu.khongNull(tb.getMaThongBao()));
        m.put("tieuDe", DuLieu.khongNull(tb.getTieuDe()));
        m.put("noiDung", DuLieu.khongNull(tb.getNoiDung()));
        m.put("doiTuong", DuLieu.khongNull(tb.getDoiTuong()));
        m.put("ngayGui", tb.getNgayGui() == null ? "" : tb.getNgayGui().format(DuLieu.NGAY));
        m.put("ngayDang", DuLieu.dinhDang(tb.getNgayDang()));
        m.put("nguoiTao", DuLieu.rong(tb.getNguoiTao()) ? "Cán bộ" : tb.getNguoiTao());
        m.put("trangThai", trangThaiChuan(tb));
        m.put("lyDoTuChoi", DuLieu.khongNull(tb.getLyDoTuChoi()));
        return m;
    }
}
