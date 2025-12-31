package org.example.backend.repository;

import org.example.backend.entity.SavedResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedResultRepository extends JpaRepository<SavedResult, Long> {
}
