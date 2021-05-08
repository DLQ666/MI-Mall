FROM java:8
#EXPOSE 8080

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","-Xms100m","-Xmx200m","/app.jar","--spring.profiles.active=prod"]
