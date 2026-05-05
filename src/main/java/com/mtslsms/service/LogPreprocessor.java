package com.mtslsms.service;

import com.mtslsms.entity.Log;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class LogPreprocessor {
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "login_success", "login_failed", "access", "error", "system"
    );

    public Log normalize(Log input) {
        if (input == null) {
            return null;
        }
        if (!StringUtils.hasText(input.getIp()) && !StringUtils.hasText(input.getContent())) {
            return null;
        }

        Log normalized = new Log();
        normalized.setTerminalId(normalizeTerminalId(input.getTerminalId()));
        normalized.setTerminalType(normalizeTerminalType(input.getTerminalType()));
        normalized.setIp(normalizeIp(input.getIp()));
        normalized.setTime(input.getTime() == null ? LocalDateTime.now() : input.getTime());
        normalized.setType(normalizeType(input.getType()));
        normalized.setContent(normalizeContent(input.getContent()));
        return normalized;
    }

    private String normalizeIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "0.0.0.0";
        }
        return ip.trim();
    }

    private String normalizeTerminalId(String terminalId) {
        if (!StringUtils.hasText(terminalId)) {
            return "unknown-terminal";
        }
        return terminalId.trim();
    }

    private String normalizeTerminalType(String terminalType) {
        if (!StringUtils.hasText(terminalType)) {
            return "UNKNOWN";
        }
        return terminalType.trim().toUpperCase();
    }

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return "system";
        }
        String normalized = type.trim().toLowerCase();
        return ALLOWED_TYPES.contains(normalized) ? normalized : "system";
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            return "empty log message";
        }
        return content.trim();
    }
}
