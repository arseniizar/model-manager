package org.example.backend.repository;

import org.example.backend.entity.SimulationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulationResultRepository extends JpaRepository<SimulationResult, Long> {
    List<SimulationResult> findBySimulationRunId(Long runId);
}
