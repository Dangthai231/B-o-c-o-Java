package com.eventmanager.backend.repository;

import com.eventmanager.backend.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {
    // Bổ sung cho Admin
    Optional<ThongBao> findByMaThongBao(String maThongBao);

    boolean existsByMaThongBao(String maThongBao);
}
