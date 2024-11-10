package com.example.e_commerce.history.repository;

import com.example.e_commerce.history.domain.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {
}
