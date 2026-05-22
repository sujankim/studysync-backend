package com.sujan.studysync.service;

import com.sujan.studysync.model.User;
import com.sujan.studysync.model.UserStreak;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

// @Async = run email sending in a background thread
// This means the HTTP request returns IMMEDIATELY
// and the email is sent in the background
// The user doesn't have to wait for the email to send
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ─── Welcome Email ────────────────────────────────────────
    // Called right after a user registers
    @Async
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to StudySync! 🎓";
        String body    = buildWelcomeHtml(user);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    // ─── Weekly Digest ────────────────────────────────────────
    // Called by the scheduler every Monday
    @Async
    public void sendWeeklyDigest(User user, UserStreak streak) {
        String subject = "Your Weekly StudySync Report 📊";
        String body    = buildWeeklyDigestHtml(user, streak);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    // ─── Streak Reminder ──────────────────────────────────────
    // Called when a user hasn't studied in 24 hours
    @Async
    public void sendStreakReminder(User user, int currentStreak) {
        String subject = currentStreak > 0
                ? "Don't lose your " + currentStreak + "-day streak! 🔥"
                : "Come back to StudySync! 📚";
        String body    = buildStreakReminderHtml(user, currentStreak);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    // ─── Core Send Method ─────────────────────────────────────
    // MimeMessage = an email that can contain HTML (not plain text)
    private void sendHtmlEmail(
            String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // MimeMessageHelper = Spring's helper to build MimeMessages
            // true = multipart (allows HTML)
            // "UTF-8" = encoding for special characters
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom(fromEmail, "StudySync");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = isHtml

            mailSender.send(message);
            log.info("Email sent to: {}", to);

        } catch (MessagingException e) {
            // Log the error but don't crash the app
            // Email failures should NEVER break core functionality
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected email error to {}: {}", to, e.getMessage());
        }
    }

    // ─── HTML Templates ───────────────────────────────────────
    // These are inline HTML strings
    // In production you'd use Thymeleaf templates — keeping simple here

    private String buildWelcomeHtml(User user) {
        String firstName = user.getName().split(" ")[0];
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Inter, Arial, sans-serif;
                             background: #f5f3ff; padding: 40px;">
                  <div style="max-width: 600px; margin: 0 auto;
                              background: white; border-radius: 16px;
                              padding: 40px; box-shadow: 0 4px 20px rgba(124,58,237,0.1);">

                    <div style="text-align: center; margin-bottom: 32px;">
                      <h1 style="font-size: 28px; color: #7c3aed; margin: 0;">
                        📚 StudySync
                      </h1>
                    </div>

                    <h2 style="color: #1e1b4b; font-size: 22px;">
                      Welcome, %s! 👋
                    </h2>

                    <p style="color: #5b5b8a; line-height: 1.7;">
                      You've joined a community of thousands of learners
                      who study together, stay consistent, and achieve more.
                    </p>

                    <p style="color: #5b5b8a; line-height: 1.7;">
                      Here's what you can do on StudySync:
                    </p>

                    <ul style="color: #5b5b8a; line-height: 2;">
                      <li>🏠 Join topic-based study rooms</li>
                      <li>💬 Chat in real time with other learners</li>
                      <li>📁 Share resources — PDFs, links, notes</li>
                      <li>🔥 Build daily study streaks</li>
                      <li>📊 Track your progress with analytics</li>
                    </ul>

                    <div style="text-align: center; margin: 32px 0;">
                      <a href="%s/rooms"
                         style="background: linear-gradient(135deg, #5b21b6, #7c3aed);
                                color: white; padding: 14px 32px;
                                border-radius: 10px; text-decoration: none;
                                font-weight: 700; font-size: 15px;">
                        Explore Study Rooms →
                      </a>
                    </div>

                    <p style="color: #9090b8; font-size: 13px;
                              text-align: center; margin-top: 32px;">
                      © 2026 StudySync. You're receiving this because
                      you created an account.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(firstName, frontendUrl);
    }

    private String buildWeeklyDigestHtml(
            User user, UserStreak streak) {
        String firstName = user.getName().split(" ")[0];
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Inter, Arial, sans-serif;
                             background: #f5f3ff; padding: 40px;">
                  <div style="max-width: 600px; margin: 0 auto;
                              background: white; border-radius: 16px;
                              padding: 40px;">

                    <h1 style="color: #7c3aed;">📊 Your Weekly Report</h1>

                    <p style="color: #5b5b8a;">
                      Here's how you studied this week, %s!
                    </p>

                    <div style="background: #f5f3ff; border-radius: 12px;
                                padding: 24px; margin: 24px 0;">
                      <div style="display: flex; gap: 24px;
                                  flex-wrap: wrap;">
                        <div style="text-align: center; flex: 1;">
                          <div style="font-size: 32px; font-weight: 900;
                                      color: #7c3aed;">
                            %d
                          </div>
                          <div style="color: #5b5b8a; font-size: 14px;">
                            Day Streak 🔥
                          </div>
                        </div>
                        <div style="text-align: center; flex: 1;">
                          <div style="font-size: 32px; font-weight: 900;
                                      color: #06b6d4;">
                            %d
                          </div>
                          <div style="color: #5b5b8a; font-size: 14px;">
                            Total Days 📅
                          </div>
                        </div>
                        <div style="text-align: center; flex: 1;">
                          <div style="font-size: 32px; font-weight: 900;
                                      color: #10b981;">
                            %dh
                          </div>
                          <div style="color: #5b5b8a; font-size: 14px;">
                            Total Study Time ⏱️
                          </div>
                        </div>
                      </div>
                    </div>

                    <div style="text-align: center; margin: 24px 0;">
                      <a href="%s/analytics"
                         style="background: linear-gradient(135deg, #5b21b6, #7c3aed);
                                color: white; padding: 14px 32px;
                                border-radius: 10px; text-decoration: none;
                                font-weight: 700;">
                        View Full Analytics →
                      </a>
                    </div>

                  </div>
                </body>
                </html>
                """.formatted(
                firstName,
                streak.getCurrentStreak(),
                streak.getTotalDays(),
                streak.getTotalMinutes() / 60,
                frontendUrl);
    }

    private String buildStreakReminderHtml(
            User user, int currentStreak) {
        String firstName = user.getName().split(" ")[0];
        String headline  = currentStreak > 0
                ? "Your " + currentStreak + "-day streak is at risk! 🔥"
                : "Come back and start a new streak! 📚";

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Inter, Arial, sans-serif;
                             background: #f5f3ff; padding: 40px;">
                  <div style="max-width: 600px; margin: 0 auto;
                              background: white; border-radius: 16px;
                              padding: 40px;">

                    <h1 style="color: #f59e0b;">%s</h1>

                    <p style="color: #5b5b8a; line-height: 1.7;">
                      Hey %s! You haven't studied today yet.
                      %s
                    </p>

                    <div style="text-align: center; margin: 24px 0;">
                      <a href="%s/rooms"
                         style="background: linear-gradient(135deg, #d97706, #f59e0b);
                                color: white; padding: 14px 32px;
                                border-radius: 10px; text-decoration: none;
                                font-weight: 700;">
                        Study Now →
                      </a>
                    </div>

                  </div>
                </body>
                </html>
                """.formatted(
                headline,
                firstName,
                currentStreak > 0
                        ? "Study for at least 1 minute to keep your streak alive!"
                        : "Even 15 minutes of study makes a difference.",
                frontendUrl);
    }

    @Async
    public void sendRenewalReminder(User user, LocalDate expiryDate) {
        String subject = "Your StudySync Pro expires on "
                + expiryDate + " 🔔";
        String body    = """
            <!DOCTYPE html>
            <html>
            <body style="font-family:Inter,Arial,sans-serif;
                         background:#f5f3ff; padding:40px;">
              <div style="max-width:600px; margin:0 auto;
                          background:white; border-radius:16px;
                          padding:40px;">
                <h1 style="color:#7c3aed;">⏰ Pro Plan Expiring Soon</h1>
                <p style="color:#5b5b8a;">
                  Hey %s! Your StudySync Pro plan expires on
                  <strong>%s</strong>.
                </p>
                <p style="color:#5b5b8a;">
                  Renew now to keep unlimited rooms,
                  advanced analytics, and all Pro features.
                </p>
                <div style="text-align:center; margin:24px 0;">
                  <a href="%s/billing"
                     style="background:linear-gradient(135deg,#5b21b6,#7c3aed);
                            color:white; padding:14px 32px;
                            border-radius:10px; text-decoration:none;
                            font-weight:700;">
                    Renew Pro — NPR 100 →
                  </a>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                user.getName().split(" ")[0],
                expiryDate,
                frontendUrl);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendSubscriptionExpired(User user) {
        String subject = "Your StudySync Pro has expired 😢";
        String body    = """
            <!DOCTYPE html>
            <html>
            <body style="font-family:Inter,Arial,sans-serif;
                         background:#f5f3ff; padding:40px;">
              <div style="max-width:600px; margin:0 auto;
                          background:white; border-radius:16px;
                          padding:40px;">
                <h1 style="color:#ef4444;">Pro Plan Expired</h1>
                <p style="color:#5b5b8a;">
                  Hey %s, your StudySync Pro plan has expired.
                  You've been moved to the free plan.
                </p>
                <p style="color:#5b5b8a;">
                  Renew anytime for just NPR 100/month to get
                  unlimited rooms and advanced analytics back.
                </p>
                <div style="text-align:center; margin:24px 0;">
                  <a href="%s/billing"
                     style="background:linear-gradient(135deg,#5b21b6,#7c3aed);
                            color:white; padding:14px 32px;
                            border-radius:10px; text-decoration:none;
                            font-weight:700;">
                    Renew Pro →
                  </a>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                user.getName().split(" ")[0],
                frontendUrl);

        sendHtmlEmail(user.getEmail(), subject, body);
    }
}
