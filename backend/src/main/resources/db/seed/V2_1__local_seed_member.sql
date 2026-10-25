-- 로컬 개발용 데모 계정: demo@thoughtindex.local / demo1234
insert into member (email, password_hash, name, created_at, updated_at)
values ('demo@thoughtindex.local', '{bcrypt}$2y$10$x20.zWl0GiHBq4kRsw0WQerVpFbjiYA8imbZaBBcB3WvJCM0fAXv.', '데모', now(), now());

-- 로컬 예시 탭을 데모 계정 것으로
update index_tab set member_id = (select id from member where email = 'demo@thoughtindex.local');
