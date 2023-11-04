FROM --platform=linux/amd64 openjdk:17-alpine
COPY ./build/libs/corn-1.jar corn.jar
EXPOSE 3000
CMD ["java", "-XX:+UseG1GC","-Dspring.profiles.active=prod", "-jar", "corn.jar"]