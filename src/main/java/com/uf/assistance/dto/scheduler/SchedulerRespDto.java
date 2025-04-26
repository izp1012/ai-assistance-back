package com.uf.assistance.dto.scheduler;

import com.uf.assistance.domain.scheduler.ScheduledJob;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SchedulerRespDto {

    private Long id;
    private String jobName;
    private String jobGroup;
    private String cronExpression;
    private String jobType;
    private String jobData;
    private ScheduledJob.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SchedulerRespDto from(ScheduledJob scheduledJob){
        SchedulerRespDto response = new SchedulerRespDto();
        response.setId(scheduledJob.getId());
        response.setJobName(scheduledJob.getJobName());
        response.setJobGroup(scheduledJob.getJobGroup());
        response.setCronExpression(scheduledJob.getCronExpression());
        response.setJobType(scheduledJob.getJobType());
        response.setJobData(scheduledJob.getJobData());
        response.setStatus(scheduledJob.getStatus());
        response.setCreatedAt(scheduledJob.getCreatedAt());
        response.setUpdatedAt(scheduledJob.getUpdatedAt());
        return response;
    }
}