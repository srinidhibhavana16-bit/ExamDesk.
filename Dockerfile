FROM eclipse-temurin:11-jdk
WORKDIR /app
COPY . .
RUN javac --release 11 ExamDesk.java ExamDeskServer.java && java ExamDesk test
EXPOSE 10000
ENV PORT=10000
CMD ["java", "ExamDeskServer"]
