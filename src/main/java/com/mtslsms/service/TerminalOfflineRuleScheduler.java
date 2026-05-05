package com.mtslsms.service;

import com.mtslsms.config.SecurityMonitoringProperties;
import com.mtslsms.entity.Event;
import com.mtslsms.repository.EventRepository;
import com.mtslsms.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TerminalOfflineRuleScheduler {
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private SecurityMonitoringProperties properties;

    @Scheduled(fixedDelayString = "#{${security.rule.terminal-offline.scan-interval-seconds:60} * 1000}")
    public void scanTerminalOffline() {
        SecurityMonitoringProperties.TerminalOffline offline = properties.getRule().getTerminalOffline();
        if (!offline.isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(offline.getThresholdMinutes());
        LocalDateTime cooldownStart = now.minusMinutes(offline.getCooldownMinutes());

        List<Object[]> rows = logRepository.terminalLastSeen();
        for (Object[] row : rows) {
            String terminalId = String.valueOf(row[0]);
            String terminalType = String.valueOf(row[1]);
            LocalDateTime lastSeen = (LocalDateTime) row[2];
            if (lastSeen == null || !lastSeen.isBefore(threshold)) {
                continue;
            }

            String marker = "[TERMINAL_OFFLINE] terminalId=" + terminalId;
            int recent = eventRepository.countByDescriptionContainingAndTimeAfter(marker, cooldownStart);
            if (recent > 0) {
                continue;
            }

            Event event = new Event();
            event.setLevel("WARNING");
            event.setDescription(marker + ", terminalType=" + terminalType +
                    ", lastSeen=" + lastSeen + ", 超过 " + offline.getThresholdMinutes() + " 分钟无日志");
            event.setTime(now);
            eventRepository.save(event);
        }
    }
}
