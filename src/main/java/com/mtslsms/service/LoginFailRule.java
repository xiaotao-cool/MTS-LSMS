package com.mtslsms.service;

import com.mtslsms.config.SecurityMonitoringProperties;
import com.mtslsms.entity.Event;
import com.mtslsms.entity.Log;
import com.mtslsms.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LoginFailRule implements SecurityRule {
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private SecurityMonitoringProperties monitoringProperties;

    @Override
    public boolean check(Log log) {
        if (!"login_failed".equals(log.getType())) return false;
        int windowMinutes = monitoringProperties.getRule().getLoginFail().getWindowMinutes();
        int threshold = monitoringProperties.getRule().getLoginFail().getThreshold();
        return logRepository.countByIpAndTypeAndTimeAfter(
                log.getIp(), "login_failed", LocalDateTime.now().minusMinutes(windowMinutes)) >= threshold;
    }

    @Override
    public Event createEvent(Log log) {
        Event e = new Event();
        e.setLevel("HIGH");
        e.setDescription("检测到暴力破解行为，来源IP: " + log.getIp());
        e.setTime(LocalDateTime.now());
        return e;
    }
}
