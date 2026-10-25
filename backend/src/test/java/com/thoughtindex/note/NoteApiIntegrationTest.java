package com.thoughtindex.note;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.thoughtindex.member.domain.Member;
import com.thoughtindex.member.domain.MemberRepository;

/**
 * 통합 테스트: 실제 앱 전체를 띄우고 HTTP 요청 → 응답까지 확인한다.
 * @Transactional 이라 테스트가 끝나면 DB 변경이 롤백돼 테스트끼리 서로 영향을 안 준다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoteApiIntegrationTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    MemberRepository memberRepository;

    MockMvc mvc;

    /** "남"으로 로그인한 요청에 붙이는 토큰 (진짜 서명 대신 테스트용 jwt()) */
    RequestPostProcessor other;

    @BeforeEach
    void setUp() {
        Long myId = memberRepository.save(Member.withPassword("me@test.com", "{noop}x", "나")).getId();
        Long otherId = memberRepository.save(Member.withPassword("other@test.com", "{noop}x", "남")).getId();
        other = jwt().jwt(j -> j.subject(String.valueOf(otherId)));
        // 따로 지정하지 않은 요청은 전부 "나"로 로그인한 상태로 보낸다
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(get("/").with(jwt().jwt(j -> j.subject(String.valueOf(myId)))))
                .build();
    }

    @Test
    @DisplayName("탭 만들기 → 메모 쓰기 → 목록 조회 → 수정")
    void noteLifecycle() throws Exception {
        long tabId = createTab("아이디어", "PEACH");

        String body = mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"tabId": %d, "title": "  앱 이름  ", "content": "서랍? 색인?"}
                        """.formatted(tabId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("앱 이름"))
                .andReturn().getResponse().getContentAsString();
        long noteId = ((Number) JsonPath.read(body, "$.id")).longValue();

        mvc.perform(get("/api/notes").param("tabId", String.valueOf(tabId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(put("/api/notes/{id}", noteId).contentType(MediaType.APPLICATION_JSON).content("""
                        {"tabId": %d, "title": "앱 이름 확정", "content": "생각서랍", "pinned": true}
                        """.formatted(tabId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned").value(true));

        mvc.perform(get("/api/tabs"))
                .andExpect(jsonPath("$[0].noteCount").value(1));
    }

    @Test
    @DisplayName("제목이 비면 400 + 어떤 필드가 틀렸는지 알려준다")
    void validationError() throws Exception {
        long tabId = createTab("공부", "SKY");

        mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"tabId": %d, "title": " "}
                        """.formatted(tabId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
    }

    @Test
    @DisplayName("없는 색 이름을 보내면 400")
    void invalidEnum() throws Exception {
        mvc.perform(post("/api/tabs").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "탭", "color": "NEON"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("없는 메모는 404 + NOTE_NOT_FOUND")
    void notFound() throws Exception {
        mvc.perform(get("/api/notes/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTE_NOT_FOUND"));
    }

    @Test
    @DisplayName("탭을 지우면 그 안의 메모도 같이 지워진다")
    void deleteTabCascadesNotes() throws Exception {
        long tabId = createTab("감정", "LAVENDER");
        String body = mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"tabId": %d, "title": "오늘 기분"}
                        """.formatted(tabId)))
                .andReturn().getResponse().getContentAsString();
        long noteId = ((Number) JsonPath.read(body, "$.id")).longValue();

        mvc.perform(delete("/api/tabs/{id}", tabId)).andExpect(status().isNoContent());

        mvc.perform(get("/api/notes/{id}", noteId)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("남의 탭과 메모는 보이지도, 고쳐지지도 않는다 (404)")
    void cannotTouchOthersData() throws Exception {
        long tabId = createTab("내 탭", "PINK");
        String body = mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"tabId": %d, "title": "비밀"}
                        """.formatted(tabId)))
                .andReturn().getResponse().getContentAsString();
        long noteId = ((Number) JsonPath.read(body, "$.id")).longValue();

        mvc.perform(get("/api/tabs").with(other))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/notes/{id}", noteId).with(other))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/tabs/{id}", tabId).with(other))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/notes/search").param("q", "비밀").with(other))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    private long createTab(String name, String color) throws Exception {
        String body = mvc.perform(post("/api/tabs").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "%s", "color": "%s"}
                        """.formatted(name, color)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}
