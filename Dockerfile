# Step 1: Build Stage (Maven use karke JAR file banayenge)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Sabse pehle pom.xml copy karke dependencies download karte hain (Build speed ke liye)
COPY pom.xml .
RUN mvn dependency:go-offline

# Ab poora code copy karke final build banate hain
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Run Stage (Sirf JAR file ko chalayenge, light-weight image banegi)
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Build stage se final JAR file yahan copy kar rahe hain
COPY --from=build /app/target/*.jar app.jar

# Port define kar rahe hain
EXPOSE 8080

# Application start karne ki command
ENTRYPOINT ["java", "-jar", "app.jar"]