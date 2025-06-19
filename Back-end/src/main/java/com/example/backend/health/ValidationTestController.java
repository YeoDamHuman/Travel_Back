package com.example.backend.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/health/validation")
@Tag(name = "ValidationTestAPI", description = "검증 설정 테스트 API")
@Validated
@Slf4j
public class ValidationTestController {

    @PostMapping("/test")
    @Operation(summary = "검증 테스트", description = "다양한 검증 어노테이션 테스트")
    public ResponseEntity<?> testValidation(
            @Valid @RequestBody ValidationTestDto request,
            BindingResult bindingResult) {

        log.info("Validation 테스트 요청: {}", request);

        // 검증 오류가 있는 경우
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "검증 실패");
            errorResponse.put("errors", bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            error -> error.getField(),
                            error -> error.getDefaultMessage()
                    )));

            log.warn("Validation 실패: {}", errorResponse);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 검증 성공
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", "검증 통과!");
        successResponse.put("data", request);

        log.info("Validation 성공: {}", request);
        return ResponseEntity.ok(successResponse);
    }

    @GetMapping("/param-test")
    @Operation(summary = "파라미터 검증 테스트", description = "URL 파라미터 검증 테스트")
    public ResponseEntity<Map<String, Object>> testParamValidation(
            @RequestParam @NotBlank(message = "이름은 필수입니다") String name,
            @RequestParam @Min(value = 1, message = "나이는 1 이상이어야 합니다")
            @Max(value = 150, message = "나이는 150 이하여야 합니다") Integer age) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "파라미터 검증 통과!");
        response.put("name", name);
        response.put("age", age);

        return ResponseEntity.ok(response);
    }

    // 올바른 요청 예제를 위한 엔드포인트 추가
    @GetMapping("/example")
    @Operation(summary = "올바른 요청 예제", description = "올바른 validation 요청 데이터 예제를 반환")
    public ResponseEntity<Map<String, Object>> getValidationExample() {
        Map<String, Object> example = new HashMap<>();
        example.put("name", "홍길동");
        example.put("age", 25);
        example.put("email", "test@example.com");
        example.put("phone", "010-1234-5678");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "올바른 validation 요청 데이터 예제");
        response.put("example", example);

        return ResponseEntity.ok(response);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationTestDto {
        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 10, message = "이름은 2-10자 사이여야 합니다")
        private String name;

        @NotNull(message = "나이는 필수입니다")
        @Min(value = 1, message = "나이는 1 이상이어야 합니다")
        @Max(value = 150, message = "나이는 150 이하여야 합니다")
        private Integer age;

        @Email(message = "올바른 이메일 형식이 아닙니다")
        @NotBlank(message = "이메일은 필수입니다")
        private String email;

        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (010-1234-5678)")
        private String phone;

        @Override
        public String toString() {
            return "ValidationTestDto{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    '}';
        }
    }
}