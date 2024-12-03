# creator_hub


## 와이어 프레임
![image](https://github.com/user-attachments/assets/f87b831d-610e-4ed9-8f1f-c56a88a59f77)

## API 명세서
![image](https://github.com/user-attachments/assets/c9830c56-394f-46d5-8906-2ce9546c8a20)


## 적용 기술 & 개발 환경
- Spring Boot 2.7.18: Spring Boot는 RESTful API 서버를 구축하는 데 사용되었습니다. Spring Boot는 빠른 개발과 설정을 가능하게 하며, 필요한 라이브러리와 플러그인들을 자동으로 처리하여 효율적인 개발 환경을 제공합니다.

- Gradle: 빌드 도구로 Gradle을 사용하여 프로젝트의 의존성 관리 및 빌드 과정을 자동화했습니다. Gradle은 빠르고 유연한 빌드 시스템으로, Java 및 Gradle 플러그인을 통해 프로젝트 관리의 편리함을 제공합니다.

- MongoDB: MongoDB는 NoSQL 데이터베이스로, 비정형 데이터를 처리하는 데 사용되었습니다. MongoDB는 데이터 모델링을 유연하게 할 수 있어 대규모의 비정형 데이터를 처리하는 데 유리합니다.

- MongoDB Compass: MongoDB Compass를 사용하여 MongoDB 데이터베이스의 데이터를 시각적으로 확인하고 관리할 수 있었습니다. 이는 데이터베이스의 구조와 내용을 쉽게 확인하고, 문제를 빠르게 해결할 수 있도록 도와주었습니다.

- Spark: Apache Spark는 인기 키워드 분석 및 추천 동영상 기능 구현에 사용되었습니다. Spark는 분산 데이터 처리와 빠른 계산을 지원하여 대규모 데이터를 실시간으로 분석하고 처리하는 데 적합합니다.

- Spark UI: Apache Spark 작업의 성능을 실시간으로 모니터링하고 최적화하기 위해 Spark UI를 활용했습니다. Spark UI는 작업 실행 계획, 작업 및 단계별 성능을 분석하여, 실행 시간을 최적화하고, 리소스 활용도를 개선하는 데 중요한 역할을 했습니다.

- JWT (JSON Web Token): 사용자 인증을 위한 보안 토큰 방식으로 JWT를 사용하여 로그인한 사용자의 정보를 안전하게 관리했습니다. JWT는 Stateless 인증을 가능하게 하여 서버의 부하를 줄이고 확장성을 향상시킵니다.

- JSP (Java Server Pages): 사용자 인터페이스를 동적으로 생성하기 위해 JSP를 사용했습니다. JSP는 서버에서 HTML 페이지를 생성하여 클라이언트에 반환하며, 웹 애플리케이션의 뷰 부분을 처리합니다.

- Java 11: Java 11을 사용하여 최신 Java 기능과 성능 최적화를 구현했습니다. Java 11은 LTS(장기 지원) 버전으로 안정성과 성능이 우수합니다.

- Spring MVC: Spring MVC는 웹 애플리케이션의 모델, 뷰, 컨트롤러를 분리하는 아키텍처를 제공하여 웹 애플리케이션의 구조를 체계적으로 구성할 수 있도록 합니다.

- 3-layer Architecture (3계층 아키텍처): 시스템을 컨트롤러, 비즈니스, 데이터 계층으로 나누어 각각의 역할을 명확하게 정의하고 유지보수성을 높였습니다.

- MongoData JPA: MongoDB와 JPA(Java Persistence API)를 결합하여 MongoDB에 데이터를 저장하고 조회하는 기능을 쉽게 처리할 수 있었습니다.

- Spring Scheduling: Spring Scheduling을 사용하여 실시간으로 데이터를 가져오고 비동기 처리를 통해 인기 키워드 및 동영상을 실시간으로 업데이트하고 저장했습니다.

- Ubuntu: 개발 및 배포 환경으로 Ubuntu OS를 사용하였으며, IntelliJ IDEA를 개발 환경으로 설정하여 Spring Boot 애플리케이션을 개발했습니다.

- WAR 파일 배포: 완성된 애플리케이션은 WAR(Web Application Archive) 파일로 빌드하여 Ubuntu 환경에 배포했습니다.


## 시스템 기능 및 설명

- 회원가입 및 인증 기능:
  1. JWT 기반의 인증 시스템을 통해 사용자는 로그인 및 회원가입을 할 수 있습니다.
  2. 사용자가 로그인한 후, 맞춤형 동영상 추천 및 인기 키워드 분석 기능을 제공받을 수 있습니다.

- YouTube API 통합:
  1. YouTube Data API를 통해 동영상 검색, 인기 동영상 조회, 추천 동영상 기능을 제공합니다.
  2. 인기 키워드를 실시간으로 분석하고, 이를 사용자에게 제공하는 기능을 구현했습니다.

- 인기 키워드 및 추천 동영상 분석:
  1. Apache Spark를 사용하여 실시간으로 인기 키워드 분석 및 추천 동영상을 추출하고 MongoDB에 저장합니다.
  2. 이 데이터는 실시간으로 비동기 처리를 통해 업데이트되며, 사용자가 접속할 때 최신 정보가 제공됩니다.

- 데이터 시각화: chart.js를 활용하여 인기 키워드 대시보드 형태로 시각화하여 제공되며, 사용자에게 유용한 정보를 쉽게 전달합니다.

- Spring Scheduling: Spring의 스케줄링 기능을 활용하여 주기적으로 YouTube API에서 데이터를 가져오고, 이를 MongoDB에 저장하여 실시간 업데이트를 지원합니다.

- 비동기 처리: 비동기 처리 기법을 사용하여 서버 부하를 줄이고, 사용자에게 빠른 응답을 제공할 수 있습니다.

## User Flow
![image](https://github.com/user-attachments/assets/062ecce8-01b2-4240-9500-fa50cddce28d)


## UI

![image](https://github.com/user-attachments/assets/78d2837e-5831-494d-aa8f-e88f24ec5541)

![image](https://github.com/user-attachments/assets/97588905-fb40-46da-b9c6-791408eef519)

![image](https://github.com/user-attachments/assets/c4667d90-e337-4b19-bc99-19281265e9e4)

![image](https://github.com/user-attachments/assets/a82188e2-5b0e-42db-8dae-774b4563f51c)

![image](https://github.com/user-attachments/assets/c230acc7-b031-496f-9748-db49eb336344)

![image](https://github.com/user-attachments/assets/8bdc1b08-9ab4-4878-989f-548d469e6a8a)

![image](https://github.com/user-attachments/assets/a57e7335-3ec3-4ec8-8132-ceaaf9fa90e1)

![image](https://github.com/user-attachments/assets/7b30312b-31e4-464c-8780-1b5a285eb4d0)


## MSD

![image](https://github.com/user-attachments/assets/0f279539-b18e-40bd-b38a-10d563e563a5)

