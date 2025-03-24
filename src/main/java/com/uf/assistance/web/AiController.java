package com.uf.assistance.web;

import com.uf.assistance.domain.ai.BaseAI;
import com.uf.assistance.domain.ai.CustomAI;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.ai.BaseAIReqDto;
import com.uf.assistance.dto.ai.BaseAIRespDto;
import com.uf.assistance.dto.ai.CustomAIReqDto;
import com.uf.assistance.dto.ai.CustomAIRespDto;
import com.uf.assistance.service.AIService;
import com.uf.assistance.service.UserService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth/ai")
@Tag(name = "AI", description = "AI CRUD 관련 API")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AiController {
    private final AIService aiService;
    private final UserService userService;

    @GetMapping("/base/all")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseDto<List<BaseAIRespDto>>> getAllBaseAIs() {
        List<BaseAI> list =  aiService.getAvailableBaseAIs();
        List<BaseAIRespDto> baseAIRespDtoList = new ArrayList<>();

        for(BaseAI ai : list) {
            BaseAIRespDto aiRespDto = BaseAIRespDto.from(ai);
            baseAIRespDtoList.add(aiRespDto);
        }
        return new ResponseEntity<>(new ResponseDto<>(1, "모든 BaseAI 리스트 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), baseAIRespDtoList), HttpStatus.OK);
    }
    @GetMapping("/custom/all")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseDto<List<CustomAIRespDto>>> getAllCustomAIs() {
        List<CustomAI> list =  aiService.getAvailableCustomAIs();
        List<CustomAIRespDto> customAIRespDtoList = new ArrayList<>();

        for(CustomAI ai : list) {
            CustomAIRespDto aiRespDto = CustomAIRespDto.from(ai);
            customAIRespDtoList.add(aiRespDto);
        }
        return new ResponseEntity<>(new ResponseDto<>(1, "모든 CustomAI 리스트 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), customAIRespDtoList), HttpStatus.OK);
    }

    @PostMapping("/base/create")
    public ResponseEntity<?> createBaseAI(@RequestBody BaseAIReqDto baseAIReqDto) {

        BaseAIRespDto baseAIRespDto = aiService.createBaseAI(baseAIReqDto);

        return new ResponseEntity<>(new ResponseDto<>(1, "AI 생성 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), baseAIRespDto), HttpStatus.OK);
    }

    @PostMapping("/custom/create")
    public ResponseEntity<?> createCustomAI(@RequestBody CustomAIReqDto customAIReqDto) {

        CustomAIRespDto customAIRespDto = aiService.createCustomAI(customAIReqDto);

        return new ResponseEntity<>(new ResponseDto<>(1, "AI 생성 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), customAIRespDto), HttpStatus.OK);
    }
}
