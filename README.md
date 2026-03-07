## Simple CMS REST API Project
(주)맑은기술 백엔드 과제로 진행된 Simple CMS REST API 프로젝트입니다. 

기본적인 요구사항인 콘텐츠 CRUD 및 권한 제어를 구현하였으며, 추가로 QueryDSL을 활용한 동적 검색과 계층형 댓글 시스템을 포함하고 있습니다.
##
### 프로젝트 실행 방법
**1. 환경 요구사항**
- Java 25 이상
- Gradle 8.x 이상
- H2 Database (별도 설치 불필요, In-memory 사용)

**2. 프로젝트 빌드 및 실행**

터미널(또는 CMD)에서 프로젝트 루트 디렉토리로 이동한 후 아래 명령어를 입력합니다.
``` {bash}
1. 레포지토리 클론 (또는 압축 해제 후 해당 폴더 이동)
git clone https://github.com/tickling1/malgn-assignment.git
cd malgn-assignment

2. 빌드 및 실행 (Unix/Linux/macOS)
./gradlew bootRun

3. 빌드 및 실행 (Windows)
gradlew.bat bootRun
```

**3. 접속 정보**

애플리케이션 실행 후, 브라우저 주소창에 아래 주소를 입력하여 접속합니다.
- API 명세서 (Swagger): http://localhost:8080/swagger-ui/index.html
- H2 Console (DB 확인): http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- User Name: sa
- Password: (비어있음)

##
### 구현 내용 및 추가 구현 기능
**1. 콘텐츠 관리 및 권한 제어 (기본 요구사항)**
- Contents CRUD: JPA를 활용하여 게시글의 생성, 수정, 삭제, 상세 조회를 구현했습니다.
- 페이징 최적화: 목록 조회 시 Pageable 객체를 사용하여 서버 부하를 줄이고 효율적인 데이터 렌더링이 가능하도록 페이징 처리를 수행했습니다.

##
**2. 보안 및 권한 검증 (기본 요구사항)**

<img width="2176" height="216" alt="image" src="https://github.com/user-attachments/assets/b6b8c937-22fa-4585-a19d-fc1eb7c8c0c6" />

- Spring Security를 활용하여 USER, ADMIN 권한을 구분했습니다.
- 콘텐츠 수정 및 삭제 시, 작성자 본인 여부를 검증하는 로직을 서비스 레이어에 구현했습니다.
- 관리자(ADMIN) 권한을 가진 사용자는 모든 콘텐츠에 대해 관리 권한(수정,삭제)을 가질 수 있도록 예외 로직을 적용했습니다.

##
**3. 동적 검색 API 분리 (추가 구현)**

<img width="1534" height="1188" alt="image" src="https://github.com/user-attachments/assets/5bf6c5ba-713f-418c-83e1-773ab1d29584" />

- QueryDSL를 도입하여 제목(Title), 작성자(CreatedBy), 작성 기간(CreatedDate Range) 등 다양한 검색 조건을 유연하게 조합할 수 있도록 동적 필터링을 구현했습니다.

##
**4. 계층형 댓글 시스템 (추가 구현)**

<img width="933" height="1000" alt="image" src="https://github.com/user-attachments/assets/053bdb30-a278-4f35-b127-c336d0cb2bf7" />

- Comments(댓글) 엔티티 내에서 자기 자신을 참조하는 parentComment 필드를 설계하여 무한 계층 구조의 기반을 마련했습니다.
- 부모 댓글 아래에 자식 댓글(답글)이 리스트 형태로 포함되는 계층형 구조입니다.
- Jackson 어노테이션(@JsonPropertyOrder, @JsonInclude)을 활용하여 API 응답 시 게시글은 최신순(내림차순)으로 보여주고,댓글과 대댓글은 대화의 맥락을 유지하기 위해 작성 순서(오름차순)로 배치되도록 하고, 불필요한 필드(답글이 비어있는 경우)는 노출되지 않도록 조정했습니다.

##
**5. 공통 예외 처리 및 응답 구조**

<img width="933" height="471" alt="스크린샷 2026-03-07 오후 9 45 35" src="https://github.com/user-attachments/assets/01c22010-e1bb-4f64-a00c-7d0077866e1a" />
<img width="1512" height="380" alt="image" src="https://github.com/user-attachments/assets/4609a248-2232-47ce-aca5-59b6b329339e" />

- Global Exception Handler: @RestControllerAdvice를 사용하여 프로젝트 전역에서 발생하는 예외를 공통된 형식으로 응답하도록 구현했습니다.
##
## AI 도구 활용 명시
**AI 활용 범위**
- 사용 도구 - Gemini 
- Java 25 및 Spring Boot 4 환경에서의 QueryDSL 설정 및 6.x 버전 문법 확인.
- 계층형 댓글 구조 구현 시 Stream API를 활용한 DTO 변환 및 정렬 로직 참고.
- REST API 응답 시 JSON 필드 순서(@JsonPropertyOrder) 및 빈 값 처리(@JsonInclude) 가이드 참고.
