package com.example.finance.repository;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByUserOrderByDateDesc(User user);
    List<Transaction> findByUserAndCommentContaining(User user, String comment);
    @Modifying
    @Transactional
    void deleteByUserAndCommentContaining(User user, String comment);
    
    List<Transaction> findByUserOrderByDateAsc(User user);
    List<Transaction> findByUserAndDateBetweenOrderByDateAsc(User user, LocalDate startDate, LocalDate endDate);
    List<Transaction> findByUserAndDateAfterOrderByDateAsc(User user, LocalDate date);
    List<Transaction> findByUserAndDateBeforeOrderByDateAsc(User user, LocalDate date);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "ORDER BY t.date DESC")
    List<Transaction> findAllByUserAndFilters(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("type") TransactionType type,
        @Param("categoryId") Integer categoryId);
}
