package com.uf.assistance.web;

import com.uf.assistance.domain.ai.AI;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.ai.AIReqDto;
import com.uf.assistance.dto.ai.AIRespDto;
import com.uf.assistance.service.AIService;
import com.uf.assistance.util.CustomDateUtil;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/ai")
public class AiController {
    private final AIService aiService;
    private final ChatModel chatModel;
    private final RetryOperations retryOperations;

    @Autowired
    public AiController(AIService aiService, ChatModel chatModel, RetryOperations retryOperations) {
        this.aiService = aiService;
        this.chatModel = chatModel;
        this.retryOperations = retryOperations;
    }

    @GetMapping("/all")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseDto<List<AIRespDto>>> getAllAIs() {
        List<AI> list =  aiService.getAvailableAIs();
        List<AIRespDto> aiRespList = new ArrayList<>();

        for(AI ai : list) {
            AIRespDto aiRespDto = AIRespDto.from(ai);
            aiRespList.add(aiRespDto);
        }
        return new ResponseEntity<>(new ResponseDto<>(1, "모든 AI 리스트 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), aiRespList), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody AIReqDto aiReqDto) {

        AIRespDto aiRespDto = aiService.createAI(aiReqDto);

        return new ResponseEntity<>(new ResponseDto<>(1, "AI 생성 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), aiRespDto), HttpStatus.OK);
    }
}
