package com.example.finance.repository;

import com.example.finance.model.Debt;
import com.example.finance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface DebtRepository extends JpaRepository<Debt, Integer> {
    List<Debt> findByUserOrderByDueDateAsc(User user);
    List<Debt> findByIsReturned(boolean isReturned);
    List<Debt> findByIsLent(boolean isLent);
    List<Debt> findByUserAndContactNameAndIsLent(User user, String contactName, boolean isLent);
}
