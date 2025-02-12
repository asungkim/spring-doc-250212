package com.example.spring_doc.domain.post.comment.controller;

import com.example.spring_doc.domain.member.member.entity.Member;
import com.example.spring_doc.domain.member.member.service.MemberService;
import com.example.spring_doc.domain.post.comment.entity.Comment;
import com.example.spring_doc.domain.post.post.entity.Post;
import com.example.spring_doc.domain.post.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    PostService postService;

    @Autowired
    private MemberService memberService;
    private Member loginedMember;
    private String token;

    @BeforeEach
    void login() {
        loginedMember = memberService.findByUsername("user1").get();
        token = memberService.getAuthToken(loginedMember);
    }

    @Test
    @DisplayName("댓글 작성")
    void write() throws Exception {
        long postId = 1;
        String content = "댓글 내용";


        String requestBody = """
                {
                    "content": "%s"
                }
                """.formatted(content).stripIndent();

        ResultActions resultActions = mvc.perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andDo(print());

        Post post = postService.getItem(postId).get();
        Comment comment = post.getLatestComment();

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d 번 댓글 작성이 완료되었습니다.".formatted(comment.getId())));
    }

    @Test
    @DisplayName("댓글 수정")
    void modify() throws Exception {
        long postId = 1;
        long commentId = 1;
        String content = "수정된 댓글 내용";


        String requestBody = """
                {
                    "content": "%s"
                }
                """.formatted(content).stripIndent();

        ResultActions resultActions = mvc.perform(
                        put("/api/v1/posts/%d/comments/%d".formatted(postId,commentId))
                                .header("Authorization", "Bearer " + token)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 수정되었습니다.".formatted(commentId)));
    }

    @Test
    @DisplayName("댓글 삭제")
    void delete1() throws Exception {
        long postId = 1;
        long commentId = 1;


        ResultActions resultActions = mvc.perform(
                        delete("/api/v1/posts/%d/comments/%d".formatted(postId, commentId))
                                .header("Authorization", "Bearer " + token)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 삭제되었습니다.".formatted(commentId)));
    }

    @Test
    @DisplayName("댓글 다건 조회")
    void items() throws Exception {
        long postId = 1;

        ResultActions resultActions = mvc.perform(
                        get("/api/v1/posts/%d/comments".formatted(postId))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1CommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[1].id").value(2));
    }

}
