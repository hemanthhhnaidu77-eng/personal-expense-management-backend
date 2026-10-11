package com.faraz.expensemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @JsonIgnore
    @ManyToOne
    public User user;

    @ManyToOne
    public Category category;

    public String title;
    public String description;
    public String paymentMethod;
    public BigDecimal amount;

    @Enumerated(EnumType.STRING)
    public Type type;

    public LocalDate transactionDate;
    public Instant createdAt = Instant.now();
    public Instant updatedAt = Instant.now();

    public enum Type {
        INCOME,
        EXPENSE
    }
}
