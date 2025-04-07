package com.uf.assistance.web;

import com.uf.assistance.domain.ai.BaseAI;
import com.uf.assistance.domain.ai.CustomAI;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.ai.BaseAIReqDto;
import com.uf.assistance.dto.ai.BaseAIRespDto;
import com.uf.assistance.dto.ai.CustomAIReqDto;
import com.uf.assistance.dto.ai.CustomAIRespDto;
import com.uf.assistance.service.AIService;
import com.uf.assistance.service.FileStorageService;
import com.uf.assistance.service.UserService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/ai")
@Tag(name = "AI", description = "AI CRUD 관련 API")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AiController {
    private final AIService aiService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

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
    public ResponseEntity<?> createCustomAI(@RequestPart("jsonData") CustomAIReqDto customAIReqDto, @RequestPart("image") MultipartFile file) {
        CustomAIRespDto customAIRespDto = aiService.createCustomAI(customAIReqDto, file);

        return new ResponseEntity<>(new ResponseDto<>(1, "Custom AI 생성 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), customAIRespDto), HttpStatus.OK);
    }


    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String filename) {
        Resource resource = fileStorageService.loadFileAsResource(filename);

        // 파일 확장자에 따라 MediaType 결정
        MediaType mediaType = getMediaTypeForFileName(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 이미지 타입에 맞게 수정
                .body(resource);
    }

    // 파일명에 따라 적절한 MediaType 반환
    private MediaType getMediaTypeForFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

}
