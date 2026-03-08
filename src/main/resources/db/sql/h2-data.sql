-- 1. 회원 데이터 (관리자 1, 일반 유저 2)
INSERT INTO members (login_id, password, name, email, role, created_date, last_modified_date)
VALUES ('admin', '$2a$10$lBfbCJoiDMo9vn3a4oJq4ubiEEOx5E1D4ldgT2xC.jVQ33CYDJRwi', '운영자', 'admin@cms.com', 'ADMIN', NOW(), NOW());

INSERT INTO members (login_id, password, name, email, role, created_date, last_modified_date)
VALUES ('user1', '$2a$10$lBfbCJoiDMo9vn3a4oJq4ubiEEOx5E1D4ldgT2xC.jVQ33CYDJRwi', '길동이', 'user1@test.com', 'USER', NOW(), NOW());

INSERT INTO members (login_id, password, name, email, role, created_date, last_modified_date)
VALUES ('user2', '$2a$10$lBfbCJoiDMo9vn3a4oJq4ubiEEOx5E1D4ldgT2xC.jVQ33CYDJRwi', '철수', 'user2@test.com', 'USER', NOW(), NOW());

-- 2. 게시글 데이터 (각 유저별 게시글)
INSERT INTO contents (title, description, view_count, member_id, created_by, created_date, last_modified_date)
VALUES ('[공지] 환영합니다!', '맑은기술 프로젝트 게시판입니다.', 10, (SELECT id FROM members WHERE login_id = 'admin'), 'admin', NOW(), NOW());

INSERT INTO contents (title, description, view_count, member_id, created_by, created_date, last_modified_date)
VALUES ('질문 있습니다!', 'H2 데이터베이스 설정이 안 돼요.', 3, (SELECT id FROM members WHERE login_id = 'user1'), 'user1', NOW(), NOW());

INSERT INTO contents (title, description, view_count, member_id, created_by, created_date, last_modified_date)
VALUES ('스프링 시큐리티 세션 질문', '세션 정보를 DB에 저장하는 설정이 궁금합니다.', 5, (SELECT id FROM members WHERE login_id = 'user2'), 'user2', NOW(), NOW());

INSERT INTO contents (title, description, view_count, member_id, created_by, created_date, last_modified_date)
VALUES ('오늘 점심 메뉴 추천', '회사 근처 돈까스 집 어떤가요?', 12, (SELECT id FROM members WHERE login_id = 'user2'), 'user2', NOW(), NOW());

-- 3. 댓글 및 대댓글 데이터
-- (1) 일반 댓글
INSERT INTO comments (content, content_id, parent_id, created_by, created_date, last_modified_date)
VALUES ('첫 번째 댓글입니다!', (SELECT id FROM contents WHERE title = '[공지] 환영합니다!' LIMIT 1), NULL, 'user1', NOW(), NOW());

INSERT INTO comments (content, content_id, parent_id, created_by, created_date, last_modified_date)
VALUES ('spring-session-jdbc 의존성을 추가해보세요.', (SELECT id FROM contents WHERE title = '스프링 시큐리티 세션 질문' LIMIT 1), NULL, 'admin', NOW(), NOW());

-- (2) 대댓글 (parent_id 참조)
-- 서브쿼리 내의 테이블 충돌 방지를 위해 임시 테이블(tmp)을 사용합니다.
INSERT INTO comments (content, content_id, parent_id, created_by, created_date, last_modified_date)
VALUES ('오! 바로 해결됐습니다. 감사합니다!',
        (SELECT id FROM contents WHERE title = '스프링 시큐리티 세션 질문' LIMIT 1),
       (SELECT id FROM (SELECT id FROM comments WHERE content = 'spring-session-jdbc 의존성을 추가해보세요.') AS tmp),
    'user2', NOW(), NOW());

INSERT INTO comments (content, content_id, parent_id, created_by, created_date, last_modified_date)
VALUES ('지하 1층 돈까스 집이 가성비 최고예요.',
        (SELECT id FROM contents WHERE title = '오늘 점심 메뉴 추천' LIMIT 1),
    NULL, 'user1', NOW(), NOW());