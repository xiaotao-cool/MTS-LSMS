package com.mtslsms.service;

import com.mtslsms.entity.Event;
import com.mtslsms.entity.Log;

public interface SecurityRule {
    boolean check(Log log);
    Event createEvent(Log log);
}