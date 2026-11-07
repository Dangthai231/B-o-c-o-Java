package com.eventmanager.backend.admin.service;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.common.HangSo;
import com.eventmanager.backend.admin.common.LoiNghiepVu;
import com.eventmanager.backend.entity.Quyen;
import com.eventmanager.backend.entity.TaiKhoan;
import com.eventmanager.backend.entity.TaiKhoanQuyen;
import com.eventmanager.backend.entity.VaiTroQuyen;
import com.eventmanager.backend.repository.QuyenRepository;
import com.eventmanager.backend.repository.TaiKhoanQuyenRepository;
import com.eventmanager.backend.repository.TaiKhoanRepository;
import com.eventmanager.backend.repository.VaiTroQuyenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Ctr_CapQuyen + Ctr_ThayDoiQuyen + Ctr_TraCuuQuyen.
 *
 * Cách hiểu đơn giản:
 *  - Mỗi VAI TRÒ có sẵn một bộ quyền mặc định (bảng vai_tro_quyen).
 *  - Admin có thể CẤP THÊM quyền riêng cho từng tài khoản (bảng tai_khoan_quyen).
 *  - Quyền thực tế của 1 tài khoản = quyền theo vai trò + quyền được cấp riêng.
 *  - "Thay đổi quyền" = đổi vai trò của tài khoản (bộ quyền mặc định đổi theo).
 */
@Service
public class PhanQuyenService {

    private final QuyenRepository quyenRepo;
    private final VaiTroQuyenRepository vaiTroQuyenRepo;
    private final TaiKhoanQuyenRepository taiKhoanQuyenRepo;
    private final TaiKhoanRepository taiKhoanRepo;
    private final TaiKhoanAdminService taiKhoanService;

    public PhanQuyenService(QuyenRepository quyenRepo, VaiTroQuyenRepository vaiTroQuyenRepo,
                            TaiKhoanQuyenRepository taiKhoanQuyenRepo, TaiKhoanRepository taiKhoanRepo,
                            TaiKhoanAdminService taiKhoanService) {
        this.quyenRepo = quyenRepo;
        this.vaiTroQuyenRepo = vaiTroQuyenRepo;
        this.taiKhoanQuyenRepo = taiKhoanQuyenRepo;
        this.taiKhoanRepo = taiKhoanRepo;
        this.taiKhoanService = taiKhoanService;
    }

    /** Toàn bộ danh mục quyền. */
    public List<Map<String, Object>> dsQuyen() {
        return quyenRepo.findAllByOrderByIdAsc().stream().map(this::quyenSangMap).toList();
    }

    /** Bước 4-8: thông tin tài khoản + quyền hiện tại (tách quyền theo vai trò và quyền riêng). */
    public Map<String, Object> quyenCuaTaiKhoan(Long taiKhoanId) {
        TaiKhoan tk = taiKhoanService.layTheoId(taiKhoanId);
        Set<String> theoVaiTro = quyenTheoVaiTro(tk.getVaiTro());
        Set<String> rieng = quyenRieng(tk.getId());
        rieng.removeAll(theoVaiTro);

        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("taiKhoan", taiKhoanService.sangMap(tk));
        kq.put("quyenTheoVaiTro", new ArrayList<>(theoVaiTro));
        kq.put("quyenRieng", new ArrayList<>(rieng));
        return kq;
    }

    /** Cấp quyền: lưu lại danh sách quyền riêng (bỏ những quyền vai trò đã có sẵn). */
    @Transactional
    public Map<String, Object> capQuyen(Long taiKhoanId, List<String> dsMaQuyen) {
        TaiKhoan tk = taiKhoanService.layTheoId(taiKhoanId);
        Set<String> hopLe = new HashSet<>();
        quyenRepo.findAll().forEach(q -> hopLe.add(q.getMaQuyen()));
        Set<String> theoVaiTro = quyenTheoVaiTro(tk.getVaiTro());

        Set<String> moi = new LinkedHashSet<>();
        for (String ma : dsMaQuyen == null ? List.<String>of() : dsMaQuyen) {
            if (!hopLe.contains(ma)) {
                throw new LoiNghiepVu("Mã quyền không hợp lệ: " + ma);
            }
            if (!theoVaiTro.contains(ma)) {
                moi.add(ma);
            }
        }

        // Xóa hết quyền riêng cũ rồi ghi lại -> đơn giản, không lo trùng
        taiKhoanQuyenRepo.deleteByTaiKhoanId(tk.getId());
        taiKhoanQuyenRepo.flush();
        LocalDateTime now = LocalDateTime.now();
        for (String ma : moi) {
            TaiKhoanQuyen tq = new TaiKhoanQuyen();
            tq.setTaiKhoanId(tk.getId());
            tq.setMaQuyen(ma);
            tq.setNgayCap(now);
            taiKhoanQuyenRepo.save(tq);
        }
        return quyenCuaTaiKhoan(taiKhoanId);
    }

