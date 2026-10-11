package com.faraz.expensemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @JsonIgnore
    @ManyToOne
    public User user;

    public String name;

    @Enumerated(EnumType.STRING)
    public Transaction.Type type;

    public Instant createdAt = Instant.now();
}
