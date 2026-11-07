package com.eventmanager.backend.repository;

import com.eventmanager.backend.entity.DangKySuKien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DangKySuKienRepository extends JpaRepository<DangKySuKien, Long> {
    List<DangKySuKien> findByTenDangNhapOrderByNgayDangKyDesc(String tenDangNhap);

    Optional<DangKySuKien> findBySuKienIdAndTenDangNhap(Long suKienId, String tenDangNhap);

    // Đếm số người đang giữ chỗ (khác trạng thái "Đã hủy")
    long countBySuKienIdAndTrangThaiNot(Long suKienId, String trangThai);

    // Bổ sung cho Admin
    List<DangKySuKien> findBySuKienId(Long suKienId);

    boolean existsByTenDangNhap(String tenDangNhap);
}
