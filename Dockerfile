FROM openjdk:11-jre-slim
COPY backend/target/scala-2.12/galley.jar /gallery.jar
CMD ["/usr/bin/java", "-jar", "/gallery.jar"]