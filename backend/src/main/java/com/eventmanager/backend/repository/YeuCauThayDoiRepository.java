package com.eventmanager.backend.repository;

import com.eventmanager.backend.entity.YeuCauThayDoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YeuCauThayDoiRepository extends JpaRepository<YeuCauThayDoi, Long> {
    List<YeuCauThayDoi> findAllByOrderByNgayGuiDesc();

    List<YeuCauThayDoi> findByTrangThaiOrderByNgayGuiDesc(String trangThai);

    long countByTrangThai(String trangThai);

    boolean existsByTenDangNhapAndTrangThai(String tenDangNhap, String trangThai);
}
