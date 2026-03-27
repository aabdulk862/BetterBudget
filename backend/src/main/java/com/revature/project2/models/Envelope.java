package com.revature.project2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Entity
@Table(name = "envelopes")
public class Envelope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int envelopeId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;
    @Column(nullable = false)
    private String envelopeDescription;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maxLimit;
    @OneToMany(mappedBy = "envelope", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Transaction> transactions;
    @OneToMany(mappedBy = "envelope", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EnvelopeHistory> envelopeHistories;

    public Envelope() {
    }

    public Envelope(int envelopeId, User user, String envelopeDescription, BigDecimal balance, BigDecimal maxLimit) {
        this.envelopeId = envelopeId;
        this.user = user;
        this.envelopeDescription = envelopeDescription;
        this.balance = balance;
        this.maxLimit = maxLimit;
    }

    @PreRemove
    private void removeFromUser(){
        if (this.user == null) return;
        this.user.getEnvelopes().remove(this);
    }

    public int getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(int envelopeId) {
        this.envelopeId = envelopeId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEnvelopeDescription() {
        return envelopeDescription;
    }

    public void setEnvelopeDescription(String envelopeDescription) {
        this.envelopeDescription = envelopeDescription;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(BigDecimal maxLimit) {
        this.maxLimit = maxLimit;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<EnvelopeHistory> getEnvelopeHistories() {
        return envelopeHistories;
    }

    public void setEnvelopeHistories(List<EnvelopeHistory> envelopeHistories) {
        this.envelopeHistories = envelopeHistories;
    }

    @Override
    public String toString() {
        return "Envelope{" +
                "envelopeId=" + envelopeId +
                ", user=" + user +
                ", envelopeDescription='" + envelopeDescription + '\'' +
                ", balance=" + balance +
                ", max=" + maxLimit +
                '}';
    }
}