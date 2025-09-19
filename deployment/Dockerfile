
##FROM eclipse-temurin:21-jdk-alpine
#FROM gradle:8-jdk21 AS build
#WORKDIR /workspace
#COPY . .
#RUN ./gradlew :app-service:bootJar --no-daemon -x test
##COPY *.jar CrediYa_Auth.jar
#ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
#EXPOSE 8080
#COPY --from=build /workspace/applications/app-service/build/libs/*.jar /app/app.jar
#USER appuser
#ENTRYPOINT ["java","-jar","/app/app.jar"]
##ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar CrediYa_Auth.jar" ]



#FROM gradle:8-jdk21 AS build
#ARG BOOT_MODULE="app-service"
#WORKDIR /workspace
#COPY . .
#RUN gradle --no-daemon clean :${BOOT_MODULE}:bootJar -x test
#
#FROM eclipse-temurin:21-jre
#ARG JAR_DIR="applications/app-service/build/libs"
#WORKDIR /app
#COPY --from=build /workspace/${JAR_DIR}/*.jar /app/app.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","/app/app.jar"]


FROM gradle:8-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle --no-daemon clean :app-service:bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/applications/app-service/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]






#
#
#Boris fernandez
#7:22 p.m.
#FROM gradle:jdk17-alpine AS build
#
#COPY --chown=gradle:gradle . /home/gradle/project
#
#WORKDIR /home/gradle/project
#
#RUN gradle clean build -x test
#
#
#FROM eclipse-temurin:17-jdk
#VOLUME /tmp
#RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
#WORKDIR /app
#COPY --from=build /home/gradle/project/applications/app-service/build/libs/*.jar ms-solicitud.jar
#ENV JAVA_OPTS=" -XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
#USER appuser
#ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar ms-solicitud.jar" ]


