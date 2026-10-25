-- 이제 모든 탭에 주인이 있어야 한다 (V2 와 나눈 이유: 로컬 seed 가 그 사이에 기존 탭에 주인을 채운다)
alter table index_tab alter column member_id set not null;
