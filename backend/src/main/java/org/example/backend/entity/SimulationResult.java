package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class SimulationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private SimulationRun simulationRun;

    private int timeStep;
    private String variableName;
    private double value;
}
