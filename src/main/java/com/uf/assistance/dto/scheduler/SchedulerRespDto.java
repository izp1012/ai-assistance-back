package com.uf.assistance.dto.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.domain.scheduler.ScheduledJob;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class SchedulerRespDto {

    private Long id;
    private String jobName;
    private String jobGroup;
    private String cronExpression;
    private String jobType;
    private Map<String, Object> jobData;
    private ScheduledJob.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SchedulerRespDto from(ScheduledJob scheduledJob){

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> parsedJobData = null;

        try {
            if (scheduledJob.getJobData() != null) {
                parsedJobData = mapper.readValue(scheduledJob.getJobData(), new TypeReference<>() {});
            }
        } catch (Exception e) {
            throw new RuntimeException("jobData 역직렬화 실패", e);
        }

        SchedulerRespDto schedulerRespDto = new SchedulerRespDto();
        schedulerRespDto.setId(scheduledJob.getId());
        schedulerRespDto.setJobName(scheduledJob.getJobName());
        schedulerRespDto.setJobGroup(scheduledJob.getJobGroup());
        schedulerRespDto.setCronExpression(scheduledJob.getCronExpression());
        schedulerRespDto.setJobType(scheduledJob.getJobType());
        schedulerRespDto.setJobData(parsedJobData);
        schedulerRespDto.setStatus(scheduledJob.getStatus());
        schedulerRespDto.setCreatedAt(scheduledJob.getCreatedAt());
        schedulerRespDto.setUpdatedAt(scheduledJob.getUpdatedAt());
        
        return schedulerRespDto;
    }
}