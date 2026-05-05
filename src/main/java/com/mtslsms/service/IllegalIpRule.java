package com.mtslsms.service;

import com.mtslsms.config.SecurityMonitoringProperties;
import com.mtslsms.entity.Event;
import com.mtslsms.entity.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class IllegalIpRule implements SecurityRule {
    @Autowired
    private SecurityMonitoringProperties monitoringProperties;

    @Override
    public boolean check(Log log) {
        return monitoringProperties.getRule().getIllegalIpList().contains(log.getIp());
    }

    @Override
    public Event createEvent(Log log) {
        Event event = new Event();
        event.setLevel("HIGH");
        event.setDescription("检测到非法IP访问，来源IP: " + log.getIp());
        event.setTime(LocalDateTime.now());
        return event;
    }
}
