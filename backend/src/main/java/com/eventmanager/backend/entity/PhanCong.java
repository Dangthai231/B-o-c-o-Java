package com.eventmanager.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * e_PhanCong: phân công cán bộ phụ trách một sự kiện.
 */
@Entity
@Table(name = "phan_cong",
        uniqueConstraints = @UniqueConstraint(columnNames = {"su_kien_id", "ten_dang_nhap"}))
public class PhanCong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long suKienId;
    @Column(nullable = false)
    private String tenDangNhap;     // tài khoản cán bộ (vai trò CAN_BO)
    private String vaiTroPhuTrach;  // VD: Trưởng ban tổ chức, Hậu cần, Truyền thông
    private LocalDateTime ngayPhanCong;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSuKienId() { return suKienId; }
    public void setSuKienId(Long suKienId) { this.suKienId = suKienId; }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getVaiTroPhuTrach() { return vaiTroPhuTrach; }
    public void setVaiTroPhuTrach(String vaiTroPhuTrach) { this.vaiTroPhuTrach = vaiTroPhuTrach; }

    public LocalDateTime getNgayPhanCong() { return ngayPhanCong; }
    public void setNgayPhanCong(LocalDateTime ngayPhanCong) { this.ngayPhanCong = ngayPhanCong; }
}
