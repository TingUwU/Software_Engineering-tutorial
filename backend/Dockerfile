# --- 第一階段：建置 (Build Stage) ---
# 使用 Java 17 的 JDK 來編譯程式
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# 1. 先複製 maven wrapper 和 pom.xml (利用 Docker 快取機制加速)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# 給予 mvnw 執行權限 (這步非常重要，Linux 環境常因沒權限而失敗)
RUN chmod +x mvnw

# 2. 下載依賴 (預先下載好，之後改程式碼就不用重新下載 jar 包)
RUN ./mvnw dependency:go-offline

# 3. 複製原始碼並開始打包
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- 第二階段：執行 (Run Stage) ---
# 使用輕量版的 JRE 來執行 (檔案小、啟動快)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 從第一階段把編譯好的 jar 檔複製過來
# 注意：這裡假設 target 下只有一個 jar 檔，通常是對的
COPY --from=build /app/target/*.jar app.jar

# 告訴 Docker 這個容器會用 8088 Port
EXPOSE 8088

# 啟動指令
ENTRYPOINT ["java", "-jar", "app.jar"]