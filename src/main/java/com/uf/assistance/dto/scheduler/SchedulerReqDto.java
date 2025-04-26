package com.uf.assistance.dto.scheduler;

import com.uf.assistance.domain.scheduler.ScheduledJob;
import lombok.Data;

@Data
public class SchedulerReqDto {

    private String jobName;
    private String jobGroup;
    private String cronExpression;
    private String jobType;
    private String jobData;

    public static ScheduledJob toEntity(SchedulerReqDto schedulerReqDto) {
        return ScheduledJob.builder()
                .jobName(schedulerReqDto.getJobName())
                .jobGroup(schedulerReqDto.getJobGroup())
                .cronExpression(schedulerReqDto.getCronExpression())
                .jobType(schedulerReqDto.getJobType())
                .jobData(schedulerReqDto.jobData)
                .status(ScheduledJob.Status.ENABLED)
                .build();
    }
}