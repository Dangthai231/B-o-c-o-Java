package com.eventmanager.backend.repository;

import com.eventmanager.backend.entity.PhanCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhanCongRepository extends JpaRepository<PhanCong, Long> {
    List<PhanCong> findBySuKienIdOrderByNgayPhanCongAsc(Long suKienId);

    boolean existsBySuKienIdAndTenDangNhap(Long suKienId, String tenDangNhap);

    long countBySuKienId(Long suKienId);
}
