package com.example.data_enrichment_service.repository;

import com.example.data_enrichment_service.entity.EnrichmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EnrichmentResultRepository extends JpaRepository<EnrichmentResult, Long> {
    boolean existsByMessageId(UUID messageId);
}
