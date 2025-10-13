# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 先拷貝 pom 以加速相依快取
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# 拷貝原始碼並建置
COPY . .
RUN mvn -q -DskipTests clean package

# ===== Run stage =====
FROM eclipse-temurin:17-jre
WORKDIR /app
EXPOSE 3000

# 複製可執行 jar
COPY --from=builder /app/target/*.jar app.jar

# 啟動 Spring Boot
ENTRYPOINT ["java","-jar","/app/app.jar"]
