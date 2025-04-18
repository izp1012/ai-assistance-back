package com.uf.assistance.web;

import com.uf.assistance.domain.ai.BaseAI;
import com.uf.assistance.domain.ai.CustomAI;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.ai.BaseAIReqDto;
import com.uf.assistance.dto.ai.BaseAIRespDto;
import com.uf.assistance.dto.ai.CustomAIReqDto;
import com.uf.assistance.dto.ai.CustomAIRespDto;
import com.uf.assistance.service.AIService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/ai")
@Tag(name = "AI", description = "AI CRUD 관련 API")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AiController {
    private final AIService aiService;

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
    @Tag(name = "CustomAI 조회", description = "Parameter 에 Y/N 둘 중 하나 입력")
    public ResponseEntity<ResponseDto<List<CustomAIRespDto>>> getAllCustomAIs(
            @RequestParam(value = "creator", required = false) String creator,
            @RequestParam(value = "active", required = false) String active,
            @RequestParam(value = "hidden", required = false) String hidden ) {

        List<CustomAI> list;

        Boolean isActive = active != null ? "Y".equalsIgnoreCase(active) : null;
        Boolean isHidden = hidden != null ? "Y".equalsIgnoreCase(hidden) : null;

        list = aiService.getCustomAIs(isActive, isHidden, creator); // 부분 조건 적용

        List<CustomAIRespDto> customAIRespDtoList = list.stream()
                .map(CustomAIRespDto::from)
                .collect(Collectors.toList());

        return new ResponseEntity<>(new ResponseDto<>(1, "CustomAI 리스트 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), customAIRespDtoList), HttpStatus.OK);
    }

    @GetMapping("/custom/{aiId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseDto<CustomAIRespDto>> getCustomAI(@PathVariable Long aiId) {
        CustomAI customAI =  aiService.getCustomAIById(aiId);
        CustomAIRespDto aiRespDto = CustomAIRespDto.from(customAI);

        return new ResponseEntity<>(new ResponseDto<>(1, "CustomAI 조회 ID : "+aiId, CustomDateUtil.toStringFormat(LocalDateTime.now()), aiRespDto), HttpStatus.OK);
    }

    @PostMapping("/base/create")
    public ResponseEntity<?> createBaseAI(@RequestBody BaseAIReqDto baseAIReqDto) {

        BaseAIRespDto baseAIRespDto = aiService.createBaseAI(baseAIReqDto);

        return new ResponseEntity<>(new ResponseDto<>(1, "Base AI 생성 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), baseAIRespDto), HttpStatus.OK);
    }

    @PostMapping(value = "/custom/create", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createCustomAI(@RequestPart("jsonData") CustomAIReqDto customAIReqDto, @RequestPart(value ="image", required = false) MultipartFile file) {
        CustomAIRespDto customAIRespDto = aiService.createCustomAI(customAIReqDto, file);

        return new ResponseEntity<>(new ResponseDto<>(1, "Custom AI 생성 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), customAIRespDto), HttpStatus.OK);
    }

    @PutMapping(value = "/custom/update", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updateCustomAI(@RequestPart("jsonData") CustomAIReqDto customAIReqDto, @RequestPart(value ="image", required = false) MultipartFile file) {
        CustomAIRespDto customAIRespDto = aiService.updateCustomAI(customAIReqDto, file);

        return new ResponseEntity<>(new ResponseDto<>(1, "Custom AI 변경 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), customAIRespDto), HttpStatus.OK);
    }

}
