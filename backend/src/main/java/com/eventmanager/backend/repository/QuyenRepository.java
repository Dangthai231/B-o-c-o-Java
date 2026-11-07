package com.eventmanager.backend.repository;

import com.eventmanager.backend.entity.Quyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuyenRepository extends JpaRepository<Quyen, Long> {
    Optional<Quyen> findByMaQuyen(String maQuyen);

    List<Quyen> findAllByOrderByIdAsc();
}
