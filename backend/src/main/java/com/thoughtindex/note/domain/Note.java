package com.thoughtindex.note.domain;

import com.thoughtindex.global.common.BaseTimeEntity;
import com.thoughtindex.tab.domain.IndexTab;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 생각 메모 한 장. 반드시 하나의 인덱스 탭에 꽂혀 있다.
 *
 * 연관관계는 Note → IndexTab 단방향만 둔다.
 * IndexTab 쪽에 List<Note> 를 두면(양방향) 편해 보여도, 탭 하나 불러올 때 메모가 딸려오는 실수가 잦다.
 * "탭의 메모 목록"은 NoteRepository 로 조회하면 충분하다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Note extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ManyToOne 기본값은 EAGER(즉시 로딩) → 항상 LAZY 로 바꿔서 필요할 때만 가져온다
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tab_id")
    private IndexTab tab;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(nullable = false)
    private boolean pinned;

    private Note(IndexTab tab, String title, String content, boolean pinned) {
        this.tab = tab;
        this.title = title;
        this.content = content;
        this.pinned = pinned;
    }

    public static Note write(IndexTab tab, String title, String content, boolean pinned) {
        return new Note(tab, title, content, pinned);
    }

    public void edit(String title, String content, boolean pinned) {
        this.title = title;
        this.content = content;
        this.pinned = pinned;
    }

    /** 다른 인덱스로 옮겨 꽂기 */
    public void moveTo(IndexTab tab) {
        this.tab = tab;
    }
}
