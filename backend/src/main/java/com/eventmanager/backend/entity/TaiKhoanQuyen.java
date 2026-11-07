package com.eventmanager.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Quyền được Admin cấp RIÊNG cho một tài khoản (ngoài quyền mặc định của vai trò).
 */
@Entity
@Table(name = "tai_khoan_quyen",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tai_khoan_id", "ma_quyen"}))
public class TaiKhoanQuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taiKhoanId;
    @Column(nullable = false)
    private String maQuyen;
    private LocalDateTime ngayCap;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaiKhoanId() { return taiKhoanId; }
    public void setTaiKhoanId(Long taiKhoanId) { this.taiKhoanId = taiKhoanId; }

    public String getMaQuyen() { return maQuyen; }
    public void setMaQuyen(String maQuyen) { this.maQuyen = maQuyen; }

    public LocalDateTime getNgayCap() { return ngayCap; }
    public void setNgayCap(LocalDateTime ngayCap) { this.ngayCap = ngayCap; }
}
