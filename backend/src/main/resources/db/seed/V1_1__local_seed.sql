-- 로컬 개발용 예시 데이터 (application-local.yml 에서만 로드)
insert into index_tab (name, color, position, created_at, updated_at) values
    ('아이디어', 'PEACH',    0, now(), now()),
    ('할일',     'MINT',     1, now(), now()),
    ('공부',     'SKY',      2, now(), now()),
    ('감정',     'LAVENDER', 3, now(), now());

insert into note (tab_id, title, content, pinned, created_at, updated_at) values
    (1, '앱 이름 뭐로 하지?', '서랍 / 색인 / 책갈피…
짧고 귀여운 게 좋음', true, now(), now()),
    (1, '주말 사이드 프로젝트', '스프링부트로 API 만들기
출시까지 해보기!', false, now(), now()),
    (2, '장보기', '우유, 계란, 사과', false, now(), now()),
    (3, '스프링 계층 구조', 'Controller → Service → Repository
각자 자기 일만 한다', false, now(), now()),
    (3, 'DTO 왜 쓰는가', '엔티티를 API 에 그대로 노출하지 않으려고', false, now(), now());
