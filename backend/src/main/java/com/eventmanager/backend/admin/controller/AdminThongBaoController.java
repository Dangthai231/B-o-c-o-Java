package com.eventmanager.backend.admin.controller;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.service.ThongBaoAdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** API màn hình "Quản lý thông báo" của Admin. */
@RestController
@RequestMapping("/api/admin/thong-bao")
public class AdminThongBaoController {

    private final ThongBaoAdminService service;

    public AdminThongBaoController(ThongBaoAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> danhSach(@RequestParam(defaultValue = "") String trangThai,
                                              @RequestParam(defaultValue = "") String tuKhoa) {
        return service.danhSach(trangThai, tuKhoa);
    }

    @GetMapping("/dem")
    public Map<String, Long> dem() {
        return service.demTheoTrangThai();
    }

    @GetMapping("/{id}")
    public Map<String, Object> chiTiet(@PathVariable Long id) {
        return service.chiTiet(id);
    }

    // Body: {tieuDe, noiDung, doiTuong}
    @PostMapping
    public Map<String, Object> taoMoi(@RequestBody Map<String, Object> body,
                                      @RequestParam(defaultValue = "admin") String nguoiTao) {
        return service.taoMoi(body, nguoiTao);
    }

    @PutMapping("/{id}")
    public Map<String, Object> capNhat(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return service.capNhat(id, body);
    }

    @PutMapping("/{id}/duyet")
    public Map<String, String> duyet(@PathVariable Long id) {
        service.duyet(id);
        return Map.of("message", "Duyệt thông báo thành công");
    }

    @PutMapping("/{id}/tu-choi")
    public Map<String, String> tuChoi(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        service.tuChoi(id, DuLieu.chuoi(body, "lyDo"));
        return Map.of("message", "Đã từ chối thông báo");
    }

    @PutMapping("/{id}/dang")
    public Map<String, String> dang(@PathVariable Long id) {
        service.dang(id);
        return Map.of("message", "Đăng thông báo thành công");
    }

    @PutMapping("/{id}/go")
    public Map<String, String> go(@PathVariable Long id) {
        service.go(id);
        return Map.of("message", "Gỡ thông báo thành công");
    }
}
