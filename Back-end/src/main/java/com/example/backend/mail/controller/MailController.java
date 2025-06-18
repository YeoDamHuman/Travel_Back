package com.example.backend.mail.controller;

import com.example.backend.mail.dto.request.MailRequest;
import com.example.backend.mail.dto.response.MailResponse;
import com.example.backend.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {
    private final MailService mailService;

    // 1️⃣ 인증 메일 발송 Controller
    @PostMapping("/send")
    public ResponseEntity<MailResponse.mailSendResponse> send(@RequestBody MailRequest.mailSendRequest email) {
        MailResponse.mailSendResponse response = mailService.sendVerificationEmail(email);
        return ResponseEntity.ok(response);
    }

    // 2️⃣ 검증 Controller
    @PostMapping("/verify")
    public ResponseEntity<MailResponse.mailVerifyResponse> verify(@RequestBody MailRequest.mailVerifyRequest token) {
        MailResponse.mailVerifyResponse response = mailService.verifyToken(token);

        if (response.getMessage().equals("이메일 인증이 완료되었습니다.")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

}
