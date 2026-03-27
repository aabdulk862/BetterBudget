package com.revature.project2.repositories;

import com.revature.project2.models.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @EntityGraph(attributePaths = {"envelope"})
    List<Transaction> findByEnvelope_EnvelopeId(Integer envelopeId);
}
