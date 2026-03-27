package com.revature.project2.repositories;

import com.revature.project2.models.EnvelopeHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvelopeHistoryRepository extends JpaRepository<EnvelopeHistory, Integer> {

    @EntityGraph(attributePaths = {"envelope", "transaction"})
    List<EnvelopeHistory> findByEnvelope_EnvelopeId(Integer envelopeId);
}
