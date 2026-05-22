package com.sujan.studysync.service;

import com.sujan.studysync.enums.Plan;
import com.sujan.studysync.model.Subscription;
import com.sujan.studysync.model.User;
import com.sujan.studysync.model.UserStreak;
import com.sujan.studysync.repository.StudySessionRepository;
import com.sujan.studysync.repository.SubscriptionRepository;
import com.sujan.studysync.repository.UserRepository;
import com.sujan.studysync.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// This service contains all @Scheduled methods
// @Scheduled = Spring runs this method automatically on a schedule
// No HTTP request needed — Spring's TaskScheduler calls it directly
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledEmailService {

    private final UserRepository         userRepository;
    private final UserStreakRepository   streakRepository;
    private final StudySessionRepository sessionRepository;
    private final EmailService           emailService;
    private final SubscriptionRepository subscriptionRepository;

    // ─── Weekly Digest ────────────────────────────────────────
    // cron = "second minute hour dayOfMonth month dayOfWeek"
    // "0 0 9 * * MON" = every Monday at 9:00 AM
    @Scheduled(cron = "0 0 9 * * MON")
    @Transactional(readOnly = true)
    public void sendWeeklyDigests() {
        log.info("Starting weekly digest emails...");

        // Get ALL users — send digest to everyone
        List<User> users = userRepository.findAll();
        int sent = 0;

        for (User user : users) {
            try {
                UserStreak streak = streakRepository
                        .findByUser(user)
                        .orElse(null);

                if (streak != null) {
                    // Only send if they have SOME study history
                    emailService.sendWeeklyDigest(user, streak);
                    sent++;
                }
            } catch (Exception e) {
                // Log individual failure but keep processing others
                log.error("Failed digest for user {}: {}",
                        user.getEmail(), e.getMessage());
            }
        }

        log.info("Weekly digest sent to {} users", sent);
    }

    // ─── Streak Reminder ──────────────────────────────────────
    // Every day at 10:00 AM
    // Checks: did this user study yesterday?
    // If NOT → send reminder to protect their streak
    @Scheduled(cron = "0 0 10 * * *")
    @Transactional(readOnly = true)
    public void sendStreakReminders() {
        log.info("Starting streak reminder emails...");

        // "Yesterday" = from midnight yesterday to midnight today
        LocalDateTime yesterdayStart =
                LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime todayStart =
                LocalDate.now().atStartOfDay();

        List<User> users = userRepository.findAll();
        int sent = 0;

        for (User user : users) {
            try {
                // Did this user study yesterday?
                Integer minutesYesterday = sessionRepository
                        .sumMinutesBetween(
                                user, yesterdayStart, todayStart);

                boolean studiedYesterday =
                        minutesYesterday != null
                                && minutesYesterday > 0;

                if (!studiedYesterday) {
                    // They didn't study — send reminder
                    int currentStreak = streakRepository
                            .findByUser(user)
                            .map(UserStreak::getCurrentStreak)
                            .orElse(0);

                    emailService.sendStreakReminder(user, currentStreak);
                    sent++;
                }
            } catch (Exception e) {
                log.error("Failed reminder for user {}: {}",
                        user.getEmail(), e.getMessage());
            }
        }

        log.info("Streak reminders sent to {} users", sent);
    }

    // ─── Renewal Reminders ────────────────────────────────────────
// Every day at 9AM — check subscriptions expiring in 3 days
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendRenewalReminders() {
        log.info("Checking for expiring subscriptions...");

        LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);

        List<Subscription> expiring = subscriptionRepository
                .findExpiringSoon(threeDaysFromNow);

        for (Subscription sub : expiring) {
            try {
                emailService.sendRenewalReminder(
                        sub.getUser(), sub.getPeriodEnd());
                sub.setRenewalEmailSent(true);
                subscriptionRepository.save(sub);
            } catch (Exception e) {
                log.error("Failed renewal reminder for {}: {}",
                        sub.getUser().getEmail(), e.getMessage());
            }
        }

        log.info("Renewal reminders sent: {}", expiring.size());
    }

    // ─── Expire Subscriptions ─────────────────────────────────────
// Every day at midnight — downgrade expired Pro users
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireSubscriptions() {
        log.info("Checking for expired subscriptions...");

        List<Subscription> expired = subscriptionRepository
                .findExpired(LocalDate.now());

        for (Subscription sub : expired) {
            try {
                sub.setStatus("expired");
                subscriptionRepository.save(sub);

                // Downgrade user to FREE
                User user = sub.getUser();
                user.setPlan(Plan.FREE);
                userRepository.save(user);

                emailService.sendSubscriptionExpired(user);
                log.info("Downgraded: {}", user.getEmail());

            } catch (Exception e) {
                log.error("Failed expiry for {}: {}",
                        sub.getUser().getEmail(), e.getMessage());
            }
        }
    }

}
