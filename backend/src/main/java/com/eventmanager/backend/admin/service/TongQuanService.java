package com.eventmanager.backend.admin.service;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.common.HangSo;
import com.eventmanager.backend.entity.DangKySuKien;
import com.eventmanager.backend.entity.SuKien;
import com.eventmanager.backend.entity.ThongBao;
import com.eventmanager.backend.repository.DangKySuKienRepository;
import com.eventmanager.backend.repository.DanhGiaSuKienRepository;
import com.eventmanager.backend.repository.SuKienRepository;
import com.eventmanager.backend.repository.TaiKhoanRepository;
import com.eventmanager.backend.repository.ThongBaoRepository;
import com.eventmanager.backend.repository.YeuCauThayDoiRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Số liệu cho trang "Tổng quan" và trang "Báo cáo" của Admin.
 */
@Service
public class TongQuanService {

    private final TaiKhoanRepository taiKhoanRepo;
    private final SuKienRepository suKienRepo;
    private final ThongBaoRepository thongBaoRepo;
    private final YeuCauThayDoiRepository yeuCauRepo;
    private final DangKySuKienRepository dangKyRepo;
    private final DanhGiaSuKienRepository danhGiaRepo;

    public TongQuanService(TaiKhoanRepository taiKhoanRepo, SuKienRepository suKienRepo,
                           ThongBaoRepository thongBaoRepo, YeuCauThayDoiRepository yeuCauRepo,
                           DangKySuKienRepository dangKyRepo, DanhGiaSuKienRepository danhGiaRepo) {
        this.taiKhoanRepo = taiKhoanRepo;
        this.suKienRepo = suKienRepo;
        this.thongBaoRepo = thongBaoRepo;
        this.yeuCauRepo = yeuCauRepo;
        this.dangKyRepo = dangKyRepo;
        this.danhGiaRepo = danhGiaRepo;
    }

