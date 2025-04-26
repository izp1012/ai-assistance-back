package com.uf.assistance.service;

import com.uf.assistance.batchjob.DynamicQuartzJob;
import com.uf.assistance.domain.scheduler.ScheduledJob;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Service
public class DynamicSchedulerService {

    private final Scheduler scheduler;

    public DynamicSchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void scheduleJob(ScheduledJob job) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(DynamicQuartzJob.class)
                .withIdentity(job.getJobName(), job.getJobGroup())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getJobName() + "_trigger", job.getJobGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void removeJob(ScheduledJob job) throws SchedulerException {
        JobKey jobKey = new JobKey(job.getJobName(), job.getJobGroup());
        scheduler.deleteJob(jobKey);
    }

    public void pauseJob(ScheduledJob job) throws SchedulerException {
        JobKey jobKey = new JobKey(job.getJobName(), job.getJobGroup());
        scheduler.pauseJob(jobKey);
    }

    public void resumeJob(ScheduledJob job) throws SchedulerException {
        JobKey jobKey = new JobKey(job.getJobName(), job.getJobGroup());
        scheduler.resumeJob(jobKey);
    }

    public void updateJobStatus(ScheduledJob job) throws SchedulerException {
        // 상태 업데이트가 Quartz에 어떻게 반영되는지 구현해야 함.
    }
}