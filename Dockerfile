# 1. Java 17이 설치된 이미지 사용
FROM eclipse-temurin:17-jdk

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 전체 프로젝트 복사
COPY . .

# 4. gradlew에 실행 권한 부여
RUN chmod +x ./gradlew

# 5. 빌드 실행 (jar 파일 생성)
RUN ./gradlew build

# 6. jar 실행
CMD ["java", "-jar", "build/libs/fishtrip-1.0-SNAPSHOT.jar"]
