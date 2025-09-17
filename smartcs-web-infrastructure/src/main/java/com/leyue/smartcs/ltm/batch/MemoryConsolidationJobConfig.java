package com.leyue.smartcs.ltm.batch;

import com.leyue.smartcs.domain.ltm.gateway.EpisodicMemoryGateway;
import com.leyue.smartcs.ltm.config.MemoryConsolidationProperties;
import com.leyue.smartcs.ltm.service.MemoryConsolidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch作业：长期记忆巩固
 */
@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class MemoryConsolidationJobConfig {

    private final MemoryConsolidationService consolidationService;
    private final EpisodicMemoryGateway episodicMemoryGateway;
    private final MemoryConsolidationProperties consolidationProperties;

    @Bean
    public ConsolidationUserItemReader consolidationUserItemReader() {
        return new ConsolidationUserItemReader(episodicMemoryGateway, consolidationProperties);
    }

    @Bean
    public ItemWriter<Long> consolidationUserItemWriter() {
        return items -> {
            for (Long userId : items) {
                try {
                    consolidationService.consolidateUserMemories(userId);
                } catch (Exception ex) {
                    log.error("批处理巩固用户记忆失败，userId={}", userId, ex);
                    throw ex;
                }
            }
        };
    }

    @Bean
    public Step memoryConsolidationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        @Qualifier("ltmTaskExecutor") TaskExecutor taskExecutor) {
        int chunkSize = Math.max(1, consolidationProperties.getChunkSize());
        int retryLimit = Math.max(0, consolidationProperties.getRetryLimit());

        SimpleStepBuilder<Long, Long> stepBuilder = new StepBuilder(
                "memoryConsolidationStep", jobRepository)
                .<Long, Long>chunk(chunkSize, transactionManager)
                .reader(consolidationUserItemReader())
                .writer(consolidationUserItemWriter());

        SimpleStepBuilder<Long, Long> asyncStepBuilder = stepBuilder.taskExecutor(taskExecutor);

        if (taskExecutor instanceof org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor threadPoolTaskExecutor) {
            int throttle = threadPoolTaskExecutor.getMaxPoolSize();
            if (throttle <= 0) {
                throttle = threadPoolTaskExecutor.getCorePoolSize();
            }
            if (throttle > 0) {
                asyncStepBuilder = asyncStepBuilder.throttleLimit(throttle);
            }
        }

        SimpleStepBuilder<Long, Long> finalBuilder = asyncStepBuilder;

        if (retryLimit > 0) {
            FaultTolerantStepBuilder<Long, Long> faultTolerantBuilder = finalBuilder.faultTolerant()
                    .retryLimit(retryLimit)
                    .retry(Exception.class);
            return faultTolerantBuilder.build();
        }

        return finalBuilder.build();
    }

    @Bean
    public Job memoryConsolidationJob(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      @Qualifier("ltmTaskExecutor") TaskExecutor taskExecutor) {
        return new JobBuilder("memoryConsolidationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .preventRestart()
                .start(memoryConsolidationStep(jobRepository, transactionManager, taskExecutor))
                .build();
    }
}
