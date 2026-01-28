package Lab_2;
import module java.base;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

public class Lab_2 {
    void main() {
        Dotenv dotenv = Dotenv.load();

        List<String> recipients = List.of(
                "LandonMatthew.Petriccione@calbaptist.edu",
                "MichaelJohn.Romero@calbaptist.edu",
                "SavannahS.Punak@calbaptist.edu",
                "NathanA.Castaneda@calbaptist.edu",
                "Brian.Reyes@calbaptist.edu",
                "LoganPatrick.Elwell@calbaptist.edu",
                "AaronAshton.Teasley@calbaptist.edu",
                "ShaunN.Thomas@calbaptist.edu",
                "Eleonora.Visnevskyte@calbaptist.edu",
                "JoaquinDiego.Luna@calbaptist.edu",
                "DylanMateo.Lopez@calbaptist.edu",
                "David.Zuniga@calbaptist.edu",
                "Sandy.Thomas@calbaptist.edu",
                "ArturoG.Valle@calbaptist.edu",
                "JeremyMatthew.Freeman@calbaptist.edu",
                "Eric.Leon@calbaptist.edu",
                "WalkerSaige.Robertson@calbaptist.edu",
                "ElliottMathew.Willer@calbaptist.edu",
                "CalebJohn.VanRandwyk@calbaptist.edu",
                "GiovanniDaCostaMello.Ianicelli@calbaptist.edu",
                "Isaac.Vass@calbaptist.edu",
                "ChristopherShawn.Stock@calbaptist.edu",
                "Linsey.Miranda@calbaptist.edu",
                "Jonathan.Serna@calbaptist.edu",
                "Christian.Legaspi@calbaptist.edu",
                "Joshua.Rivera1@calbaptist.edu",
                "LoganMicheal.Clements@calbaptist.edu"
        );

        List<String> oneRecipient = List.of(
                "ElijahD.Tabor@calbaptist.edu"
        );

        sendBulkEmail(dotenv, oneRecipient);
    }

