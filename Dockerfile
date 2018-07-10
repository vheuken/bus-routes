FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/bus-routes.jar /bus-routes/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/bus-routes/app.jar"]
