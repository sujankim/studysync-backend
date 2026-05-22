package com.sujan.studysync.repository;

import com.sujan.studysync.model.Subscription;
import com.sujan.studysync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUser(User user);

    Optional<Subscription> findByKhaltiPidx(String pidx);

    // Find active subscriptions expiring soon (for renewal reminders)
    // "expiring soon" = period ends within the next 3 days
    @Query("""
        SELECT s FROM Subscription s
        WHERE s.status = 'active'
        AND s.periodEnd <= :threeDaysFromNow
        AND s.renewalEmailSent = false
        """)
    List<Subscription> findExpiringSoon(
            @Param("threeDaysFromNow") LocalDate threeDaysFromNow);

    // Find subscriptions that have already expired
    @Query("""
        SELECT s FROM Subscription s
        WHERE s.status = 'active'
        AND s.periodEnd < :today
        """)
    List<Subscription> findExpired(
            @Param("today") LocalDate today);

    // Avoids LazyInitializationException when accessing subscription.getUser()
    @Query("""
    SELECT s FROM Subscription s
    JOIN FETCH s.user
    WHERE s.khaltiPidx = :pidx
    """)
    Optional<Subscription> findByKhaltiPidxWithUser(
            @Param("pidx") String pidx);
}
