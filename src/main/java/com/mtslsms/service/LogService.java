package com.mtslsms.service;

import com.mtslsms.entity.Log;
import com.mtslsms.repository.EventRepository;
import com.mtslsms.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private LogRepository logRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private List<SecurityRule> rules; // 自动注入所有 SecurityRule 实现类
    @Autowired
    private LogPreprocessor logPreprocessor;


    @Transactional
    public void processLog(Log log) {
        Log normalizedLog = logPreprocessor.normalize(log);
        if (normalizedLog == null) {
            return;
        }
        logRepository.save(normalizedLog); // 1. 存储标准化日志

        // 2. 规则引擎自动检测
        for (SecurityRule rule : rules) {
            if (rule.check(normalizedLog)) {
                eventRepository.save(rule.createEvent(normalizedLog));
            }
        }
    }

    public Map<String, Object> overview() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalLogs", logRepository.count());
        result.put("totalEvents", eventRepository.count());
        result.put("connectedTerminals", logRepository.countDistinctTerminals());
        result.put("logsLastHour", logRepository.countByTimeAfter(oneHourAgo));
        result.put("eventsLast24Hours", eventRepository.countByTimeAfter(oneDayAgo));
        result.put("trendDays", 7);
        result.put("trendStart", sevenDaysAgo.format(DATE_FMT));
        return result;
    }

    public List<Object[]> dailyTrend(int days) {
        int safeDays = Math.max(days, 1);
        return logRepository.countDailyLogs(LocalDateTime.now().minusDays(safeDays));
    }

    public List<Object[]> eventLevels(int days) {
        int safeDays = Math.max(days, 1);
        return eventRepository.countByLevel(LocalDateTime.now().minusDays(safeDays));
    }

    public Page<Log> queryLogs(String terminalId, String ip, String type, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        Specification<Log> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(terminalId)) {
                predicates.add(cb.equal(root.get("terminalId"), terminalId.trim()));
            }
            if (StringUtils.hasText(ip)) {
                predicates.add(cb.equal(root.get("ip"), ip.trim()));
            }
            if (StringUtils.hasText(type)) {
                predicates.add(cb.equal(root.get("type"), type.trim().toLowerCase()));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("time"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("time"), endTime));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return logRepository.findAll(specification, pageable);
    }

    public List<Map<String, Object>> terminalStats() {
        return logRepository.terminalStats().stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("terminalId", row[0]);
            item.put("terminalType", row[1]);
            item.put("logCount", row[2]);
            item.put("lastSeen", row[3]);
            return item;
        }).toList();
    }
}