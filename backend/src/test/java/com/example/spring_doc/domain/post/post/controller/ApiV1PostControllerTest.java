package com.example.spring_doc.domain.post.post.controller;

import com.example.spring_doc.domain.member.member.entity.Member;
import com.example.spring_doc.domain.member.member.service.MemberService;
import com.example.spring_doc.domain.post.post.entity.Post;
import com.example.spring_doc.domain.post.post.service.PostService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostControllerTest {


    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;
    @Autowired
    private MemberService memberService;

    private Member loginedMember;
    private String token;

    @BeforeEach
    void beforeLogin() {
        loginedMember = memberService.findByUsername("user1").get();
        token = memberService.getAuthToken(loginedMember);
    }

    private void checkPost(ResultActions resultActions, Post post) throws Exception {
        resultActions
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()))
                .andExpect(jsonPath("$.data.content").value(post.getContent()))
                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getNickname()))
                .andExpect(jsonPath("$.data.published").value(post.isPublished()))
                .andExpect(jsonPath("$.data.listed").value(post.isListed()))
                .andExpect(jsonPath("$.data.createdDate").value(matchesPattern(post.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.modifiedDate").value(matchesPattern(post.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
    }

    private ResultActions itemRequest(long postId, String apiKey) throws Exception {
        return mvc
                .perform(
                        get("/api/v1/posts/%d".formatted(postId))
                                .header("Authorization", "Bearer " + apiKey)
                )
                .andDo(print());
    }

    private void checkPosts(ResultActions resultActions, List<Post> posts) throws Exception {
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            resultActions
                    .andExpect(jsonPath("$.data.items[%d]".formatted(i)).exists())
                    .andExpect(jsonPath("$.data.items[%d].id".formatted(i)).value(post.getId()))
                    .andExpect(jsonPath("$.data.items[%d].title".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$.data.items[%d].content".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.data.items[%d].authorId".formatted(i)).value(post.getAuthor().getId()))
                    .andExpect(jsonPath("$.data.items[%d].authorName".formatted(i)).value(post.getAuthor().getNickname()))
                    .andExpect(jsonPath("$.data.items[%d].published".formatted(i)).value(post.isPublished()))
                    .andExpect(jsonPath("$.data.items[%d].listed".formatted(i)).value(post.isListed()))
                    .andExpect(jsonPath("$.data.items[%d].createdDate".formatted(i)).value(matchesPattern(post.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.data.items[%d].modifiedDate".formatted(i)).value(matchesPattern(post.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
        }
    }

    @Test
    @DisplayName("글 다건 조회")
    void items1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items.length()").value(3)) // itemsPerPage
                .andExpect(jsonPath("$.data.currentPageNo").isNumber()) // curPage
                .andExpect(jsonPath("$.data.totalPages").isNumber()); // totalPages

        Page<Post> postPage = postService.getListedItems(1, 3, "title", "");
        List<Post> posts = postPage.getContent();
        checkPosts(resultActions, posts);

    }

    @Test
    @DisplayName("글 다건 조회 - 검색 - 제목, 페이징이 되어야 함.")
    void items2() throws Exception {

        int page = 1;
        int pageSize = 3;

        //검색어, 검색대상
        String keywordType = "title";
        String keyword = "title";


        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts?page=%d&pageSize=%d&keywordType=%s&keyword=%s"
                                .formatted(page, pageSize, keywordType, keyword))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.data.items.length()").value(pageSize)) // itemsPerPage
                .andExpect(jsonPath("$.data.currentPageNo").value(page)) // curPage
                .andExpect(jsonPath("$.data.totalPages").value(3)) // totalPages
                .andExpect(jsonPath("$.data.totalItems").value(7));


        Page<Post> postPage = postService.getListedItems(page, pageSize, keywordType, keyword);
        List<Post> posts = postPage.getContent();
        checkPosts(resultActions, posts);
    }

    @Test
    @DisplayName("글 다건 조회 - 검색 - 내용, 페이징이 되어야 함.")
    void items3() throws Exception {

        int page = 1;
        int pageSize = 3;

        //검색어, 검색대상
        String keywordType = "content";
        String keyword = "content";


        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts?page=%d&pageSize=%d&keywordType=%s&keyword=%s"
                                .formatted(page, pageSize, keywordType, keyword))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.data.items.length()").value(pageSize)) // itemsPerPage
                .andExpect(jsonPath("$.data.currentPageNo").value(page)) // curPage
                .andExpect(jsonPath("$.data.totalPages").value(3)) // totalPages
                .andExpect(jsonPath("$.data.totalItems").value(7));


        Page<Post> postPage = postService.getListedItems(page, pageSize, keywordType, keyword);
        List<Post> posts = postPage.getContent();
        checkPosts(resultActions, posts);
    }

    @Test
    @DisplayName("내가 작성한 글 조회 - 검색 페이징이 되어야 함.")
    void mines() throws Exception {

        int page = 1;
        int pageSize = 3;

        //검색어, 검색 대상
        String keywordType = "content";
        String keyword = "content";


        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/me?page=%d&pageSize=%d&keywordType=%s&keyword=%s"
                                .formatted(page, pageSize, keywordType, keyword))
                                .header("Authorization", "Bearer " + token)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getMines"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("내 글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items.length()").value(pageSize)) // itemsPerPage
                .andExpect(jsonPath("$.data.currentPageNo").value(page)) // curPage
                .andExpect(jsonPath("$.data.totalPages").value(2)) // totalPages
                .andExpect(jsonPath("$.data.totalItems").value(4));

        Page<Post> postPage = postService.getMines(loginedMember, page, pageSize, keywordType, keyword);
        List<Post> posts = postPage.getContent();
        checkPosts(resultActions, posts);
    }

    @Test
    @DisplayName("글 단건 조회 - 다른 유저의 공개글 조회")
    void item1() throws Exception {
        long postId = 1;

        ResultActions resultActions = itemRequest(postId, token);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글을 조회하였습니다.".formatted(postId)));

        Post post = postService.getItem(postId).get();
        checkPost(resultActions, post);
    }

    @Test
    @DisplayName("글 단건 조회 실패 - 없는 글인 경우")
    void item2() throws Exception {
        long postId = 100;

        ResultActions resultActions = itemRequest(postId, token);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));

    }

    @Test
    @DisplayName("글 단건 조회 - 다른 유저의 비공개글 조회")
    void item3() throws Exception {

        long postId = 3;

        ResultActions resultActions = itemRequest(postId, token);

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value("403-1"))
                .andExpect(jsonPath("$.msg").value("비공개 설정된 글입니다."));

    }


    private ResultActions writeRequest(String apiKey, String title, String content) throws Exception {
        String requestBody = """
                {
                    "title": "%s",
                    "content": "%s",
                    "published": true,
                    "listed": true
                }
                """.formatted(title, content).stripIndent();

        return mvc
                .perform(
                        post("/api/v1/posts")
                                .header("Authorization", "Bearer " + apiKey)
                                .contentType("application/json")
                                .content(requestBody)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("글 작성")
    void write1() throws Exception {
        String title = "new title";
        String content = "new content";

        ResultActions resultActions = writeRequest(token, title, content);

        Post post = postService.getLatestItem().get();

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 작성 완료되었습니다."
                        .formatted(post.getId())));

        checkPost(resultActions, post);
    }

    @Test
    @DisplayName("글 작성 실패 - 로그인 하지 않았을 때")
    void write2() throws Exception {
        String title = "new title";
        String content = "new content";
        String token = "";

        ResultActions resultActions = writeRequest(token, title, content);

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));


    }

    @Test
    @DisplayName("글 작성 실패 - 제목, 내용이 없을 때")
    void write3() throws Exception {
        String title = "";
        String content = "";

        ResultActions resultActions = writeRequest(token, title, content);

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(jsonPath("$.code").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content : NotBlank : must not be blank
                        title : NotBlank : must not be blank
                        """.trim().stripIndent()));


    }

    private ResultActions modifyReqeust(long postId, String apiKey, String title, String content) throws Exception {
        String requestBody = """
                {
                    "title": "%s",
                    "content": "%s",
                    "published": true,
                    "listed": true
                }
                """.formatted(title, content).stripIndent();
        return mvc
                .perform(
                        put("/api/v1/posts/%d".formatted(postId))
                                .header("Authorization", "Bearer " + apiKey)
                                .contentType("application/json")
                                .content(requestBody)

                )
                .andDo(print());
    }

    @Test
    @DisplayName("글 수정")
    void modify1() throws Exception {

        long postId = 1;
        String title = "modified title";
        String content = "modified content";

        ResultActions resultActions = modifyReqeust(postId, token, title, content);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 수정이 완료되었습니다.".formatted(postId)));

        Post post = postService.getItem(postId).get();
        checkPost(resultActions, post);
    }

    @Test
    @DisplayName("글 수정 실패 - no apiKey ")
    void modify2() throws Exception {

        long postId = 1;
        String title = "modified title";
        String content = "modified content";
        String token = null;

        ResultActions resultActions = modifyReqeust(postId, token, title, content);

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
    }

    @Test
    @DisplayName("글 수정 실패 - no post ")
    void modify3() throws Exception {

        long postId = 1000;
        String title = "modified title";
        String content = "modified content";

        ResultActions resultActions = modifyReqeust(postId, token, title, content);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));
    }

    @Test
    @DisplayName("글 수정 실패 - no input data ")
    void modify4() throws Exception {

        long postId = 1;
        String title = "";
        String content = "";

        ResultActions resultActions = modifyReqeust(postId, token, title, content);

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("400-1"))
                .andExpect(jsonPath("$.msg").value("""
                        content : NotBlank : must not be blank
                        title : NotBlank : must not be blank
                        """.trim().stripIndent()));
    }

    @Test
    @DisplayName("글 수정 실패 - no permission ")
    void modify5() throws Exception {

        long postId = 3;
        String title = "다른 유저 제목 수정";
        String content = "다른 유저 내용 수정";

        ResultActions resultActions = modifyReqeust(postId, token, title, content);

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(jsonPath("$.code").value("403-1"))
                .andExpect(jsonPath("$.msg").value("자신이 작성한 글만 수정 가능합니다."));
    }

    private ResultActions deleteRequest(long postId, String apiKey) throws Exception {
        return mvc
                .perform(
                        delete("/api/v1/posts/%d".formatted(postId))
                                .header("Authorization", "Bearer " + apiKey)

                )
                .andDo(print());
    }

    @Test
    @DisplayName("글 삭제")
    void delete1() throws Exception {

        long postId = 1;

        ResultActions resultActions = deleteRequest(postId, token);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 삭제 완료되었습니다.".formatted(postId)));


        Assertions.assertThat(postService.getItem(postId)).isEmpty();
    }

    @Test
    @DisplayName("글 삭제 실패 - no apiKey ")
    void delete2() throws Exception {

        long postId = 1;
        String token = null;

        ResultActions resultActions = deleteRequest(postId, token);

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("401-1"))
                .andExpect(jsonPath("$.msg").value("잘못된 인증키입니다."));
    }

    @Test
    @DisplayName("글 삭제 실패 - no post ")
    void delete3() throws Exception {

        long postId = 1000;

        ResultActions resultActions = deleteRequest(postId, token);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 글입니다."));
    }

    @Test
    @DisplayName("글 삭제 실패 - no permission ")
    void delete4() throws Exception {

        long postId = 3;

        ResultActions resultActions = deleteRequest(postId, token);

        resultActions
                .andExpect(status().isForbidden())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value("403-1"))
                .andExpect(jsonPath("$.msg").value("자신이 작성한 글만 삭제 가능합니다."));
    }

    @Test
    @DisplayName("통계 조회 - 관리자 접근")
    @WithUserDetails("admin")
    void statisticsAdmin() throws Exception {

        ResultActions resultActions = mvc.perform(
                        get("/api/v1/posts/statistics")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getStatistics"))
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.msg").value("통계 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.postCount").value(10))
                .andExpect(jsonPath("$.data.postPublishedCount").value(10))
                .andExpect(jsonPath("$.data.postListedCount").value(10));

    }

    @Test
    @DisplayName("통계 조회 - user1 접근")
    @WithUserDetails("user1")
    void statisticsUser() throws Exception {
        ResultActions resultActions = mvc.perform(
                        get("/api/v1/posts/statistics")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden());

    }
}