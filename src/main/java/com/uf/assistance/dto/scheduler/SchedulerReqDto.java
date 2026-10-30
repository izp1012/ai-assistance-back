package com.uf.assistance.dto.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.batchjob.DynamicQuartzJob;
import com.uf.assistance.domain.ai.AISubscription;
import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.domain.scheduler.Status;
import com.uf.assistance.domain.user.User;
import lombok.Data;

import java.util.HashMap;
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

        // 실행 시 DynamicQuartzJob이 jobData에서 subscriptionId를 읽으므로 함께 저장
        Map<String, Object> jobData = new HashMap<>();
        if (schedulerReqDto.getJobData() != null) {
            jobData.putAll(schedulerReqDto.getJobData());
        }
        jobData.put("subscriptionId", String.valueOf(aisubscription.getId()));

        ObjectMapper mapper = new ObjectMapper();
        String jobDataJson = null;
        try {
            jobDataJson = mapper.writeValueAsString(jobData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("jobData 직렬화 실패", e);
        }

        return ScheduledJob.builder()
                .jobName(schedulerReqDto.getJobName())
                .jobGroup(schedulerReqDto.getJobGroup() != null ? schedulerReqDto.getJobGroup() : "DEFAULT")
                .jobClass(schedulerReqDto.getJobClass() != null ? schedulerReqDto.getJobClass() : DynamicQuartzJob.class.getName())
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