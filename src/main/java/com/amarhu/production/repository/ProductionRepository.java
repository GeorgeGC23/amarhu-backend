package com.amarhu.production.repository;

import com.amarhu.user.entity.User;
import com.amarhu.production.entity.Production;
import com.amarhu.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {
    // Buscar producciones en un rango de fechas
    List<Production> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Production> findByUser(User user);
}
