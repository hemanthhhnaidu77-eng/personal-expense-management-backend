package com.faraz.expensemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @JsonIgnore
    @ManyToOne
    public User user;

    @ManyToOne
    public Category category;

    public BigDecimal monthlyLimit;
    public Integer month;
    public Integer year;
    public Instant createdAt = Instant.now();
    public Instant updatedAt = Instant.now();
}
