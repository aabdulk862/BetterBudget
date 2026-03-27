package com.revature.project2.models;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Entity
@Table(name = "envelopeHistories")
public class EnvelopeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int amountHistoryId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelopeId")
    private Envelope envelope;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transactionId")
    private Transaction transaction;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal envelopeAmount;

    public EnvelopeHistory() {
    }

    public EnvelopeHistory(int amountHistoryId, Envelope envelope, Transaction transaction, BigDecimal envelopeAmount) {
        this.amountHistoryId = amountHistoryId;
        this.envelope = envelope;
        this.transaction = transaction;
        this.envelopeAmount = envelopeAmount;
    }

    public int getAmountHistoryId() {
        return amountHistoryId;
    }

    public void setAmountHistoryId(int amountHistoryId) {
        this.amountHistoryId = amountHistoryId;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public BigDecimal getEnvelopeAmount() {
        return envelopeAmount;
    }

    public void setEnvelopeAmount(BigDecimal envelopeAmount) {
        this.envelopeAmount = envelopeAmount;
    }

    @Override
    public String toString() {
        return "EnvelopeHistory{" +
                "amountHistoryId=" + amountHistoryId +
                ", envelope=" + envelope +
                ", transactionId=" + transaction +
                ", envelopeAmount=" + envelopeAmount +
                '}';
    }
}
