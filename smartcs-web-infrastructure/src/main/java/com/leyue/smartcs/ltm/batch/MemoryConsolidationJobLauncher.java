package com.leyue.smartcs.ltm.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时触发记忆巩固批处理作业
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "smartcs.ai.ltm.consolidation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MemoryConsolidationJobLauncher {

    private final JobLauncher jobLauncher;
    private final Job memoryConsolidationJob;

    @Scheduled(cron = "${smartcs.ai.ltm.consolidation.schedule:0 0 2 * * ?}")
    public void launchJob() {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        try {
            log.info("启动记忆巩固批处理作业");
            jobLauncher.run(memoryConsolidationJob, parameters);
        } catch (JobExecutionException ex) {
            log.error("记忆巩固批处理作业触发失败", ex);
        }
    }
}
