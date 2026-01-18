# Java 21 런타임 이미지를 기반으로 설정
FROM eclipse-temurin:21-jdk-jammy

# 컨테이너 내 작업 디렉토리 설정
WORKDIR /app

# 빌드 시 생성된 jar 파일을 컨테이너 내부로 복사
# Gradle 빌드 결과물인 build/libs/*.jar 파일을 app.jar로 복사합니다.
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 컨테이너 실행 시 애플리케이션 실행 명령어
# 메모리 효율을 위해 JVM 옵션을 추가할 수 있습니다.
ENTRYPOINT ["java", "-jar", "app.jar"]

# 서비스 포트 노출 (Spring Boot 기본값 8080)
EXPOSE 8080