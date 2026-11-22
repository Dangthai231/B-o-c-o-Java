package com.eventmanager.backend.controller;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.service.ThongBaoAdminService;
import com.eventmanager.backend.entity.ThongBao;
import com.eventmanager.backend.repository.ThongBaoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ThongBaoController {

    @Autowired
    private ThongBaoAdminService thongBaoAdminService;

    @Autowired
    private ThongBaoRepository repo;

    // Đổ dữ liệu lên JTable
    @GetMapping("/api/thong-bao/danh-sach")
    public List<ThongBao> getAll() {
        return repo.findAll();
    }

    // Nhận dữ liệu từ form khi ấn nút Gửi / Lưu Nháp
    @PostMapping("/api/thong-bao/tao-moi")
    public ResponseEntity<ThongBao> taoThongBao(@RequestBody ThongBao tb) {
        ThongBao saved = repo.save(tb);
        return ResponseEntity.ok(saved);
    }

    // Cập nhật thông báo
    @PutMapping("/api/thong-bao/cap-nhat/{id}")
    public ResponseEntity<ThongBao> capNhatThongBao(@PathVariable Long id, @RequestBody ThongBao tbDetails) {
        return repo.findById(id).map(tb -> {
            tb.setTieuDe(tbDetails.getTieuDe());
            tb.setNoiDung(tbDetails.getNoiDung());
            tb.setDoiTuong(tbDetails.getDoiTuong());
            tb.setNgayGui(tbDetails.getNgayGui());
            tb.setTrangThai(tbDetails.getTrangThai());
            return ResponseEntity.ok(repo.save(tb));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Xóa thông báo
    @DeleteMapping("/api/thong-bao/xoa/{id}")
    public ResponseEntity<Void> xoaThongBao(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/thong-bao")
    public List<Map<String, Object>> danhSach(@RequestParam(defaultValue = "") String trangThai,
                                              @RequestParam(defaultValue = "") String tuKhoa) {
        return thongBaoAdminService.danhSach(trangThai, tuKhoa);
    }

    @GetMapping("/api/admin/thong-bao/dem")
    public Map<String, Long> dem() {
        return thongBaoAdminService.demTheoTrangThai();
    }

    @GetMapping("/api/admin/thong-bao/{id}")
    public Map<String, Object> chiTiet(@PathVariable Long id) {
        return thongBaoAdminService.chiTiet(id);
    }

    // Body: {tieuDe, noiDung, doiTuong}
    @PostMapping("/api/admin/thong-bao")
    public Map<String, Object> taoMoi(@RequestBody Map<String, Object> body,
                                      @RequestParam(defaultValue = "admin") String nguoiTao) {
        return thongBaoAdminService.taoMoi(body, nguoiTao);
    }

    @PutMapping("/api/admin/thong-bao/{id}")
    public Map<String, Object> capNhatAdmin(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return thongBaoAdminService.capNhat(id, body);
    }

    @PutMapping("/api/admin/thong-bao/{id}/duyet")
    public Map<String, String> duyet(@PathVariable Long id) {
        thongBaoAdminService.duyet(id);
        return Map.of("message", "Duyệt thông báo thành công");
    }

    @PutMapping("/api/admin/thong-bao/{id}/tu-choi")
    public Map<String, String> tuChoi(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        thongBaoAdminService.tuChoi(id, DuLieu.chuoi(body, "lyDo"));
        return Map.of("message", "Đã từ chối thông báo");
    }

    @PutMapping("/api/admin/thong-bao/{id}/dang")
    public Map<String, String> dang(@PathVariable Long id) {
        thongBaoAdminService.dang(id);
        return Map.of("message", "Đăng thông báo thành công");
    }

    @PutMapping("/api/admin/thong-bao/{id}/go")
    public Map<String, String> go(@PathVariable Long id) {
        thongBaoAdminService.go(id);
        return Map.of("message", "Gỡ thông báo thành công");
    }
}
