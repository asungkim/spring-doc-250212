package com.example.spring_doc.global.security;

import com.example.spring_doc.domain.member.member.entity.Member;
import com.example.spring_doc.domain.member.member.service.MemberService;
import com.example.spring_doc.global.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final Rq rq;
    private final MemberService memberService;

    private boolean isAuthorizationHeader() {
        String authorizationHeader = rq.getHeader("Authorization");

        if (authorizationHeader == null) {
            return false;
        }

        return authorizationHeader.startsWith("Bearer ");
    }

    record AuthToken(String apiKey, String accessToken) {
    }

    private AuthToken getAuthTokenFromRequest() {
        if (isAuthorizationHeader()) {
            String authorizationHeader = rq.getHeader("Authorization");
            String authToken = authorizationHeader.replaceAll("Bearer ", "");

            String[] tokenBits = authToken.split(" ");

            if (tokenBits.length < 2) {
                return null;
            }

            return new AuthToken(tokenBits[0], tokenBits[1]);
        }

        String accessToken = rq.getValueFromCookie("accessToken");
        String apiKey = rq.getValueFromCookie("apiKey");

        if (accessToken == null || apiKey == null) {
            return null;
        }

        return new AuthToken(apiKey, accessToken);
    }

    private Member getMemberByAccessToken(String apiKey, String accessToken) {
        Optional<Member> opAccMember = memberService.getMemberByAccessToken(accessToken);

        if (opAccMember.isPresent()) {
            return opAccMember.get();
        }

        Optional<Member> opApiMember = memberService.findByApiKey(apiKey);

        if (opApiMember.isEmpty()) {
            return null;
        }

        String newAccessToken = memberService.genAccessToken(opApiMember.get());
        rq.addCookie("accessToken", newAccessToken);
        rq.addCookie("apiKey", apiKey);

        return opApiMember.get();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String url = request.getRequestURI();

        if (List.of("/api/v1/members/login", "/api/v1/members/join", "/api/v1/members/logout").contains(url)) {
            filterChain.doFilter(request, response);
            return;
        }

        AuthToken tokens = getAuthTokenFromRequest();
        if (tokens == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 재발급 코드
        Member writer = getMemberByAccessToken(tokens.apiKey, tokens.accessToken);
        if (writer == null) {
            filterChain.doFilter(request, response);
            return;
        }

        rq.setLogin(writer);
        filterChain.doFilter(request, response);
    }
}
