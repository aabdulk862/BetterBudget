package com.revature.project2.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String transactionDescription;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelopeId")
    private Envelope envelope;
    @Column(nullable = false)
    private LocalDateTime datetime;
    @Column(nullable = false)
    private String category;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal transactionAmount;
    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<EnvelopeHistory> envelopeHistories;

    public Transaction() {
    }

    public Transaction(int transactionId, String title, String transactionDescription, Envelope envelope, LocalDateTime datetime, String category, BigDecimal transactionAmount) {
        this.transactionId = transactionId;
        this.title = title;
        this.transactionDescription = transactionDescription;
        this.envelope = envelope;
        this.datetime = datetime;
        this.category = category;
        this.transactionAmount = transactionAmount;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public List<EnvelopeHistory> getEnvelopeHistories() {
        return envelopeHistories;
    }

    public void setEnvelopeHistories(List<EnvelopeHistory> envelopeHistories) {
        this.envelopeHistories = envelopeHistories;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", title='" + title + '\'' +
                ", transactionDescription='" + transactionDescription + '\'' +
                ", envelopeId=" + envelope +
                ", datetime=" + datetime +
                ", category='" + category + '\'' +
                ", transactionAmount=" + transactionAmount +
                '}';
    }
}