    public Map<String, Object> tongQuan() {
        List<SuKien> dsSuKien = suKienRepo.findAll();
        List<ThongBao> dsThongBao = thongBaoRepo.findAll();
        LocalDateTime now = LocalDateTime.now();

        long keHoachCho = dsSuKien.stream()
                .filter(s -> HangSo.CHO_DUYET.equals(SuKienAdminService.trangThaiDuyet(s))).count();
        long thongBaoCho = dsThongBao.stream()
                .filter(t -> HangSo.CHO_DUYET.equals(ThongBaoAdminService.trangThaiChuan(t))).count();
        long yeuCauCho = yeuCauRepo.countByTrangThai(HangSo.CHO_DUYET);
        long sapDienRa = dsSuKien.stream()
                .filter(s -> HangSo.DA_DUYET.equals(SuKienAdminService.trangThaiDuyet(s)))
                .filter(s -> s.getThoiGianBatDau() != null && s.getThoiGianBatDau().isAfter(now))
                .filter(s -> !HangSo.SK_DA_HUY.equals(s.getTrangThai()))
                .count();

        // 5 sự kiện sắp diễn ra gần nhất
        List<Map<String, Object>> sapToi = new ArrayList<>();
        dsSuKien.stream()
                .filter(s -> HangSo.DA_DUYET.equals(SuKienAdminService.trangThaiDuyet(s)))
                .filter(s -> s.getThoiGianBatDau() != null && s.getThoiGianBatDau().isAfter(now))
                .filter(s -> !HangSo.SK_DA_HUY.equals(s.getTrangThai()))
                .sorted(Comparator.comparing(SuKien::getThoiGianBatDau))
                .limit(5)
                .forEach(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.getId());
                    m.put("tenSuKien", s.getTenSuKien());
                    m.put("thoiGianBatDau", DuLieu.dinhDang(s.getThoiGianBatDau()));
                    m.put("diaDiem", DuLieu.khongNull(s.getDiaDiem()));
                    m.put("soDangKy", dangKyRepo.countBySuKienIdAndTrangThaiNot(s.getId(), "Đã hủy"));
                    m.put("soLuongToiDa", s.getSoLuongToiDa() == null ? 0 : s.getSoLuongToiDa());
                    sapToi.add(m);
                });

        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("tongTaiKhoan", taiKhoanRepo.count());
        kq.put("tongSuKien", dsSuKien.size());
        kq.put("suKienSapDienRa", sapDienRa);
        kq.put("thongBaoDaDang", dsThongBao.stream()
                .filter(t -> HangSo.TB_DA_DANG.equals(ThongBaoAdminService.trangThaiChuan(t))).count());
        kq.put("keHoachChoDuyet", keHoachCho);
        kq.put("thongBaoChoDuyet", thongBaoCho);
        kq.put("yeuCauChoDuyet", yeuCauCho);
        kq.put("tongViecCanXuLy", keHoachCho + thongBaoCho + yeuCauCho);
        kq.put("suKienSapToi", sapToi);
        return kq;
    }

    public Map<String, Object> baoCao() {
        List<SuKien> dsSuKien = suKienRepo.findAll();
        List<DangKySuKien> dsDangKy = dangKyRepo.findAll();

        // 1. Số sự kiện theo 6 tháng gần nhất (tính theo tháng bắt đầu)
        YearMonth thangNay = YearMonth.now();
        List<Map<String, Object>> theoThang = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = thangNay.minusMonths(i);
            long dem = dsSuKien.stream().filter(s -> s.getThoiGianBatDau() != null
                    && YearMonth.from(s.getThoiGianBatDau()).equals(ym)).count();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("nhan", "T" + ym.getMonthValue() + "/" + (ym.getYear() % 100));
            m.put("giaTri", dem);
            theoThang.add(m);
        }

        // 2. Top 5 sự kiện nhiều lượt đăng ký nhất
        Map<Long, Long> demDangKy = new HashMap<>();
        for (DangKySuKien dk : dsDangKy) {
            if (!"Đã hủy".equals(dk.getTrangThai())) demDangKy.merge(dk.getSuKienId(), 1L, Long::sum);
        }
        List<Map<String, Object>> top = new ArrayList<>();
        dsSuKien.stream()
                .sorted((a, b) -> Long.compare(demDangKy.getOrDefault(b.getId(), 0L), demDangKy.getOrDefault(a.getId(), 0L)))
                .limit(5)
                .forEach(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("nhan", s.getTenSuKien());
                    m.put("giaTri", demDangKy.getOrDefault(s.getId(), 0L));
                    m.put("toiDa", s.getSoLuongToiDa() == null ? 0 : s.getSoLuongToiDa());
                    top.add(m);
                });

        // 3. Tỉ lệ tham gia trên các lượt đăng ký của sự kiện đã kết thúc
        long thamGia = dsDangKy.stream().filter(d -> "Đã tham gia".equals(d.getTrangThai())).count();
        long vang = dsDangKy.stream().filter(d -> "Vắng mặt".equals(d.getTrangThai())).count();
        double diemTb = danhGiaRepo.findAll().stream().filter(d -> d.getSoSao() != null)
                .mapToInt(d -> d.getSoSao()).average().orElse(0);

        Map<String, Long> theoVaiTro = new LinkedHashMap<>();
        for (String vt : HangSo.DS_VAI_TRO) theoVaiTro.put(HangSo.tenVaiTro(vt), taiKhoanRepo.countByVaiTro(vt));

        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("suKienTheoThang", theoThang);
        kq.put("topDangKy", top);
        kq.put("tongDangKy", dsDangKy.stream().filter(d -> !"Đã hủy".equals(d.getTrangThai())).count());
        kq.put("soThamGia", thamGia);
        kq.put("soVangMat", vang);
        kq.put("diemDanhGiaTb", Math.round(diemTb * 10) / 10.0);
        kq.put("taiKhoanTheoVaiTro", theoVaiTro);
        return kq;
    }
}
