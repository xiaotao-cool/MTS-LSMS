package com.mtslsms.repository;

import com.mtslsms.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer>, JpaSpecificationExecutor<Log> {

    int countByIpAndTypeAndTimeAfter(String ip, String type, LocalDateTime localDateTime);

    int countByIpAndTimeAfter(String ip, LocalDateTime localDateTime);

    int countByTimeAfter(LocalDateTime localDateTime);

    @Query("SELECT COUNT(DISTINCT l.terminalId) FROM Log l")
    long countDistinctTerminals();

    @Query("SELECT COUNT(DISTINCT l.terminalId) " +
            "FROM Log l " +
            "WHERE l.ip = :ip " +
            "AND l.type = 'login_failed' " +
            "AND l.time >= :startTime")
    long countDistinctTerminalsByIpForFailedLogin(
            @Param("ip") String ip,
            @Param("startTime") LocalDateTime startTime
    );

    @Query("SELECT FUNCTION('DATE', l.time), COUNT(l) " +
            "FROM Log l " +
            "WHERE l.time >= :startTime " +
            "GROUP BY FUNCTION('DATE', l.time) " +
            "ORDER BY FUNCTION('DATE', l.time)")
    List<Object[]> countDailyLogs(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT l.terminalId, l.terminalType, COUNT(l), MAX(l.time) " +
            "FROM Log l " +
            "GROUP BY l.terminalId, l.terminalType " +
            "ORDER BY COUNT(l) DESC")
    List<Object[]> terminalStats();

    @Query("SELECT l.terminalId, l.terminalType, MAX(l.time) " +
            "FROM Log l " +
            "GROUP BY l.terminalId, l.terminalType")
    List<Object[]> terminalLastSeen();
}

