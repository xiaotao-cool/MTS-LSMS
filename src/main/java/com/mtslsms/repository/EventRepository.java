package com.mtslsms.repository;

import com.mtslsms.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    int countByTimeAfter(LocalDateTime localDateTime);
    int countByDescriptionContainingAndTimeAfter(String description, LocalDateTime localDateTime);

    @Query("SELECT e.level, COUNT(e) " +
            "FROM Event e " +
            "WHERE e.time >= :startTime " +
            "GROUP BY e.level " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> countByLevel(@Param("startTime") LocalDateTime startTime);
}
