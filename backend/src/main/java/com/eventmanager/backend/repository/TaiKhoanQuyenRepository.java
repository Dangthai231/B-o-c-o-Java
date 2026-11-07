package com.eventmanager.backend.repository;

import com.eventmanager.backend.entity.TaiKhoanQuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaiKhoanQuyenRepository extends JpaRepository<TaiKhoanQuyen, Long> {
    List<TaiKhoanQuyen> findByTaiKhoanId(Long taiKhoanId);

    List<TaiKhoanQuyen> findByMaQuyen(String maQuyen);

    void deleteByTaiKhoanId(Long taiKhoanId);
}
