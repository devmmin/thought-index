package com.thoughtindex.tab.domain;

import com.thoughtindex.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메모장 옆에 붙이는 인덱스(견출지) 한 장.
 *
 * - setter 를 열지 않는다: 값은 rename(), changeColor() 같은 "의미 있는 메서드"로만 바뀐다.
 * - 기본 생성자는 JPA 용이라 protected 로 막아둔다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexTab extends BaseTimeEntity {

    /** 한 번에 붙일 수 있는 탭 수. 화면 옆에 다 들어가야 해서 제한한다 */
    public static final int MAX_COUNT = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    // ORDINAL(숫자) 로 저장하면 enum 순서만 바꿔도 데이터가 깨진다 → 항상 STRING
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TabColor color;

    /** 위에서부터의 순서 (0부터) */
    @Column(nullable = false)
    private int position;

    private IndexTab(String name, TabColor color, int position) {
        this.name = name;
        this.color = color;
        this.position = position;
    }

    public static IndexTab create(String name, TabColor color, int position) {
        return new IndexTab(name, color, position);
    }

    public void update(String name, TabColor color) {
        this.name = name;
        this.color = color;
    }

    public void moveTo(int position) {
        this.position = position;
    }
}
