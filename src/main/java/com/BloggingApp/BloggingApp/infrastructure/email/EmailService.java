package com.BloggingApp.BloggingApp.infrastructure.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final JavaMailSender javaMailSender;

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(htmlBody, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("Blogging App <" + senderEmail + ">");

            javaMailSender.send(mimeMessage);
            System.out.println("✅ Email sent successfully to: " + to);
        } catch (Exception e) {
            // 🔥 Exception throw mat karo, bas log kar do
            System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public void sendCommentNotification(String ownerEmail, String ownerName, String commenterName, String postTitle, String commentContent) {
        String subject = "🔔 New Comment Alert: " + postTitle;

        // Ye hai tera "Mst" Design
        String htmlBody = """
            <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 20px auto; border: 1px solid #eee; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05);">
                <div style="background-color: #6c63ff; padding: 25px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">New Interaction!</h1>
                </div>
                <div style="padding: 30px; line-height: 1.6; color: #444;">
                    <p style="font-size: 18px;">Hi <b>%s</b>,</p>
                    <p>Good news! Your content is getting attention. <b>%s</b> just commented on your post:</p>
                    
                    <div style="background-color: #f8f9fa; border-left: 5px solid #6c63ff; padding: 20px; margin: 25px 0; border-radius: 4px; font-style: italic; color: #555;">
                        <h4 style="margin-top: 0; color: #6c63ff;">"%s"</h4>
                        <p style="margin-bottom: 0; font-size: 14px;">— %s</p>
                    </div>
                    
                    <div style="text-align: center; margin-top: 30px;">
                        <a href="#" style="background-color: #6c63ff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 25px; font-weight: bold; display: inline-block;">View Comment</a>
                    </div>
                </div>
                <div style="background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #888;">
                    <p>Sent with ❤️ from Blogging App Team</p>
                </div>
            </div>
            """.formatted(ownerName, commenterName, postTitle, commentContent);

        sendHtmlEmail(ownerEmail, subject, htmlBody);
    }


    public void sendWelcomeEmail(String userEmail, String userName) {
        // Subject mein bhi %s format use karne ke liye String.format lagana padega
        String subject = String.format("Welcome to the Community, %s! 🚀", userName);

        String htmlBody = """
        <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 20px auto; border: 1px solid #eee; border-radius: 15px; overflow: hidden; box-shadow: 0 10px 20px rgba(0,0,0,0.1);">
            <div style="background: linear-gradient(135deg, #6c63ff 0%%, #3f3d56 100%%); padding: 40px; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 28px; letter-spacing: 1px;">Welcome to Blogging App!</h1>
                <p style="color: #e0e0e0; margin-top: 10px;">Where your stories find a home.</p>
            </div>
            
            <div style="padding: 40px; line-height: 1.8; color: #444; background-color: #ffffff;">
                <p style="font-size: 18px; margin-bottom: 20px;">Hi <b>%s</b>,</p>
                <p>We are absolutely thrilled to have you join our creative community! Whether you're here to share your tech journey, write poetry, or post your latest trekking adventures, you're in the right place.</p>
                
                <div style="background-color: #f4f7ff; padding: 25px; border-radius: 10px; margin: 30px 0; text-align: center; border: 1px dashed #6c63ff;">
                    <h3 style="margin: 0; color: #6c63ff;">Ready to write your first post?</h3>
                    <p style="font-size: 14px; color: #666; margin: 10px 0 20px 0;">Don't keep your ideas waiting. Share them with the world!</p>
                    <a href="http://localhost:3000/write" style="background-color: #6c63ff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 50px; font-weight: bold; display: inline-block; box-shadow: 0 4px 15px rgba(108, 99, 255, 0.3);">Start Blogging Now</a>
                </div>
                
                <p>If you have any questions, just hit reply. We're always here to help.</p>
                
                <p style="margin-top: 40px; border-top: 1px solid #eee; padding-top: 20px;">
                    Happy Writing,<br>
                    <span style="color: #6c63ff; font-weight: bold; font-size: 18px;">Rohit Joshi</span><br>
                    <small style="color: #888;">Founder, Blogging App Team</small>
                </p>
            </div>
            
            <div style="background-color: #f9f9f9; padding: 20px; text-align: center; font-size: 12px; color: #aaa;">
                <p>You received this email because you signed up for Blogging App.<br>
                © 2026 Blogging App. All rights reserved.</p>
            </div>
        </div>
        """.formatted(userName); // Sirf userName chahiye yahan

        sendHtmlEmail(userEmail, subject, htmlBody);
    }

//    @Async
//    public void sendEmail(String to, String subject, String body){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//        javaMailSender.send(message);
//    }
//
//    public void sendCommentNotification(String ownerEmail, String ownerName, String commenterName, String postTitle, String commentContent){
//        String subject = "New Comment on: " + postTitle;
//
//        String body = "Hi " + ownerName + ", \n\n" +
//                      "Great news🤗 " + commenterName + " just commented on your post '" + postTitle + "'.\n\n" +
//                      "Comment: \"" + commentContent + "\"\n\n" +
//                      "Keep up the great writing!🎉\n" +
//                      "Best regards💌,\n" +
//                      "Blogging App Team🧑‍💻";
//
//        sendEmail(ownerEmail, subject, body);
//    }
}


/*

🎨 Plain Text vs HTML Email
Abhi hum SimpleMailMessage use kar rahe hain jo sirf saada text bhej sakta hai. Isse "Congested" (bhara-bhara) lagta hai kyunki hum font, color, ya buttons add nahi kar sakte.

Professional apps (Youtube, LinkedIn) HTML templates bhejte hain jisme:

Fonts thode bade aur saaf hote hain.

Background color hota hai.

Ek mast "View Comment" wala button hota hai.

🛠️ Step 1: EmailService ko Upgrade Karo
Ab hum SimpleMailMessage ko hata kar MimeMessage use karenge. Isse tu HTML tags (<h1>, <b>, <div>) use kar payega.
💅 Step 2: "Mst" HTML Template (The Logic)
Ab sendCommentNotification method mein hum ek mast design wala HTML string banayenge.
*/