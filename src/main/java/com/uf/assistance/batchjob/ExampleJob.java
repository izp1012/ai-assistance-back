package com.uf.assistance.batchjob;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 예제 작업 구현
 */
@Component
public class ExampleJob extends AbstractScheduledJob {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void executeJob(JobExecutionContext context) throws Exception {
        String jobName = context.getJobDetail().getKey().getName();

        logger.info("Executing example job: {}", jobName);

        // 여기에 실제 작업 로직 구현
        // 예: 데이터베이스 정리, 알림 전송, 데이터 처리 등

        // 예제를 위한 간단한 작업
        Thread.sleep(1000); // 1초 대기 (예시용)

        logger.info("Example job completed: {}", jobName);
    }
}