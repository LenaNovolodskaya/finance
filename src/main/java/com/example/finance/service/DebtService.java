package com.example.finance.service;

import com.example.finance.model.Debt;
import com.example.finance.model.Category;
import com.example.finance.model.CategoryType;
import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class DebtService {
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public DebtService(TransactionRepository transactionRepository, 
                       CurrentUserService currentUserService,
                       CategoryRepository categoryRepository) {
        
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
        this.categoryRepository = categoryRepository;
    }
    
    public String getDebtStatus(Debt debt) {
        if (debt == null || debt.getDueDate() == null) {
            return "Дата не указана";
        }
        
        if (debt.getIsReturned()) {
            return "Возвращено";
        }
        
        LocalDate today = LocalDate.now();
        if (debt.getDueDate().isBefore(today)) {
            return "Просрочено";
        } else if (debt.getDueDate().isEqual(today)) {
            return "Сегодня";
        } else {
            long daysRemaining = ChronoUnit.DAYS.between(today, debt.getDueDate());
            return "Осталось " + daysRemaining + " дней";
        }
    }
    
    public BigDecimal calculateTotalDebts(List<Debt> debts, boolean isLent) {
        return debts.stream()
                .filter(d -> d.getIsLent() == isLent && !d.getIsReturned())
                .map(debt -> debt.getRemainingAmount() != null ? debt.getRemainingAmount() : debt.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Transactional
    public void createDebtTransaction(Debt debt) {
        Transaction transaction = new Transaction();
        transaction.setUser(currentUserService.getCurrentUser());
        transaction.setDate(LocalDate.now());
        transaction.setComment(getDebtTransactionComment(debt, false));
        
        if (debt.getIsLent()) {
            transaction.setType(TransactionType.EXPENSE);
        } else {
            transaction.setType(TransactionType.INCOME);
        }
        
        transaction.setAmount(debt.getAmount());
        transaction.setCategory(getDebtCategory());
        transactionRepository.save(transaction);
    }

    @Transactional
    public void processDebtRepayment(Debt debt, BigDecimal repaymentAmount) {
        if (repaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма возврата должна быть больше 0");
        }
        
        if (repaymentAmount.compareTo(debt.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Сумма возврата не может быть больше оставшегося долга");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(currentUserService.getCurrentUser());
        transaction.setDate(LocalDate.now());
        transaction.setComment(getDebtTransactionComment(debt, true));
        
        if (debt.getIsLent()) {
            transaction.setType(TransactionType.INCOME);
        } else {
            transaction.setType(TransactionType.EXPENSE);
        }
        
        transaction.setAmount(repaymentAmount);
        transaction.setCategory(getDebtCategory());
        transactionRepository.save(transaction);

        debt.setRemainingAmount(debt.getRemainingAmount().subtract(repaymentAmount));
        
        if (debt.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            debt.setIsReturned(true);
        }
    }

    private String getDebtTransactionComment(Debt debt, boolean isRepayment) {
        if (debt.getIsLent()) {
            return isRepayment ? 
                "Возврат долга от " + debt.getContactName() : 
                "Заём для " + debt.getContactName();
        } else {
            return isRepayment ? 
                "Возврат долга " + debt.getContactName() : 
                "Заём от " + debt.getContactName();
        }
    }

    private Category getDebtCategory() {
        Optional<Category> globalDebtCategory = categoryRepository.findByName("Долги").filter(c -> !c.isCustom() && c.getType() == CategoryType.OTHER);
        if (globalDebtCategory.isPresent()) {
            return globalDebtCategory.get();
        }

        Optional<Category> customDebtCategory = categoryRepository.findByName("Долги").filter(c -> c.isCustom() && c.getType() == CategoryType.OTHER);
        
        return customDebtCategory.orElseGet(() -> {
            Category newCategory = new Category();
            newCategory.setName("Долги");
            newCategory.setType(CategoryType.OTHER);
            newCategory.setCustom(true);
            return categoryRepository.save(newCategory);
        });
    }
}
