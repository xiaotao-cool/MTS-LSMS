package com.mtslsms.controller;

import com.mtslsms.entity.Event;
import com.mtslsms.entity.Log;
import com.mtslsms.repository.EventRepository;
import com.mtslsms.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin // 允许跨域请求
public class SecurityController {

    @Autowired
    private LogService logService;
    @Autowired
    private EventRepository eventRepository;

    // 接收终端采集数据
    @PostMapping("/collect")
    public ResponseEntity<String> collect(
            @RequestBody Log log,
            @RequestHeader(value = "X-Terminal-Id", required = false) String terminalId,
            @RequestHeader(value = "X-Terminal-Type", required = false) String terminalType
    ) {
        if (terminalId != null && !terminalId.isBlank()) {
            log.setTerminalId(terminalId);
        }
        if (terminalType != null && !terminalType.isBlank()) {
            log.setTerminalType(terminalType);
        }
        logService.processLog(log);
        return ResponseEntity.ok("Success");
    }

    // 接收原始文本日志（简单文件采集/网络采集入口）
    @PostMapping("/collect/raw")
    public ResponseEntity<String> collectRaw(
            @RequestBody String raw,
            @RequestParam(required = false, defaultValue = "raw-terminal") String terminalId,
            @RequestParam(required = false, defaultValue = "RAW") String terminalType
    ) {
        Log log = new Log();
        log.setTerminalId(terminalId);
        log.setTerminalType(terminalType);
        log.setType("system");
        log.setContent(raw);
        logService.processLog(log);
        return ResponseEntity.ok("Raw log accepted");
    }

    // 分页查询日志 (给 Vue 表格使用)
    @GetMapping("/logs")
    public Page<Log> getLogs(
            @RequestParam(required = false) String terminalId,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            Pageable pageable
    ) {
        return logService.queryLogs(terminalId, ip, type, startTime, endTime, pageable);
    }

    // 查询安全事件统计 (给 ECharts 使用)
    @GetMapping("/events/stats")
    public List<Event> getRecentEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/events")
    public Page<Event> getEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @GetMapping("/stats/overview")
    public Map<String, Object> getOverview() {
        return logService.overview();
    }

    @GetMapping("/stats/trend")
    public List<Map<String, Object>> getTrend(@RequestParam(defaultValue = "7") int days) {
        return logService.dailyTrend(days).stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", String.valueOf(row[0]));
            item.put("count", row[1]);
            return item;
        }).toList();
    }

    @GetMapping("/stats/event-levels")
    public List<Map<String, Object>> getEventLevels(@RequestParam(defaultValue = "7") int days) {
        return logService.eventLevels(days).stream().map(row -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("level", String.valueOf(row[0]));
            item.put("count", row[1]);
            return item;
        }).toList();
    }

    @GetMapping("/terminals/stats")
    public List<Map<String, Object>> getTerminalStats() {
        return logService.terminalStats();
    }
}