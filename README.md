# ExamDesk website
Requires JDK 11 or newer. No external dependencies. In this folder run:

    javac --release 11 ExamDesk.java ExamDeskServer.java
    java ExamDeskServer

Open http://localhost:8765 in your browser. Keep terminal open. Do not open index.html directly because calculations run in Java. Stop with Ctrl+C. All six modules accept custom input. The server is local only; no database or authentication. To run original tests: java ExamDesk test
