FROM openjdk:17-jdk-alpine
COPY springboot-application/target/piae-sem-webapp-0.0.1-SNAPSHOT.jar piae-sem-webapp-0.0.1-SNAPSHOT.jar
#COPY wait-for-it.sh /app/wait-for-it.sh
ENTRYPOINT ["java","-jar","/piae-sem-webapp-0.0.1-SNAPSHOT.jar"]
#ENTRYPOINT ["./wait-for-it.sh", "mysql-db:3306", "--timeout=60", "--", "java", "-jar", "/piae-sem-webapp-0.0.1-SNAPSHOT.jar"]