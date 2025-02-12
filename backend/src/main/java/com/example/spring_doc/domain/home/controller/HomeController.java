package com.example.spring_doc.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "HomeController", description = "API 서버 홈")
@RestController
public class HomeController {

    @Operation(summary = "API 서버 시작페이지", description = "API 서버 시작페이지입니다. api 호출은 인증 해주세요.")
    @GetMapping("/")
    public String home() {
        return "API 서버에 오신 것을 환영합니다.";
    }
}
