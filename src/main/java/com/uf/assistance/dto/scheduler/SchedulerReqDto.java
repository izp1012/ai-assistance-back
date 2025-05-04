package com.uf.assistance.dto.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.domain.ai.AISubscription;
import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.domain.scheduler.Status;
import com.uf.assistance.domain.user.User;
import lombok.Data;

import java.util.Map;

@Data
public class SchedulerReqDto {

    private String jobName;
    private String jobGroup;
    private String jobClass;
    private String cronExpression;
    private String description;
    private String jobType;
    private Map<String, Object> jobData;
    private Status status;
    private Long aisubscriptionId;
    private String userId;

    public static ScheduledJob toEntity(SchedulerReqDto schedulerReqDto, AISubscription aisubscription, User user) {

        ObjectMapper mapper = new ObjectMapper();
        String jobDataJson = null;
        try {
            jobDataJson = mapper.writeValueAsString(schedulerReqDto.getJobData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("jobData 직렬화 실패", e);
        }

        return ScheduledJob.builder()
                .jobName(schedulerReqDto.getJobName())
                .jobGroup(schedulerReqDto.getJobGroup() != null ? schedulerReqDto.getJobGroup() : "DEFAULT")
                .jobClass(schedulerReqDto.getJobClass())
                .cronExpression(schedulerReqDto.getCronExpression())
                .description(schedulerReqDto.getDescription())
                .jobType(schedulerReqDto.getJobType())
                .jobData(jobDataJson)
                .status(schedulerReqDto.getStatus() != null? schedulerReqDto.getStatus() : null)
                .aiSubscription(aisubscription)
                .user(user)
                .build();
    }
}