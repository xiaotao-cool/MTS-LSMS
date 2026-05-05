package com.mtslsms.service;

import com.mtslsms.config.SecurityMonitoringProperties;
import com.mtslsms.entity.Log;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class LogFileWatcher {
    private static final Logger logger = LoggerFactory.getLogger(LogFileWatcher.class);
    private static final Pattern IPV4 = Pattern.compile("\\b((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\b");

    @Autowired
    private SecurityMonitoringProperties properties;
    @Autowired
    private LogService logService;

    private WatchService watchService;
    private ExecutorService executorService;
    private final Map<Path, Long> offsets = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    @PostConstruct
    public void start() {
        if (!properties.getWatch().isEnabled()) {
            return;
        }
        Path watchDir = Paths.get(properties.getWatch().getDir());
        if (!Files.exists(watchDir)) {
            logger.warn("WatchService目录不存在: {}", watchDir.toAbsolutePath());
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            watchDir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );
            if (properties.getWatch().isScanOnStart()) {
                scanHistoryFiles(watchDir);
            }
            running = true;
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> watchLoop(watchDir));
            logger.info("WatchService已启动，监听目录: {}", watchDir.toAbsolutePath());
        } catch (IOException e) {
            logger.error("WatchService启动失败", e);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {
                // ignore close exception
            }
        }
    }

    private void watchLoop(Path watchDir) {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("WatchService监听中断", e);
                break;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                Path relative = (Path) event.context();
                Path target = watchDir.resolve(relative);
                if (!Files.isRegularFile(target) || !isWatchFile(target)) {
                    continue;
                }
                readAppendedLines(target);
            }

            if (!key.reset()) {
                break;
            }
        }
    }

    private void readAppendedLines(Path filePath) {
        long start = offsets.getOrDefault(filePath, 0L);
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            if (start > raf.length()) {
                start = 0L;
            }
            raf.seek(start);

            String line;
            while ((line = raf.readLine()) != null) {
                if (!line.isBlank()) {
                    ingestLine(line.trim());
                }
            }
            offsets.put(filePath, raf.getFilePointer());
        } catch (IOException e) {
            logger.error("读取日志文件失败: {}", filePath, e);
        }
    }

    private void scanHistoryFiles(Path watchDir) {
        try (Stream<Path> stream = Files.list(watchDir)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(this::isWatchFile)
                    .forEach(this::readAppendedLines);
            logger.info("历史日志扫描完成: {}", watchDir.toAbsolutePath());
        } catch (IOException e) {
            logger.error("历史日志扫描失败: {}", watchDir, e);
        }
    }

    private boolean isWatchFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        List<String> extensions = properties.getWatch().getExtensions();
        if (extensions == null || extensions.isEmpty()) {
            return true;
        }
        for (String extension : extensions) {
            if (fileName.endsWith(extension.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void ingestLine(String line) {
        Log log = new Log();
        log.setTerminalId("local-file-watcher");
        log.setTerminalType("FILE_AGENT");
        log.setType(properties.getWatch().getDefaultType());
        log.setContent(line);

        Matcher matcher = IPV4.matcher(line);
        if (matcher.find()) {
            log.setIp(matcher.group());
        }

        logService.processLog(log);
    }
}
