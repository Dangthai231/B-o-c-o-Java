package com.eventmanager.backend.entity;

import jakarta.persistence.*;

/**
 * Quyền mặc định của mỗi vai trò (ADMIN / CAN_BO / HOC_SINH).
 */
@Entity
@Table(name = "vai_tro_quyen",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vai_tro", "ma_quyen"}))
public class VaiTroQuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vaiTro;
    @Column(nullable = false)
    private String maQuyen;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getMaQuyen() { return maQuyen; }
    public void setMaQuyen(String maQuyen) { this.maQuyen = maQuyen; }
}
