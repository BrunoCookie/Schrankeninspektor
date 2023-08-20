FROM jelastic/maven:3.8.6-openjdk-20.ea-b24
WORKDIR /app
COPY . .
ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN mvn clean install -DskipTests
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/Schrankeninspektor-0.0.1-SNAPSHOT.jar"]