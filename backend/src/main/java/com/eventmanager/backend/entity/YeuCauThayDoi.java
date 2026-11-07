package com.eventmanager.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * e_YeuCauThayDoi: yêu cầu thay đổi thông tin cá nhân do người dùng gửi, chờ Admin duyệt.
 */
@Entity
@Table(name = "yeu_cau_thay_doi")
public class YeuCauThayDoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenDangNhap;     // người gửi yêu cầu

    // Thông tin đề xuất (giá trị MỚI người dùng muốn đổi)
    private String hoTenMoi;
    private String emailMoi;
    private String soDienThoaiMoi;
    private String lopMoi;

    @Column(columnDefinition = "TEXT")
    private String lyDo;            // lý do / minh chứng người dùng ghi kèm

    private String trangThai;       // "Chờ duyệt", "Đã duyệt", "Từ chối"
    @Column(columnDefinition = "TEXT")
    private String lyDoTuChoi;
    private LocalDateTime ngayGui;
    private LocalDateTime ngayXuLy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getHoTenMoi() { return hoTenMoi; }
    public void setHoTenMoi(String hoTenMoi) { this.hoTenMoi = hoTenMoi; }

    public String getEmailMoi() { return emailMoi; }
    public void setEmailMoi(String emailMoi) { this.emailMoi = emailMoi; }

    public String getSoDienThoaiMoi() { return soDienThoaiMoi; }
    public void setSoDienThoaiMoi(String soDienThoaiMoi) { this.soDienThoaiMoi = soDienThoaiMoi; }

    public String getLopMoi() { return lopMoi; }
    public void setLopMoi(String lopMoi) { this.lopMoi = lopMoi; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getLyDoTuChoi() { return lyDoTuChoi; }
    public void setLyDoTuChoi(String lyDoTuChoi) { this.lyDoTuChoi = lyDoTuChoi; }

    public LocalDateTime getNgayGui() { return ngayGui; }
    public void setNgayGui(LocalDateTime ngayGui) { this.ngayGui = ngayGui; }

    public LocalDateTime getNgayXuLy() { return ngayXuLy; }
    public void setNgayXuLy(LocalDateTime ngayXuLy) { this.ngayXuLy = ngayXuLy; }
}
