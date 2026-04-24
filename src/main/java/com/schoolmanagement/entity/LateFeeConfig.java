package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "late_fee_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LateFeeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPerDay;

    @Column(name = "grace_period_days", nullable = false)
    private int gracePeriodDays;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
