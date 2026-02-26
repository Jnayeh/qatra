package com.zayenha.qatra.center.infrastructure.persistence.repository;

import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SlotJpaRepository extends JpaRepository<SlotEntity, Long> {

    List<SlotEntity> findByCenterIdAndDateOrderByStartTime(Long centerId, LocalDate date);

    @Query("SELECT s FROM SlotEntity s LEFT JOIN FETCH s.center WHERE s.center.id = :centerId AND s.date = :date ORDER BY s.startTime")
    List<SlotEntity> findByCenterIdAndDateWithCenter(@Param("centerId") Long centerId, @Param("date") LocalDate date);

    @Query("SELECT s FROM SlotEntity s WHERE s.center.id = :centerId AND s.date BETWEEN :from AND :to ORDER BY s.date, s.startTime")
    List<SlotEntity> findByCenterIdAndDateRange(@Param("centerId") Long centerId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT s FROM SlotEntity s LEFT JOIN FETCH s.center WHERE s.center.id = :centerId AND s.date BETWEEN :from AND :to ORDER BY s.date, s.startTime")
    List<SlotEntity> findByCenterIdAndDateRangeWithCenter(@Param("centerId") Long centerId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT s FROM SlotEntity s WHERE s.center.id = :centerId AND s.date = :date AND s.startTime < :endTime AND s.endTime > :startTime")
    List<SlotEntity> findOverlapping(@Param("centerId") Long centerId, @Param("date") LocalDate date,
                                     @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    Optional<SlotEntity> findById(Long id);

    @Query("SELECT s FROM SlotEntity s LEFT JOIN FETCH s.center WHERE s.id = :id")
    Optional<SlotEntity> findByIdWithCenter(@Param("id") Long id);

    @Query("SELECT s FROM SlotEntity s WHERE s.date >= :from AND s.date <= :to ORDER BY s.center.id, s.date, s.startTime")
    List<SlotEntity> findAllByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
