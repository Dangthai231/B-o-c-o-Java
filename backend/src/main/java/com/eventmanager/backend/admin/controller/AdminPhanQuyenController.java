package com.eventmanager.backend.admin.controller;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.service.PhanQuyenService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** API màn hình "Phân quyền người dùng". */
@RestController
@RequestMapping("/api/admin/phan-quyen")
public class AdminPhanQuyenController {

    private final PhanQuyenService service;

    public AdminPhanQuyenController(PhanQuyenService service) {
        this.service = service;
    }

    @GetMapping("/quyen")
    public List<Map<String, Object>> dsQuyen() {
        return service.dsQuyen();
    }

    @GetMapping("/tai-khoan/{id}")
    public Map<String, Object> quyenCuaTaiKhoan(@PathVariable Long id) {
        return service.quyenCuaTaiKhoan(id);
    }

    // PUT /api/admin/phan-quyen/tai-khoan/5/quyen   Body: {"dsMaQuyen": ["DUYET_THONG_BAO", "XEM_BAO_CAO"]}
    @PutMapping("/tai-khoan/{id}/quyen")
    public Map<String, Object> capQuyen(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<String> ds = new ArrayList<>();
        if (body.get("dsMaQuyen") instanceof List<?> list) {
            for (Object o : list) ds.add(String.valueOf(o));
        }
        return service.capQuyen(id, ds);
    }

    // PUT /api/admin/phan-quyen/tai-khoan/5/vai-tro   Body: {"vaiTro": "CAN_BO"}
    @PutMapping("/tai-khoan/{id}/vai-tro")
    public Map<String, Object> doiVaiTro(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return service.doiVaiTro(id, DuLieu.chuoi(body, "vaiTro"));
    }

    // GET /api/admin/phan-quyen/tra-cuu?maQuyen=QL_SU_KIEN&tuKhoa=
    @GetMapping("/tra-cuu")
    public Map<String, Object> traCuu(@RequestParam String maQuyen, @RequestParam(defaultValue = "") String tuKhoa) {
        return service.traCuu(maQuyen, tuKhoa);
    }
}
