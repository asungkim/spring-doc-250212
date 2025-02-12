package com.example.spring_doc.domain.post.post.controller;

import com.example.spring_doc.domain.member.member.entity.Member;
import com.example.spring_doc.domain.member.member.service.MemberService;
import com.example.spring_doc.domain.post.post.dto.PageDto;
import com.example.spring_doc.domain.post.post.dto.PostWithContentDto;
import com.example.spring_doc.domain.post.post.entity.Post;
import com.example.spring_doc.domain.post.post.service.PostService;
import com.example.spring_doc.global.Rq;
import com.example.spring_doc.global.dto.RsData;
import com.example.spring_doc.global.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ApiV1PostController", description = "글 관련 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {

    private final PostService postService;
    private final Rq rq;
    private final MemberService memberService;

    record StatisticsResBody(long postCount, long postPublishedCount, long postListedCount) {
    }

    @Operation(summary = "통계 조회")
    @GetMapping("/statistics")
    public RsData<StatisticsResBody> getStatistics() {

        Member actor = rq.getActor();

        if (!actor.isAdmin()) {
            return new RsData<>(
                    "403-1",
                    "접근 권한이 없습니다."
            );
        }

        return new RsData<>(
                "200-1",
                "통계 조회가 완료되었습니다.",
                new StatisticsResBody(
                        10,
                        10,
                        10
                )
        );
    }


    @Operation(
            summary = "글 목록 조회",
            description = "페이징 처리와 검색 기능")
    @GetMapping
    @Transactional(readOnly = true)
    public RsData<PageDto> getItems(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "3") int pageSize,
                                    @RequestParam(defaultValue = "title") String keywordType,
                                    @RequestParam(defaultValue = "") String keyword) {
        Page<Post> postPage = postService.getListedItems(page, pageSize, keywordType, keyword);

        return new RsData<>(
                "200-1",
                "글 목록 조회가 완료되었습니다.",
                new PageDto(postPage)
        );

    }

    @Operation(
            summary = "내 글 목록 조회",
            description = "페이징 처리와 검색 기능")
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public RsData<PageDto> getMines(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int pageSize,
            @RequestParam(defaultValue = "title") String keywordType,
            @RequestParam(defaultValue = "") String keyword) {

        Member actor = rq.getActor();
        Page<Post> pagePost = postService.getMines(actor, page, pageSize, keywordType, keyword);

        return new RsData<>(
                "200-1",
                "내 글 목록 조회가 완료되었습니다.",
                new PageDto(pagePost)
        );
    }

    @Operation(
            summary = "글 단건 조회",
            description = "비밀글은 작성자만 조회 가능 ")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public RsData<PostWithContentDto> getItem(@PathVariable long id) {
        Post post = postService.getItem(id).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 글입니다.")
        );

        // 비공개 글인 경우에만 인증을 하고 읽어본다. 공개글인 경우 로그인 하지 않은 사람도 확인 가능
        if (!post.isPublished()) {
            Member actor = rq.getActor();
            post.canRead(actor);
        }

        return new RsData<>(
                "200-1",
                "%d번 글을 조회하였습니다.".formatted(id),
                new PostWithContentDto(post)
        );
    }

    record WriteReqBody(@NotBlank String title,
                        @NotBlank String content,
                        boolean published,
                        boolean listed) {
    }

    @PostMapping
    @Transactional
    @Operation(
            summary = "글 작성",
            description = "로그인 한 사용자만 글 작성 가능"
    )
    public RsData<PostWithContentDto> write(@Valid @RequestBody WriteReqBody body) {

        Member actor = rq.getActor();
        Member realActor = rq.getRealActor(actor);

//        Member actor=Member.builder()
//                .id(1L)
//                .build();

        Post post = postService.write(realActor, body.title(), body.content(), body.published(), body.listed());

        return new RsData<>(
                "201-1",
                "%d번 글 작성 완료되었습니다.".formatted(post.getId()),
                new PostWithContentDto(post)
        );
    }

    record ModifyReqBody(@NotBlank String title, @NotBlank String content) {
    }

    @PutMapping("{id}")
    @Transactional
    @Operation(
            summary = "글 수정",
            description = "작성자와 관리자만 글 수정 가능"
    )
    public RsData<PostWithContentDto> modify(@Valid @RequestBody ModifyReqBody body,
                                             @PathVariable long id) {
        Member actor = rq.getActor();

        Post post = postService.getItem(id).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 글입니다.")
        );

        if (post.canModify(actor)) {
            postService.modify(post, body.title(), body.content());
        }

        return new RsData<>(
                "200-1",
                "%d번 글 수정이 완료되었습니다.".formatted(post.getId()),
                new PostWithContentDto(post)
        );
    }

    @DeleteMapping("{id}")
    @Transactional
    @Operation(
            summary = "글 삭제",
            description = "작성자와 관리자만 글 삭제 가능"
    )
    public RsData<Void> delete(@PathVariable long id) {
        Member actor = rq.getActor();

        Post post = postService.getItem(id).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 글입니다.")
        );

        if (post.canDelete(actor)) {
            postService.delete(post);
        }

        return new RsData<>(
                "200-1",
                "%d번 글 삭제 완료되었습니다.".formatted(id)
        );
    }


}
