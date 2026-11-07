package com.eventmanager.backend.repository;

import com.eventmanager.backend.entity.TaiKhoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Long> {
    // Tìm tài khoản theo username
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    // ===== Bổ sung cho chức năng Admin =====
    boolean existsByTenDangNhap(String tenDangNhap);

    Optional<TaiKhoan> findByEmailIgnoreCase(String email);

    long countByTrangThai(String trangThai);

    long countByVaiTro(String vaiTro);

    List<TaiKhoan> findByVaiTroOrderByHoTenAsc(String vaiTro);

    /**
     * Tra cứu tài khoản: tìm theo họ tên / email / mã số / tên đăng nhập,
     * lọc theo vai trò và trạng thái. Tham số nào truyền chuỗi rỗng "" thì bỏ qua điều kiện đó.
     */
    @Query("""
            SELECT t FROM TaiKhoan t
            WHERE (:tuKhoa = ''
                   OR LOWER(COALESCE(t.hoTen, '')) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(COALESCE(t.email, '')) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(COALESCE(t.maSo, '')) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))
                   OR LOWER(t.tenDangNhap) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))
              AND (:vaiTro = '' OR t.vaiTro = :vaiTro)
              AND (:trangThai = '' OR t.trangThai = :trangThai)
            """)
    Page<TaiKhoan> timKiem(@Param("tuKhoa") String tuKhoa,
                           @Param("vaiTro") String vaiTro,
                           @Param("trangThai") String trangThai,
                           Pageable pageable);
}
