package com.eventmanager.backend.entity;

import jakarta.persistence.*;

/**
 * e_Quyen: danh mục quyền (chức năng) trong hệ thống. VD: QL_SU_KIEN = Quản lý sự kiện.
 */
@Entity
@Table(name = "quyen")
public class Quyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String maQuyen;   // VD: QL_SU_KIEN
    private String tenQuyen;  // VD: Quản lý sự kiện
    private String moTa;
    private String nhom;      // Nhóm chức năng: Sự kiện, Thông báo, Tài khoản...

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMaQuyen() { return maQuyen; }
    public void setMaQuyen(String maQuyen) { this.maQuyen = maQuyen; }

    public String getTenQuyen() { return tenQuyen; }
    public void setTenQuyen(String tenQuyen) { this.tenQuyen = tenQuyen; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getNhom() { return nhom; }
    public void setNhom(String nhom) { this.nhom = nhom; }
}
