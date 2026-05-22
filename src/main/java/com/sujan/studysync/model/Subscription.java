package com.sujan.studysync.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

// Tracks each user's Pro subscription
// Since Khalti has no auto-recurring, we track manually
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription extends BaseEntity {

    // Which user owns this subscription
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // PRO or FREE — mirrors user.plan
    @Column(nullable = false)
    @Builder.Default
    private String plan = "FREE";

    // active, expired, cancelled, pending
    @Column(nullable = false)
    @Builder.Default
    private String status = "pending";

    // Khalti's unique token for this payment
    // Used to verify payment with Khalti lookup API
    @Column(length = 100)
    private String khaltiPidx;

    // Amount paid in NPR (e.g. 100)
    private Integer amountNpr;

    // Amount in paisa — what Khalti actually uses
    // 100 NPR = 10000 paisa
    private Integer amountPaisa;

    // When the current paid period started
    private LocalDate periodStart;

    // When the current paid period ends
    // User must pay again before this date to keep Pro
    private LocalDate periodEnd;

    // Whether we've sent the renewal reminder email
    @Builder.Default
    private Boolean renewalEmailSent = false;
}

