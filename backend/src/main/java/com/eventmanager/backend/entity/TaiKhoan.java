package com.eventmanager.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tai_khoan")
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tenDangNhap;

    @Column(nullable = false)
    private String matKhau;

    @Column(nullable = false)
    private String vaiTro; // Nhận 3 giá trị: "ADMIN", "CAN_BO", "HOC_SINH"

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    // ===== Các cột bổ sung cho chức năng Admin (cho phép NULL để không ảnh hưởng dữ liệu cũ) =====
    private String hoTen;
    private String email;
    private String maSo;          // Mã sinh viên / mã nhân viên
    private String soDienThoai;
    private String trangThai;     // "HOAT_DONG" hoặc "BI_KHOA"
    private LocalDateTime ngayTao;

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMaSo() { return maSo; }
    public void setMaSo(String maSo) { this.maSo = maSo; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

}
