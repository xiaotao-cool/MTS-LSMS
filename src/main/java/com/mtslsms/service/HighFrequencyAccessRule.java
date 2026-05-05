package com.mtslsms.service;

import com.mtslsms.config.SecurityMonitoringProperties;
import com.mtslsms.entity.Event;
import com.mtslsms.entity.Log;
import com.mtslsms.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class HighFrequencyAccessRule implements SecurityRule {
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private SecurityMonitoringProperties monitoringProperties;

    @Override
    public boolean check(Log log) {
        if (!"access".equals(log.getType())) {
            return false;
        }
        int windowMinutes = monitoringProperties.getRule().getHighFrequency().getWindowMinutes();
        int threshold = monitoringProperties.getRule().getHighFrequency().getThreshold();
        return logRepository.countByIpAndTimeAfter(log.getIp(), LocalDateTime.now().minusMinutes(windowMinutes)) >= threshold;
    }

    @Override
    public Event createEvent(Log log) {
        Event event = new Event();
        event.setLevel("MEDIUM");
        event.setDescription("检测到高频访问，来源IP: " + log.getIp());
        event.setTime(LocalDateTime.now());
        return event;
    }
}
