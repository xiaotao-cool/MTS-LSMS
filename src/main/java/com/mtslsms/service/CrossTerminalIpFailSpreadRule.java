package com.mtslsms.service;

import com.mtslsms.config.SecurityMonitoringProperties;
import com.mtslsms.entity.Event;
import com.mtslsms.entity.Log;
import com.mtslsms.repository.EventRepository;
import com.mtslsms.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CrossTerminalIpFailSpreadRule implements SecurityRule {
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private SecurityMonitoringProperties properties;

    @Override
    public boolean check(Log log) {
        if (!"login_failed".equals(log.getType())) {
            return false;
        }
        int windowMinutes = properties.getRule().getCrossTerminalFailSpread().getWindowMinutes();
        int terminalThreshold = properties.getRule().getCrossTerminalFailSpread().getTerminalThreshold();
        int cooldownMinutes = properties.getRule().getCrossTerminalFailSpread().getCooldownMinutes();
        String marker = "[IP_SPREAD] ip=" + log.getIp();

        long attackedTerminals = logRepository.countDistinctTerminalsByIpForFailedLogin(
                log.getIp(),
                LocalDateTime.now().minusMinutes(windowMinutes)
        );
        if (attackedTerminals < terminalThreshold) {
            return false;
        }
        int recentAlertCount = eventRepository.countByDescriptionContainingAndTimeAfter(
                marker,
                LocalDateTime.now().minusMinutes(cooldownMinutes)
        );
        return recentAlertCount == 0;
    }

    @Override
    public Event createEvent(Log log) {
        int windowMinutes = properties.getRule().getCrossTerminalFailSpread().getWindowMinutes();
        long attackedTerminals = logRepository.countDistinctTerminalsByIpForFailedLogin(
                log.getIp(),
                LocalDateTime.now().minusMinutes(windowMinutes)
        );

        Event event = new Event();
        event.setLevel("HIGH");
        event.setDescription("[IP_SPREAD] ip=" + log.getIp() +
                " 在 " + windowMinutes + " 分钟内跨 " + attackedTerminals + " 个终端出现登录失败扩散");
        event.setTime(LocalDateTime.now());
        return event;
    }
}
