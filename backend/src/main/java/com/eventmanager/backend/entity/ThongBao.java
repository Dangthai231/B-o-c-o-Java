package com.eventmanager.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "thong_bao")
public class ThongBao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String maThongBao; // VD: TB001

    private String tieuDe;

    @Column(columnDefinition = "TEXT") // Cho phép nội dung dài
    private String noiDung;

    private String doiTuong; // Khớp với JComboBox: "Tất cả...", "Ban tổ chức"...
    private LocalDate ngayGui;
    private String trangThai; // Khớp với JComboBox: "Nháp", "Đã gửi"...

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMaThongBao() { return maThongBao; }
    public void setMaThongBao(String maThongBao) { this.maThongBao = maThongBao; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String doiTuong) { this.doiTuong = doiTuong; }

    public LocalDate getNgayGui() { return ngayGui; }
    public void setNgayGui(LocalDate ngayGui) { this.ngayGui = ngayGui; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    // ===== Bổ sung cho Admin: duyệt / đăng / gỡ thông báo =====
    private String nguoiTao;
    @Column(columnDefinition = "TEXT")
    private String lyDoTuChoi;
    private LocalDateTime ngayDang;

    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }

    public String getLyDoTuChoi() { return lyDoTuChoi; }
    public void setLyDoTuChoi(String lyDoTuChoi) { this.lyDoTuChoi = lyDoTuChoi; }

    public LocalDateTime getNgayDang() { return ngayDang; }
    public void setNgayDang(LocalDateTime ngayDang) { this.ngayDang = ngayDang; }

}
