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

    /** 한 사람이 붙일 수 있는 탭 수. 화면 옆에 다 들어가야 해서 제한한다 */
    public static final int MAX_COUNT = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 주인 회원의 id.
     * Member 객체(@ManyToOne)가 아니라 id 만 들고 있다: 탭은 "누구 것인지"만 알면 되고 회원 정보는 필요 없다.
     * 다른 도메인은 id 로만 참조하면 tab 패키지가 member 패키지의 클래스를 몰라도 된다.
     */
    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 20)
    private String name;

    // ORDINAL(숫자) 로 저장하면 enum 순서만 바꿔도 데이터가 깨진다 → 항상 STRING
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TabColor color;

    /** 위에서부터의 순서 (0부터) */
    @Column(nullable = false)
    private int position;

    private IndexTab(Long memberId, String name, TabColor color, int position) {
        this.memberId = memberId;
        this.name = name;
        this.color = color;
        this.position = position;
    }

    public static IndexTab create(Long memberId, String name, TabColor color, int position) {
        return new IndexTab(memberId, name, color, position);
    }

    public void update(String name, TabColor color) {
        this.name = name;
        this.color = color;
    }

    public void moveTo(int position) {
        this.position = position;
    }
}
