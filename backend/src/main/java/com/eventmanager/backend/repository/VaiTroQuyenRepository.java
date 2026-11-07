package com.eventmanager.backend.repository;

import com.eventmanager.backend.entity.VaiTroQuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaiTroQuyenRepository extends JpaRepository<VaiTroQuyen, Long> {
    List<VaiTroQuyen> findByVaiTro(String vaiTro);

    List<VaiTroQuyen> findByMaQuyen(String maQuyen);
}
