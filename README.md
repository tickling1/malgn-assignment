Simple CMS REST API Project
(주)맑은기술 백엔드 과제로 진행된 Simple CMS REST API 프로젝트입니다. 기본적인 요구사항인 콘텐츠 CRUD 및 권한 제어를 구현하였으며, 추가로 QueryDSL을 활용한 동적 검색과 계층형 댓글 시스템을 포함하고 있습니다.

프로젝트 실행 방법
1. 환경 요구사항
- Java 25 이상
- Gradle 8.x 이상
- H2 Database (별도 설치 불필요, In-memory 사용)

2. 프로젝트 빌드 및 실행
터미널(또는 CMD)에서 프로젝트 루트 디렉토리로 이동한 후 아래 명령어를 입력합니다.
# 레포지토리 클론 (또는 압축 해제 후 해당 폴더 이동)
git clone https://github.com/사용자계정/malgn-assignment.git
cd malgn-assignment

# 빌드 및 실행 (Unix/Linux/macOS)
./gradlew bootRun

# 빌드 및 실행 (Windows)
gradlew.bat bootRun




AI 도구 활용 명시
- 사용 도구: Gemini 

활용 범위
- Java 25 및 Spring Boot 4 환경에서의 QueryDSL 설정 및 6.x 버전 문법 확인.
- 계층형 댓글 구조 구현 시 Stream API를 활용한 DTO 변환 및 정렬 로직 최적화.
- REST API 응답 시 JSON 필드 순서(@JsonPropertyOrder) 및 빈 값 처리(@JsonInclude) 가이드 참고.
