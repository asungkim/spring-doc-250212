package com.example.spring_doc.global.init;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

@Profile("dev")
@Configuration
public class DevInitData {

    @Bean
    ApplicationRunner devApplicationRunner() {
        return args -> {
            genApiJsonFile("http://localhost:8080/v3/api-docs/apiV1", "apiV1.json");
            runCmdJsonToTs();
        };
    }

    public void runCmdJsonToTs() {
        String[] command = {
                "npx", "--package", "typescript",
                "--package", "openapi-typescript",
                "--package", "punycode",
                "openapi-typescript", "apiV1.json", "-o", "schema.d.ts"
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // 표준 에러 출력도 읽기 위해 설정

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 터미널 출력 내용 확인
            }

            int exitCode = process.waitFor();
            System.out.println("프로세스 종료 코드: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void genApiJsonFile(String url, String filename) throws Exception {
        Path outputFile = Path.of(filename); // 저장할 파일 경로

        try {
            // HttpClient 생성
            HttpClient client = HttpClient.newHttpClient();

            // GET 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // 요청 실행 및 응답 받기
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 응답 코드 확인 (200 OK)
            if (response.statusCode() == 200) {
                // JSON 데이터를 파일로 저장
                Files.writeString(outputFile, response.body());
                System.out.println("JSON 파일 저장 완료: " + outputFile.toAbsolutePath());
            } else {
                System.err.println("오류: HTTP 응답 코드 " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
