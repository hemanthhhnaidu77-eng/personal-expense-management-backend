package com.faraz.expensemanagement.service;

import com.faraz.expensemanagement.dto.Dtos;
import com.faraz.expensemanagement.entity.*;
import com.faraz.expensemanagement.repo.*;
import com.faraz.expensemanagement.security.Jwt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AppService {
    public final UserRepo users;
    final TxRepo tx;
    final CatRepo cats;
    final BudgetRepo budgets;
    final BCryptPasswordEncoder enc;
    final Jwt jwt;

    AppService(UserRepo u, TxRepo t, CatRepo c, BudgetRepo b, BCryptPasswordEncoder e, Jwt j) {
        users = u;
        tx = t;
        cats = c;
        budgets = b;
        enc = e;
        jwt = j;
    }

    User me() {
        return users.findByEmailIgnoreCase(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Dtos.User dto(User u) {
        return new Dtos.User(u.id, u.fullName, u.email);
    }

    public Dtos.Auth register(Dtos.Register r) {
        if (r == null || r.fullName() == null || r.email() == null || r.password() == null
                || !r.password().equals(r.confirmPassword())) {
            throw new IllegalArgumentException("All fields are required and passwords must match");
        }
        if (users.existsByEmailIgnoreCase(r.email().trim())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        User u = new User();
        u.fullName = r.fullName().trim();
        u.email = r.email().trim().toLowerCase();
        u.password = enc.encode(r.password());
        return new Dtos.Auth(null, dto(users.save(u)));
    }

    public Dtos.Auth login(Dtos.Login r) {
        if (r == null || r.email() == null || r.password() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        User u = users.findByEmailIgnoreCase(r.email().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!enc.matches(r.password(), u.password)) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return new Dtos.Auth(jwt.make(u.email), dto(u));
    }

    public Map<String, Object> summary() {
        User u = me();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        BigDecimal i = tx.sum(u, Transaction.Type.INCOME, startOfMonth, endOfMonth);
        if (i == null) i = BigDecimal.ZERO;
        BigDecimal e = tx.sum(u, Transaction.Type.EXPENSE, startOfMonth, endOfMonth);
        if (e == null) e = BigDecimal.ZERO;
        BigDecimal b = i.subtract(e);
        return Map.of("totalIncome", i, "totalExpenses", e, "currentBalance", b, "monthlySavings", b);
    }

    public List<Transaction> recent() {
        return tx.findTop10ByUserOrderByTransactionDateDesc(me());
    }

    public List<Transaction> all() {
        return tx.findByUserOrderByTransactionDateDesc(me());
    }

    public Transaction save(Dtos.Tx r, Long id) {
        User u = me();
        Transaction t = (id == null) ? new Transaction() : tx.findByIdAndUser(id, u)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        t.user = u;
        t.title = r.title();
        t.description = r.description();
        t.amount = r.amount();
        t.type = (r.type() != null) ? Transaction.Type.valueOf(r.type().toUpperCase()) : Transaction.Type.EXPENSE;
        t.transactionDate = (r.transactionDate() != null) ? r.transactionDate() : LocalDate.now();
        t.paymentMethod = r.paymentMethod();
        t.category = (r.categoryId() == null) ? null : cats.findByIdAndUser(r.categoryId(), u).orElse(null);
        return tx.save(t);
    }

    public void delete(Long id) {
        tx.delete(tx.findByIdAndUser(id, me()).orElseThrow(() -> new IllegalArgumentException("Transaction not found")));
    }

    public List<Category> categories() {
        return cats.findByUser(me());
    }

    public Category saveCat(Dtos.Category r) {
        Category c = new Category();
        c.user = me();
        c.name = r.name();
        c.type = (r.type() != null) ? Transaction.Type.valueOf(r.type().toUpperCase()) : Transaction.Type.EXPENSE;
        return cats.save(c);
    }

    public List<Budget> budgetList() {
        return budgets.findByUser(me());
    }

    public Budget saveBudget(Dtos.Budget r) {
        Budget b = new Budget();
        b.user = me();
        b.monthlyLimit = r.monthlyLimit();
        b.month = r.month();
        b.year = r.year();
        b.category = (r.categoryId() == null) ? null : cats.findByIdAndUser(r.categoryId(), me()).orElse(null);
        return budgets.save(b);
    }
}