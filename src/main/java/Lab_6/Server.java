package Lab_6;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.UnsupportedEncodingException;

public class Server {

    private static Path baseDir;

    public static void main(String[] args) throws IOException {
        baseDir = Paths.get(System.getProperty("user.dir"), "src", "main", "java", "Lab_6");
        System.out.println("Serving files from: " + baseDir.toAbsolutePath());

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/submit", new SubmitHandler());
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server is running on http://localhost:8000/");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();

            String fileName;
            String contentType;

            if (requestPath.equals("/background.png")) {
                fileName = "background.png";
                contentType = "image/png";
            } else if (requestPath.equals("/logo.png")) {
                fileName = "logo.png";
                contentType = "image/png";
            } else {
                fileName = "index.html";
                contentType = "text/html; charset=UTF-8";
            }

            Path filePath = baseDir.resolve(fileName);

            if (!Files.exists(filePath)) {
                String notFound = "404 Not Found: " + filePath.toAbsolutePath();
                byte[] notFoundBytes = notFound.getBytes();
                exchange.sendResponseHeaders(404, notFoundBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(notFoundBytes);
                os.close();
                return;
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(fileBytes);
            outputStream.flush();
            outputStream.close();
        }
    }

    static class SubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String email = extractEmail(body);

            System.out.println("Received email: " + email);

            if (email != null && !email.isEmpty()) {
                try {
                    sendEmail(email);
                    System.out.println("Email sent to: " + email);
                } catch (Exception e) {
                    System.err.println("Failed to send email: " + e.getMessage());
                }
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.write("OK".getBytes());
            os.close();
        }

        private String extractEmail(String body) throws UnsupportedEncodingException {
            for (String param : body.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && kv[0].equals("email")) {
                    return URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name());
                }
            }
            return null;
        }

        private void sendEmail(String toEmail) throws MessagingException, IOException {
            String fromEmail = "ryanstoffel44@gmail.com";
            String appPassword = "ofli jncf bezo bbet";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, appPassword);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Got you!");

            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("You just got phished! This was a security awareness exercise. Stay vigilant!");

            MimeBodyPart imagePart = new MimeBodyPart();
            Path imagePath = baseDir.resolve("prank.png");
            imagePart.attachFile(imagePath.toFile());
            imagePart.setFileName("prank.png");

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(imagePart);

            message.setContent(multipart);
            Transport.send(message);
        }
    }
}