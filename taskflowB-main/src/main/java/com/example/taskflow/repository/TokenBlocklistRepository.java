package com.example.taskflow.repository;

import com.example.taskflow.domain.TokenBlocklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlocklistRepository extends JpaRepository<TokenBlocklist, String> {
}