    /** Thay đổi quyền = đổi vai trò. Không cho hạ vai trò Quản trị viên cuối cùng. */
    @Transactional
    public Map<String, Object> doiVaiTro(Long taiKhoanId, String vaiTroMoi) {
        if (!HangSo.DS_VAI_TRO.contains(vaiTroMoi)) {
            throw new LoiNghiepVu("Vai trò không hợp lệ");
        }
        TaiKhoan tk = taiKhoanService.layTheoId(taiKhoanId);
        if (vaiTroMoi.equals(tk.getVaiTro())) {
            throw new LoiNghiepVu("Tài khoản đang có vai trò này rồi");
        }
        if (HangSo.ADMIN.equals(tk.getVaiTro()) && taiKhoanRepo.countByVaiTro(HangSo.ADMIN) <= 1) {
            throw new LoiNghiepVu("Hệ thống phải còn ít nhất 1 Quản trị viên");
        }
        tk.setVaiTro(vaiTroMoi);
        taiKhoanRepo.save(tk);
        return quyenCuaTaiKhoan(taiKhoanId);
    }

    /** Tra cứu quyền: những tài khoản nào đang có quyền X (kèm nguồn: theo vai trò hay cấp riêng). */
    public Map<String, Object> traCuu(String maQuyen, String tuKhoa) {
        Quyen q = quyenRepo.findByMaQuyen(maQuyen).orElseThrow(() -> LoiNghiepVu.khongTimThay("quyền"));
        Set<String> vaiTroCo = new HashSet<>();
        vaiTroQuyenRepo.findByMaQuyen(maQuyen).forEach(v -> vaiTroCo.add(v.getVaiTro()));
        Set<Long> idCapRieng = new HashSet<>();
        taiKhoanQuyenRepo.findByMaQuyen(maQuyen).forEach(t -> idCapRieng.add(t.getTaiKhoanId()));

        String kw = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        List<Map<String, Object>> ds = new ArrayList<>();
        for (TaiKhoan tk : taiKhoanRepo.findAll()) {
            boolean coTheoVaiTro = vaiTroCo.contains(tk.getVaiTro());
            boolean coRieng = idCapRieng.contains(tk.getId());
            if (!coTheoVaiTro && !coRieng) continue;
            String timTrong = (DuLieu.khongNull(tk.getHoTen()) + " " + tk.getTenDangNhap() + " "
                    + DuLieu.khongNull(tk.getEmail())).toLowerCase();
            if (!kw.isEmpty() && !timTrong.contains(kw)) continue;

            Map<String, Object> m = taiKhoanService.sangMap(tk);
            m.put("nguon", coTheoVaiTro ? "Theo vai trò" : "Cấp riêng");
            ds.add(m);
        }

        List<String> tenVaiTro = vaiTroCo.stream().map(HangSo::tenVaiTro).sorted().toList();
        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("quyen", quyenSangMap(q));
        kq.put("vaiTroMacDinh", tenVaiTro);
        kq.put("taiKhoan", ds);
        return kq;
    }

    // ==================== HÀM PHỤ ====================

    private Set<String> quyenTheoVaiTro(String vaiTro) {
        Set<String> s = new LinkedHashSet<>();
        vaiTroQuyenRepo.findByVaiTro(vaiTro).forEach(v -> s.add(v.getMaQuyen()));
        return s;
    }

    private Set<String> quyenRieng(Long taiKhoanId) {
        Set<String> s = new LinkedHashSet<>();
        taiKhoanQuyenRepo.findByTaiKhoanId(taiKhoanId).forEach(t -> s.add(t.getMaQuyen()));
        return s;
    }

    private Map<String, Object> quyenSangMap(Quyen q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("maQuyen", q.getMaQuyen());
        m.put("tenQuyen", q.getTenQuyen());
        m.put("moTa", DuLieu.khongNull(q.getMoTa()));
        m.put("nhom", DuLieu.khongNull(q.getNhom()));
        List<String> vaiTro = new ArrayList<>();
        for (VaiTroQuyen v : vaiTroQuyenRepo.findByMaQuyen(q.getMaQuyen())) {
            vaiTro.add(HangSo.tenVaiTro(v.getVaiTro()));
        }
        m.put("vaiTroMacDinh", String.join(", ", vaiTro));
        return m;
    }
}
