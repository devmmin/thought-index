# 1단계: 빌드 (JDK 가 있는 무거운 이미지에서 jar 를 만든다)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# 의존성 선언 파일만 먼저 복사 → 소스만 바뀌면 이 층(라이브러리 다운로드)은 Docker 캐시를 재사용
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon -q > /dev/null || true
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 2단계: 실행 (JRE 만 있는 가벼운 이미지에 jar 하나만 옮긴다)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# 무료 서버는 메모리가 512MB 라서 JVM 이 컨테이너 메모리의 70% 까지만 쓰게
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
