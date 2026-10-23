package com.thoughtindex.note;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.thoughtindex.global.config.JpaConfig;
import com.thoughtindex.note.domain.Note;
import com.thoughtindex.note.domain.NoteRepository;
import com.thoughtindex.tab.domain.IndexTab;
import com.thoughtindex.tab.domain.TabColor;

/**
 * 슬라이스 테스트: JPA 관련 빈만 띄워서 쿼리가 의도대로 나오는지 본다. (웹 계층은 안 뜸)
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class NoteRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    NoteRepository noteRepository;

    @Test
    @DisplayName("탭의 메모는 고정한 것부터 나온다")
    void findByTab_pinnedFirst() {
        IndexTab tab = em.persist(IndexTab.create("공부", TabColor.SKY, 0));
        em.persist(Note.write(tab, "일반 메모", "", false));
        em.persist(Note.write(tab, "고정 메모", "", true));

        var page = noteRepository.findByTabIdOrderByPinnedDescUpdatedAtDesc(tab.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Note::getTitle).containsExactly("고정 메모", "일반 메모");
    }

    @Test
    @DisplayName("검색은 제목과 내용을 대소문자 구분 없이 찾는다")
    void search() {
        IndexTab tab = em.persist(IndexTab.create("공부", TabColor.SKY, 0));
        em.persist(Note.write(tab, "JPA 정리", "", false));
        em.persist(Note.write(tab, "오늘 배운 것", "spring 트랜잭션", false));
        em.persist(Note.write(tab, "장보기", "우유", false));

        assertThat(noteRepository.search("jpa", PageRequest.of(0, 10)).getContent())
                .extracting(Note::getTitle).containsExactly("JPA 정리");
        assertThat(noteRepository.search("SPRING", PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
    }
}
