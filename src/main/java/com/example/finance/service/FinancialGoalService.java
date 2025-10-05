package com.example.finance.service;

import com.example.finance.model.FinancialGoal;
import com.example.finance.repository.FinancialGoalRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class FinancialGoalService {
    private final FinancialGoalRepository financialGoalRepository;

    public FinancialGoalService(FinancialGoalRepository financialGoalRepository) {
        this.financialGoalRepository = financialGoalRepository;
    }

    public FinancialGoal saveGoal(FinancialGoal goal) {
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setCompleted(false);
        }

        calculateMonthlyContribution(goal);
        return financialGoalRepository.save(goal);
    }

    public void addContribution(FinancialGoal goal, BigDecimal amount) {
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCompleted(true);
        }
        financialGoalRepository.save(goal);
    }

    public void calculateMonthlyContribution(FinancialGoal goal) {
        long months = java.time.temporal.ChronoUnit.MONTHS.between(
            LocalDate.now(),
            goal.getTargetDate()
        );
        if (months <= 0) months = 1;
        goal.setMonthlyContribution(
            goal.getTargetAmount().subtract(goal.getCurrentAmount())
                .divide(BigDecimal.valueOf(months), 2, BigDecimal.ROUND_UP)
        );
    }
    
    public double calculateProgressPercentage(FinancialGoal goal) {
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return goal.getCurrentAmount()
                .divide(goal.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
                .doubleValue() * 100;
    }
    
    public double calculateProgress(FinancialGoal goal) {
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return goal.getCurrentAmount()
                .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .doubleValue() * 100;
    }
}
