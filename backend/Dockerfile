FROM gradle:5.6.2-jdk12 as BUILD

RUN mkdir -p /home/gradle/src
WORKDIR /home/gradle/src
COPY . /home/gradle/src
RUN gradle --no-daemon shadowJar

FROM openjdk:13-oraclelinux7

COPY --from=BUILD /home/gradle/src/build/libs/HueIT-backend.jar /bin/runner/run.jar
WORKDIR /bin/runner

ENTRYPOINT ["java","-jar","run.jar"]