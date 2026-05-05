package com.mtslsms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "security")
public class SecurityMonitoringProperties {
    private Rule rule = new Rule();
    private Watch watch = new Watch();

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Watch getWatch() {
        return watch;
    }

    public void setWatch(Watch watch) {
        this.watch = watch;
    }

    public static class Rule {
        private LoginFail loginFail = new LoginFail();
        private HighFrequency highFrequency = new HighFrequency();
        private CrossTerminalFailSpread crossTerminalFailSpread = new CrossTerminalFailSpread();
        private TerminalOffline terminalOffline = new TerminalOffline();
        private List<String> illegalIpList = new ArrayList<>();

        public LoginFail getLoginFail() {
            return loginFail;
        }

        public void setLoginFail(LoginFail loginFail) {
            this.loginFail = loginFail;
        }

        public HighFrequency getHighFrequency() {
            return highFrequency;
        }

        public void setHighFrequency(HighFrequency highFrequency) {
            this.highFrequency = highFrequency;
        }

        public CrossTerminalFailSpread getCrossTerminalFailSpread() {
            return crossTerminalFailSpread;
        }

        public void setCrossTerminalFailSpread(CrossTerminalFailSpread crossTerminalFailSpread) {
            this.crossTerminalFailSpread = crossTerminalFailSpread;
        }

        public TerminalOffline getTerminalOffline() {
            return terminalOffline;
        }

        public void setTerminalOffline(TerminalOffline terminalOffline) {
            this.terminalOffline = terminalOffline;
        }

        public List<String> getIllegalIpList() {
            return illegalIpList;
        }

        public void setIllegalIpList(List<String> illegalIpList) {
            this.illegalIpList = illegalIpList;
        }
    }

    public static class LoginFail {
        private int windowMinutes = 5;
        private int threshold = 5;

        public int getWindowMinutes() {
            return windowMinutes;
        }

        public void setWindowMinutes(int windowMinutes) {
            this.windowMinutes = windowMinutes;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }

    public static class HighFrequency {
        private int windowMinutes = 1;
        private int threshold = 20;

        public int getWindowMinutes() {
            return windowMinutes;
        }

        public void setWindowMinutes(int windowMinutes) {
            this.windowMinutes = windowMinutes;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }

    public static class CrossTerminalFailSpread {
        private int windowMinutes = 10;
        private int terminalThreshold = 3;
        private int cooldownMinutes = 10;

        public int getWindowMinutes() {
            return windowMinutes;
        }

        public void setWindowMinutes(int windowMinutes) {
            this.windowMinutes = windowMinutes;
        }

        public int getTerminalThreshold() {
            return terminalThreshold;
        }

        public void setTerminalThreshold(int terminalThreshold) {
            this.terminalThreshold = terminalThreshold;
        }

        public int getCooldownMinutes() {
            return cooldownMinutes;
        }

        public void setCooldownMinutes(int cooldownMinutes) {
            this.cooldownMinutes = cooldownMinutes;
        }
    }

    public static class TerminalOffline {
        private boolean enabled = true;
        private int thresholdMinutes = 5;
        private int cooldownMinutes = 10;
        private int scanIntervalSeconds = 60;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getThresholdMinutes() {
            return thresholdMinutes;
        }

        public void setThresholdMinutes(int thresholdMinutes) {
            this.thresholdMinutes = thresholdMinutes;
        }

        public int getCooldownMinutes() {
            return cooldownMinutes;
        }

        public void setCooldownMinutes(int cooldownMinutes) {
            this.cooldownMinutes = cooldownMinutes;
        }

        public int getScanIntervalSeconds() {
            return scanIntervalSeconds;
        }

        public void setScanIntervalSeconds(int scanIntervalSeconds) {
            this.scanIntervalSeconds = scanIntervalSeconds;
        }
    }

    public static class Watch {
        private boolean enabled = false;
        private String dir = "logs";
        private String defaultType = "system";
        private boolean scanOnStart = true;
        private List<String> extensions = new ArrayList<>(List.of(".log", ".txt"));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public String getDefaultType() {
            return defaultType;
        }

        public void setDefaultType(String defaultType) {
            this.defaultType = defaultType;
        }

        public boolean isScanOnStart() {
            return scanOnStart;
        }

        public void setScanOnStart(boolean scanOnStart) {
            this.scanOnStart = scanOnStart;
        }

        public List<String> getExtensions() {
            return extensions;
        }

        public void setExtensions(List<String> extensions) {
            this.extensions = extensions;
        }
    }
}
