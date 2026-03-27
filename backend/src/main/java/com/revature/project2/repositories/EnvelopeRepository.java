package com.revature.project2.repositories;

import com.revature.project2.models.Envelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvelopeRepository extends JpaRepository<Envelope, Integer> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Envelope> findById(Integer id);

    @EntityGraph(attributePaths = {"user"})
    List<Envelope> findByUser_UserId(Integer userId);

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Envelope> findAll();

    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<Envelope> findAll(Pageable pageable);
}