    public static void sendBulkEmail(Dotenv dotenv, List<String> recipients) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.office365.com");
        properties.put("mail.smtp.host", dotenv.get("HOST"));
        properties.put("mail.smtp.port", dotenv.get("PORT"));

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(dotenv.get("EMAIL_USERNAME"), dotenv.get("EMAIL_PASSWORD"));
            }
        });

        int successCount = 0;
        int failCount = 0;

        for (String recipient : recipients) {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(dotenv.get("EMAIL_USERNAME"), "Andy B. Vowell-bvowell@calbaptist.edu"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject("2026-SP-UT GST050-A-Chapel Convocation: The THIRD CHAPEL of the Semester is CANCELLED");

                String encodedEmail = java.net.URLEncoder.encode(recipient, java.nio.charset.StandardCharsets.UTF_8);
                String chapelScheduleLink = "https://script.google.com/macros/s/AKfycbwCwotVhud5rhVjIfWqio7G2W_2nXPq0EjluQZgPIRkYK7mokjAr7q-SkWvQ0ui-a-yNg/exec?email=" + encodedEmail;

                String htmlContent = """
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            color: #000000;
                            margin: 0;
                            padding: 0;
                            line-height: 1.6;
                        }
                        .intro {
                            color: #d9534f;
                            margin-bottom: 15px;
                        }
                        .intro a {
                            color: #d9534f;
                            text-decoration: underline;
                        }
                        h1 {
                            font-size: 18px;
                            font-weight: bold;
                            margin: 15px 0;
                            line-height: 1.4;
                        }
                        .alert {
                            color: #d9534f;
                            margin: 15px 0;
                        }
                        .alert a {
                            color: #d9534f;
                        }
                        .emphasis {
                            font-weight: bold;
                        }
                        .underline {
                            text-decoration: underline;
                        }
                        .schedule-link {
                            display: inline-block;
                            background-color: #0066cc;
                            color: #ffffff;
                            padding: 10px 20px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin: 20px 0;
                            font-weight: bold;
                        }
                        .schedule-link:hover {
                            background-color: #0052a3;
                        }
                        ol {
                            padding-left: 25px;
                            margin: 10px 0;
                        }
                        li {
                            margin: 10px 0;
                        }
                        p {
                            margin: 10px 0;
                        }
                    </style>
                </head>
                <body>
                    <p class="intro">
                        This message is being distributed to all students enrolled in\s
                        <a href="#">Section A</a> GST050 Chapel/Convocation:
                    </p>
                   \s
                    <h1>
                        The third Chapel of the Spring 2026 semester scheduled for Tuesday, January 27th\s
                        in the Fowler Events Center has been cancelled due to flooding in the Events Center.\s
                        An additional online Chapel will be made available on Blackboard to replace this session.
                    </h1>
                   \s
                    <p style="text-align: center;">
                        <a href="%s" class="schedule-link">View Updated Chapel Schedule</a>
                    </p>
                   \s
                    <p class="alert">
                        You are currently enrolled in <a href="#">Section A of GST050 Chapel/Convocation</a>.\s
                        You will be expected to acquire a minimum of 15 credits for the Spring 2026 semester\s
                        by attending the <span class="underline">9:00 am in-person Chapel Service</span> and\s
                        viewing the Online Chapel options.
                    </p>
                   \s
                    <p>
                        There will be a total of <span class="emphasis">18 opportunities</span> to acquire\s
                        Chapel credit during the spring semester: <span class="emphasis">12 in-person Tuesday Chapels</span>,\s
                        and <span class="emphasis">6 asynchronous online Chapels</span> (including the additional\s
                        online Chapel replacing the cancelled January 27th session) that will post to\s
                        Blackboard on specified dates throughout the semester. To receive a passing grade\s
                        at the end of the semester, a student must acquire at least\s
                        <span class="emphasis">15 credits</span>.
                    </p>
                   \s
                    <ol>
                        <li>
                            Doors to the Fowler Events Center are scheduled to open at approximately 8:35am.\s
                            Chapel starts promptly at 9:00am. Chapel scanning for credit ends at 9:05am.
                        </li>
                        <li>
                            <span class="emphasis">Don't forget your CBU Card!</span> In order to receive\s
                            Chapel credit, students must present a valid CBU Card (Student ID) at the time\s
                            of arrival. Then each individual will acknowledge that the proper identification\s
                            and Chapel date/time has appeared correctly in the Chapel Tracking System by\s
                            tapping the CONFIRM button on the screen. Students arriving at the Events Center\s
                            without a CBU Card may sign in to receive credit, however, this exception is\s
                            allowed <span class="underline">a maximum of two times per semester</span>.
                        </li>
                        <li>
                            Skateboards and scooters brought to the Events Center must be stored in the\s
                            skateboard racks along the east side of the Events Center Plaza. There will be\s
                            absolutely no skateboards or scooters permitted within the Events Center. Bicycles\s
                            brought to the Events Center should be parked at the rack near Lancer Arms,\s
                            Lancer Plaza North, or another nearby location.
                        </li>
                    </ol>
                   \s
                    <p style="margin-top: 20px;">
                        Questions regarding Chapel at CBU should be directed to\s
                        <a href="mailto:chapel@calbaptist.edu">chapel@calbaptist.edu</a>.
                    </p>
                </body>
                </html>
               \s""".formatted(chapelScheduleLink);

                message.setContent(htmlContent, "text/html; charset=utf-8");

                Transport.send(message);
                successCount++;
                System.out.println("Email sent successfully to: " + recipient);

            } catch (MessagingException | UnsupportedEncodingException e) {
                failCount++;
                System.err.println("Failed to send email to: " + recipient);
                System.err.println("  Error: " + e.getMessage());
            }
        }

        System.out.println("Total recipients: " + recipients.size());
        System.out.println("Successfully sent: " + successCount);
        System.out.println("Failed: " + failCount);
    }
}