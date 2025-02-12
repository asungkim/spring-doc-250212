package com.example.spring_doc.domain.member.member.service;

import com.example.spring_doc.domain.member.member.entity.Member;
import com.example.spring_doc.standard.Ut;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthTokenServiceTest {

    @Autowired
    private AuthTokenService authTokenService;
    @Autowired
    private MemberService memberService;

    @Value("${custom.jwt.secret-key}")
    private String keyString;
    @Value("${custom.jwt.expire-seconds}")
    private int expireSeconds;

    SecretKey secretKey = Keys.hmacShaKeyFor("abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890".getBytes());

    @Test
    @DisplayName("AuthTokenService 생성")
    void init() {
        assertThat(authTokenService).isNotNull();
    }

    @Test
    @DisplayName("jwt 생성")
    void createToken() {
        int expireSeconds = 60 * 60 * 24 * 365;
        Map<String, Object> original = Map.of("name", "paul", "age", 23);
        String jwtStr = Ut.Jwt.createToken(keyString, expireSeconds, original);

        assertThat(jwtStr).isNotBlank();
        Map<String, Object> parsePayload = Ut.Jwt.getPayload(keyString, jwtStr);

        assertThat(parsePayload).containsAllEntriesOf(original);

    }

    @Test
    @DisplayName("access token 생성")
    void accessToken() {
        // jwt -> access token
        Member member = memberService.findByUsername("user1").get();

        String accessToken = authTokenService.genAccessToken(member);
        assertThat(accessToken).isNotBlank();
        System.out.println("AccessToken = " + accessToken);
    }

    @Test
    @DisplayName("jwt valid check")
    void checkValid() {

        Member member = memberService.findByUsername("user1").get();
        String accessToken = authTokenService.genAccessToken(member);

        boolean isValid = Ut.Jwt.isValidToken(keyString, accessToken);
        assertThat(isValid).isTrue();

        Map<String, Object> parsedPayLoad = authTokenService.getPayload(accessToken);

        assertThat(parsedPayLoad).containsAllEntriesOf(
                Map.of("id", member.getId(), "username", member.getUsername())
        );
    }
}
