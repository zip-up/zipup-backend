# ZIPup 서버 레포지토리
## 집업 - 집들이 선물 펀딩 서비스 !
[![](http://img.shields.io/badge/-서비스소개서-gray?style=flat-square&logo=notion&link=https://file.notion.so/f/f/18b15060-3725-484f-806b-77744900d59c/a4d837c6-20f1-4fc4-b81e-33e0cb714b66/Zipup_%EC%84%9C%EB%B9%84%EC%8A%A4%EC%86%8C%EA%B0%9C%EC%84%9C_%ED%8F%AC%ED%85%90%EC%A0%A4%EB%A6%AC.pdf?id=37921faa-8c60-485f-b070-58369be92683&table=block&spaceId=18b15060-3725-484f-806b-77744900d59c&expirationTimestamp=1712210400000&signature=mhjcurHk1GeROZ-9XEDjYZt2UDZJQhy5bEuI1HC1jR8&downloadName=Zipup_%EC%84%9C%EB%B9%84%EC%8A%A4%EC%86%8C%EA%B0%9C%EC%84%9C_%ED%8F%AC%ED%85%90%EC%A0%A4%EB%A6%AC.pdf)](https://file.notion.so/f/f/1ca94a2b-50ab-4fed-aa6e-9f77386b2d89/a4d837c6-20f1-4fc4-b81e-33e0cb714b66/Zipup_%EC%84%9C%EB%B9%84%EC%8A%A4%EC%86%8C%EA%B0%9C%EC%84%9C_%ED%8F%AC%ED%85%90%EC%A0%A4%EB%A6%AC.pdf?id=8238745d-636c-4adf-9bd8-03772127b4d5&table=block&spaceId=1ca94a2b-50ab-4fed-aa6e-9f77386b2d89&expirationTimestamp=1715212800000&signature=PGRS3J1GnLiDTTX62F9SwPR35WtoCDfKWZuY2fFVEAE)

<a href="http://api.zip-up.kro.kr/swagger-ui/index.html#" style="display: inline">
    <img src="https://img.shields.io/badge/Swagger API-009639?style=flat-square&logo=Swagger&logoColor=" width="120px" />
</a>

## ERD
![image](https://github.com/zip-up/zipup-backend/assets/104782275/2c03765d-9d25-4cc9-8982-5c769255d26d)

#### 주요 도메인
- User : 사용자 정보
- Fund : 펀딩 정보
- Present : 펀딩 참여 정보
- Payment : 결제 내역 정보
- Review : 펀딩 후기 정보

## 기능 소개
### 🎁 웹 크롤링을 활용해 집들이 상품 url만으로 상품 이미지 게시
### 🗝️ 카카오 소셜 로그인 & JWT 활용해 간편 로그인 기능 제공
### 💰 Toss Payments API 활용한 결제 flow

---
## 서비스 아키텍처
<img width="760" alt="스크린샷 2024-04-03 오후 12 59 02" src="https://github.com/zip-up/zipup-backend/assets/104782275/c0e3986e-36c3-4e5b-a268-b2bd989290b4">

----
## CI / CD 파이프라인
![image](https://github.com/zip-up/zipup-backend/assets/104782275/3229304c-6aa1-427d-b5ca-1b3bc8953b5f)

## 주요 구현
- **Cerbot, NGINX로 ssl 인증 구현**
- **Docker Compose로 Container 환경에서 배포**
- **카카오 소셜 로그인 구현 시 OAuth2.0 위임 방식으로 서버에서 카카오 인가 code&token 검증 후 JWT 값을 쿠키에 담아 클라이언트에 응답**
- **JWT는 Redis Docker Image에 저장하도록 구현**
- **Selenium Docker Image 활용해 상품 이미지 크롤링 구현**
- **Toss Payments API 활용해 결제 flow 구현**

---
## 사용 기술
#### Backend
<a href="https://github.com/topics/java" style="display: inline">
    <img src="https://img.shields.io/badge/jdk_11-white" width="45px" />
</a>
<a href="https://github.com/spring-projects" style="display: inline">
    <img src="https://img.shields.io/badge/Spring Boot 2.7-6DB33F?style=flat-square&logo=Spring Boot&logoColor=white" width="110px" />
</a>
<a href="https://github.com/gradle" style="display: inline">
    <img src="https://img.shields.io/badge/Gradle 8.6.x-02303A?logo=gradle" width="100px" />
</a>
<a href="https://github.com/spring-projects/spring-data-jpa" style="display: inline">
    <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=flat-square&logo=JPA&logoColor=white" />
</a>
<a href="" style="display: inline">
    <img src="https://img.shields.io/badge/JUnit5-25A162?logo=junit5&logoColor=white" />
</a>
<a href="https://github.com/spring-projects/spring-security" style="display: inline">
    <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat-square&logo=Spring Security&logoColor=white" />
</a>

#### Storage
<a href="https://github.com/mariadb" style="display: inline">
    <img src="https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=MariaDB&logoColor=white" />
</a>
<a href="https://github.com/hibernate" style="display: inline">
    <img src="https://img.shields.io/badge/hibernate-59666C?logo=hibernate"  />
</a>
<a href="https://github.com/redis" style="display: inline">
    <img src="https://img.shields.io/badge/redis-DC382D?style=flat-square&logo=redis&logoColor=white" />
</a>

#### Infra & SSL & CI-CD
<a href="https://github.com/NaverCloudPlatform" style="display: inline">
    <img src="https://img.shields.io/badge/Rocky Linux 8.8  -03C75A?style=flat-square&logo=rocky linux&logoColor=white" width="120px" />
</a>
<a href="https://github.com/NaverCloudPlatform" style="display: inline">
    <img src="https://img.shields.io/badge/Cloud DB for MySQL-03C75A?style=flat-square&logo=naver&logoColor=white" />
</a>
<a href="https://github.com/nginx" style="display: inline">
    <img src="https://img.shields.io/badge/NGINX-009639?style=flat-square&logo=Nginx&logoColor=white" />
</a>
<a href="https://github.com/letsencrypt" style="display: inline">
    <img src="https://img.shields.io/badge/Let's Encrypt-0E0F37?style=flat-square&logo=let's encrypt&logoColor=white" />
</a>
<a href="https://docs.github.com/ko/actions" style="display: inline">
    <img src="https://img.shields.io/badge/Github Actions-2088FF?style=flat-square&logo=Github Actions&logoColor=white" />
</a>
<a href="https://github.com/docker" style="display: inline">
    <img src="https://img.shields.io/badge/Docker-2088FF?style=flat-square&logo=Docker&logoColor=white" />
</a>
<a href="https://github.com/docker" style="display: inline">
    <img src="https://img.shields.io/badge/Docker Compose-2088FF?style=flat-square&logo=Docker-compose&logoColor=white" />
</a>

#### Social Login & Payments & Crawling
<a href="https://github.com/kakao" style="display: inline">
    <img src="https://img.shields.io/badge/Kakao API-FFCD00?style=flat-square&logo=Kakao&logoColor=white" />
</a>
<a href="https://github.com/naver" style="display: inline">
    <img src="https://img.shields.io/badge/Toss Payments API-2088FF?style=flat-square&logoColor=white" />
</a>
<a href="https://github.com/google" style="display: inline">
    <img src="https://img.shields.io/badge/Selenium-009639?style=flat-square&logo=Selenium&logoColor=white" />
</a>
