package ru.vilas.sewing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class ScheduledTaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final BackupService backupService;

    public ScheduledTaskService(BackupService backupService) {
        this.backupService = backupService;
    }

    @Scheduled(cron = "${task.cron.expression}", zone = "Europe/Moscow")
    public void performScheduledTask() {
        LOGGER.info("Scheduled task started.");
        backupService.createBackupAndSendEmail();
        LOGGER.info("Scheduled task completed.");
    }
}
