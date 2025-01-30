package com.amarhu.video.repository;

import com.amarhu.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {

    // Busca videos por rango de fechas
    List<Video> findByDateBetween(String startDate, String endDate);

    @Query("SELECT v FROM Video v WHERE v.description LIKE %:codigo%")
    List<Video> findByCodigo(@Param("codigo") String codigo);

    List<Video> findByDescriptionContainingAndDateBetween(String description, String startDate, String endDate);

    List<Video> findByDescriptionContaining(String codigo);


}
