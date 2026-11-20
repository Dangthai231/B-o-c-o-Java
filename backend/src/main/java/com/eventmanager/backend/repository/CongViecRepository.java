package com.eventmanager.backend.repository;

import com.eventmanager.backend.entity.CongViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CongViecRepository extends JpaRepository<CongViec, Long> {

    List<CongViec> findAllByOrderByIdDesc();

    List<CongViec> findBySuKienIdOrderByIdDesc(Long suKienId);

    List<CongViec> findByTrangThaiOrderByIdDesc(String trangThai);

    List<CongViec> findByNhanSuContainingIgnoreCase(String nhanSu);

    @Query("SELECT COUNT(c) FROM CongViec c WHERE c.trangThai = ?1")
    long demTheoTrangThai(String trangThai);

    @Query("SELECT COUNT(c) FROM CongViec c WHERE c.hanChot < CURRENT_DATE AND c.trangThai <> 'Hoàn thành'")
    long demQuaHan();

    @Query("SELECT COALESCE(AVG(c.tienDo), 0) FROM CongViec c")
    Double tinhTienDoTrungBinh();
}
