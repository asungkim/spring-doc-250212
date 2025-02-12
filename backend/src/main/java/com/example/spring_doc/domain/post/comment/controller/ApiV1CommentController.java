package com.example.spring_doc.domain.post.comment.controller;

import com.example.spring_doc.domain.member.member.entity.Member;
import com.example.spring_doc.domain.post.comment.dto.CommentDto;
import com.example.spring_doc.domain.post.comment.entity.Comment;
import com.example.spring_doc.domain.post.post.entity.Post;
import com.example.spring_doc.domain.post.post.service.PostService;
import com.example.spring_doc.global.Rq;
import com.example.spring_doc.global.dto.RsData;
import com.example.spring_doc.global.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ApiV1CommentController", description = "댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
public class ApiV1CommentController {

    private final PostService postService;
    private final Rq rq;

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(
            summary = "댓글 목록 조회",
            description = "게시글의 댓글 목록을 가져옵니다."
    )
    public List<CommentDto> getItems(@PathVariable long postId) {
        Post post = postService.getItem(postId).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
        );

        return post.getComments()
                .stream()
                .map(CommentDto::new)
                .toList();
    }

    record WriteReqBody(String content) {
    }

    @PostMapping()
    @Transactional
    @Operation(
            summary = "댓글 작성",
            description = "게시글의 댓글을 작성합니다."
    )
    public RsData<Void> write(@PathVariable long postId, @RequestBody WriteReqBody body) {
        Member writer = rq.getActor();
        Comment comment = _write(postId, writer, body.content());

        postService.flush();

        return new RsData<>("201-1",
                "%d 번 댓글 작성이 완료되었습니다.".formatted(comment.getId()));
    }

    public Comment _write(long postId, Member writer, String content) {
        Post post = postService.getItem(postId).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
        );

        return post.addComment(writer, content);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(
            summary = "댓글 상세",
            description = "게시글의 댓글 상세 정보를 가져옵니다."
    )
    public CommentDto getItem(@PathVariable long postId, @PathVariable long id) {
        Post post = postService.getItem(postId).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
        );

        Comment comment = post.getCommentById(id);
        return new CommentDto(comment);
    }

    record ModifyReqBody(String content) {
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(
            summary = "댓글 수정",
            description = "게시글의 댓글을 수정합니다."
    )
    public RsData<Void> modify(@PathVariable long postId, @PathVariable long id, @RequestBody ModifyReqBody body) {
        Member writer = rq.getActor();

        Post post = postService.getItem(postId).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
        );

        Comment comment = post.getCommentById(id);

        if (comment.canModify(writer)) {
            comment.modify(body.content());
        }


        return new RsData<>("200-1",
                "%d번 댓글이 수정되었습니다.".formatted(id));

    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(
            summary = "댓글 삭제",
            description = "게시글의 댓글을 삭제합니다."
    )
    public RsData<Void> delete(@PathVariable long postId, @PathVariable long id) {

        Member writer = rq.getActor();
        Post post = postService.getItem(postId).orElseThrow(
                () -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
        );

        Comment comment = post.getCommentById(id);

        if (comment.canDelete(writer)) {
            post.deleteComment(comment);
        }

        return new RsData<>("200-1",
                "%d번 댓글이 삭제되었습니다.".formatted(id));
    }
}
