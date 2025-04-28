package com.uf.assistance.dto.scheduler;

import com.uf.assistance.domain.scheduler.ScheduledJob;
import lombok.Data;

@Data
public class SchedulerReqDto {

    private String jobName;
    private String jobGroup;
    private String jobClass;
    private String cronExpression;
    private String description;
    private String jobType;
    private String jobData;
    private ScheduledJob.Status status;

    public static ScheduledJob toEntity(SchedulerReqDto schedulerReqDto) {
        return ScheduledJob.builder()
                .jobName(schedulerReqDto.getJobName())
                .jobGroup(schedulerReqDto.getJobGroup() != null ? schedulerReqDto.getJobGroup() : "DEFAULT")
                .jobClass(schedulerReqDto.getJobClass())
                .cronExpression(schedulerReqDto.getCronExpression())
                .description(schedulerReqDto.getDescription())
                .jobType(schedulerReqDto.getJobType())
                .jobData(schedulerReqDto.jobData)
                .status(ScheduledJob.Status.NEW)
                .build();
    }
}