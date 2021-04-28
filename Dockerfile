FROM openjdk:11.0-jre
ARG JAR_FILE=target/yang-0.0.1-SNAPSHOT.jar
ARG DIR_PATH=src/main/resources/YANG_FILES

RUN mkdir /YANG_FILE_DIR
COPY ${JAR_FILE} yang-parser.jar
COPY ${DIR_PATH} /YANG_FILE_DIR
EXPOSE 8081
ENTRYPOINT ["java","-jar","yang-parser.jar"]
