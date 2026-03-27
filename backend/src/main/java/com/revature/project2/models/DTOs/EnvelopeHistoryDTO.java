package com.revature.project2.models.DTOs;


import java.math.BigDecimal;

// Data Transfer Object (DTO) for representing the envelope history.
public class EnvelopeHistoryDTO {

    // Unique identifier for the amount history entry.
    private int amountHistoryId;

    // DTO representing the envelope associated with this history.
    private EnvelopeDTO envelopeDTO;

    // DTO representing the transaction associated with this history.
    private TransactionDTO transactionDTO;

    // Amount of money in the envelope for this history entry.
    private BigDecimal envelopeAmount;

    // Getter method for amountHistoryId.
    public int getAmountHistoryId() {
        return amountHistoryId;
    }

    // Setter method for amountHistoryId.
    public void setAmountHistoryId(int amountHistoryId) {
        this.amountHistoryId = amountHistoryId;
    }

    // Getter method for envelopeDTO.
    public EnvelopeDTO getEnvelopeDTO() {
        return envelopeDTO;
    }

    // Setter method for envelopeDTO.
    public void setEnvelopeDTO(EnvelopeDTO envelopeDTO) {
        this.envelopeDTO = envelopeDTO;
    }

    // Getter method for transactionDTO.
    public TransactionDTO getTransactionDTO() {
        return transactionDTO;
    }

    // Setter method for transactionDTO.
    public void setTransactionDTO(TransactionDTO transactionDTO) {
        this.transactionDTO = transactionDTO;
    }

    // Getter method for envelopeAmount.
    public BigDecimal getEnvelopeAmount() {
        return envelopeAmount;
    }

    // Setter method for envelopeAmount.
    public void setEnvelopeAmount(BigDecimal envelopeAmount) {
        this.envelopeAmount = envelopeAmount;
    }
}
