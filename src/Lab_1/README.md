# *CSC431-A - Info Security & Computer Forensics - Ryan Stoffel*
This project requires two Java Dependencies:
```
com.sun.mail:jakarta.mail:2.0.1
io.github.cdimascio:dotenv-java:3.0.0
```
Make sure to install them using Maven.

Next, you want to set up a .env file in your projects root. In this file provide these credentials:
```
EMAIL_USERNAME=example@gmail.com
EMAIL_PASSWORD=GMAIL APP SPECIFIC PASSWORD
HOST=smtp.gmail.com
PORT=587
```

Then to run my program, make sure you open Lab_1.java, and specify who you want to send the email to. Once you have done that, run the Java program by using this command:
```
java Lab_1.java
```
You will get a console message telling you that the email has been sent successfully.