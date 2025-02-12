package com.example.spring_doc.domain.member.member.service;

import com.example.spring_doc.domain.member.member.entity.Member;
import com.example.spring_doc.standard.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    @Value("${custom.jwt.secret-key}")
    private String keyString;
    @Value("${custom.jwt.expire-seconds}")
    private int expireSeconds;

    String genAccessToken(Member member) {

        return Ut.Jwt.createToken(
                keyString,
                expireSeconds,
                Map.of("id", member.getId(), "username", member.getUsername())
        );

    }

    Map<String, Object> getPayload(String token) {

        if (!Ut.Jwt.isValidToken(keyString, token)) {
            return null;
        }

        Map<String, Object> payLoad = Ut.Jwt.getPayload(keyString, token);


        Number idNo = (Number) payLoad.get("id");
        long id = idNo.longValue();

        String username = (String) payLoad.get("username");

        return Map.of("id", id, "username", username);
    }
}
