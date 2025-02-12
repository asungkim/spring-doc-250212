package com.example.spring_doc.domain.member.member.controller;

import com.example.spring_doc.domain.member.member.dto.MemberDto;
import com.example.spring_doc.domain.member.member.entity.Member;
import com.example.spring_doc.domain.member.member.service.MemberService;
import com.example.spring_doc.global.Rq;
import com.example.spring_doc.global.dto.RsData;
import com.example.spring_doc.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

    private final MemberService memberService;
    private final Rq rq;

    record JoinReqBody(@NotBlank String username,
                       @NotBlank String password,
                       @NotBlank String nickname) {
    }

    @PostMapping("/join")
    public RsData<MemberDto> join(@Valid @RequestBody JoinReqBody body) {

        memberService.findByUsername(body.username())
                .ifPresent(_ -> {
                            throw new ServiceException("409-1", "이미 사용중인 아이디입니다.");
                        }
                );

        Member member = memberService.join(body.username(), body.password(), body.nickname());
        return new RsData<>(
                "201-1",
                "회원가입이 완료되었습니다.",
                new MemberDto(member));
    }

    record LoginReqBody(@NotBlank
                        String username,
                        @NotBlank
                        String password) {
    }

    record LoginResBody(MemberDto item, String apiKey, String accessToken) {
    }

    @PostMapping("/login")
    public RsData<LoginResBody> login(@Valid @RequestBody LoginReqBody body, HttpServletResponse response) {
        Member member = memberService.findByUsername(body.username()).orElseThrow(
                () -> new ServiceException("401-1", "존재하지 않는 아이디입니다.")
        );

        String accessToken = memberService.genAccessToken(member);

        rq.addCookie("accessToken", accessToken);
        rq.addCookie("apiKey", member.getApiKey());

        if (!member.getPassword().equals(body.password())) {
            throw new ServiceException("401-1", "비밀번호가 일치하지 않습니다.");
        }

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(member.getNickname()),
                new LoginResBody(
                        new MemberDto(member),
                        member.getApiKey(),
                        accessToken)
        );
    }

    @DeleteMapping("/logout")
    public RsData<Void> logout() {
        rq.removeCookie("accessToken");
        rq.removeCookie("apiKey");

        return new RsData<>("200-1", "로그아웃 되었습니다.");
    }


    @GetMapping("/me")
    public RsData<MemberDto> me() {

        Member actor = rq.getActor();
        Member realActor = rq.getRealActor(actor);

        return new RsData<>(
                "200-1",
                "내 정보 조회가 완료되었습니다.",
                new MemberDto(realActor)
        );
    }
}
