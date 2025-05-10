package com.uf.assistance.batchjob;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.domain.chat.MessageType;
import com.uf.assistance.dto.message.ChatReqDto;
import com.uf.assistance.dto.message.ChatRespDto;
import com.uf.assistance.service.ChatService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class DynamicQuartzJob implements Job {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;

    public DynamicQuartzJob(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException{
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        String jobGroup = context.getJobDetail().getKey().getGroup();
        String jobName = context.getJobDetail().getKey().getName();
        String jobData = jobDataMap.getString("jobData");
        logger.info("스케줄된 작업이 실행되었습니다. Group : {}, Name : {}",jobGroup, jobName);


        // 로직 분기
        switch (jobGroup) {
            case "SendMessageAI":
                JobDataMap dataMap = context.getJobDetail().getJobDataMap();

                logger.info("Job '{}' executed.", dataMap.toString());

                ObjectMapper objectMapper = new ObjectMapper();
                // Map으로 변환
                try {
                    HashMap<String, String> map = objectMapper.readValue(jobData, HashMap.class);
                    String prompt = map.get("prompt");
                    String senderName = map.get("senderName");
                    Long subscriptionId = Long.parseLong(map.get("subscriptionId"));

                    logger.info("Job '{}' prompt {}, senderName : {}, subscriptionId : {}", prompt, senderName, subscriptionId);
                    if (prompt == null || senderName == null || subscriptionId == null) {
                        throw new JobExecutionException("필수 데이터 누락!");
                    }

                    // 실제 로직 수행 (예: 메시지 전송)
                    logger.info("Job data: {}", dataMap);
                    ChatReqDto chatReqDto = ChatReqDto.builder()
                            .sender(senderName)
                            .content(prompt)
                            .build();

                    ChatRespDto userChatDto = chatService.sendMessage(chatReqDto, subscriptionId, MessageType.USER);
                    ChatRespDto aiChatDto = chatService.sendMessageAI(chatReqDto, subscriptionId, MessageType.ASSISTANT);

                    messagingTemplate.convertAndSend("/topic/public/ai/" + subscriptionId, userChatDto);
                    messagingTemplate.convertAndSend("/topic/public/ai/" + subscriptionId, aiChatDto);

                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                break;
            case "CheckData":
                // 데이터 검증 로직
                break;
            default:
                throw new IllegalArgumentException("Unknown jobGroup: " + jobGroup);
        }
    }
}
