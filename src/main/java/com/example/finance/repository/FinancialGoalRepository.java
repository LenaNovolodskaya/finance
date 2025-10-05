package com.example.finance.repository;

import com.example.finance.model.FinancialGoal;
import com.example.finance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Integer> {
    List<FinancialGoal> findByUserOrderByTargetDateAsc(User user);
    List<FinancialGoal> findByUserAndIsCompleted(User user, boolean isCompleted);
}