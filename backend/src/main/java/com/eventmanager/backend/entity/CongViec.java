package com.eventmanager.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cong_viec")
public class CongViec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maCv;
    private String tenCongViec;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    private String trangThai;   // "Chưa bắt đầu", "Đang thực hiện", "Hoàn thành", "Tạm hoãn"
    private Integer tienDo;     // 0-100
    private String nhanSu;      // Người được phân công

    private Long suKienId;      // Liên kết với sự kiện
    private String tenSuKien;   // Cache tên sự kiện để hiển thị nhanh

    private String mucDoUuTien; // "Cao", "Trung bình", "Thấp"
    private LocalDate hanChot;
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
        if (trangThai == null) trangThai = "Chưa bắt đầu";
        if (tienDo == null) tienDo = 0;
        if (mucDoUuTien == null) mucDoUuTien = "Trung bình";
    }

    // ==================== Getters & Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMaCv() { return maCv; }
    public void setMaCv(String maCv) { this.maCv = maCv; }

    public String getTenCongViec() { return tenCongViec; }
    public void setTenCongViec(String tenCongViec) { this.tenCongViec = tenCongViec; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Integer getTienDo() { return tienDo; }
    public void setTienDo(Integer tienDo) { this.tienDo = tienDo; }

    public String getNhanSu() { return nhanSu; }
    public void setNhanSu(String nhanSu) { this.nhanSu = nhanSu; }

    public Long getSuKienId() { return suKienId; }
    public void setSuKienId(Long suKienId) { this.suKienId = suKienId; }

    public String getTenSuKien() { return tenSuKien; }
    public void setTenSuKien(String tenSuKien) { this.tenSuKien = tenSuKien; }

    public String getMucDoUuTien() { return mucDoUuTien; }
    public void setMucDoUuTien(String mucDoUuTien) { this.mucDoUuTien = mucDoUuTien; }

    public LocalDate getHanChot() { return hanChot; }
    public void setHanChot(LocalDate hanChot) { this.hanChot = hanChot; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
