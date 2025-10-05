package com.example.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "goal")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "title", nullable = false)
    @NotBlank(message = "Введите название цели")
    private String title;
    
    @Column(name = "target_amount", nullable = false)
    @NotNull(message = "Введите целевую сумму")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal targetAmount = BigDecimal.ZERO;
    
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(name = "target_date", nullable = false)
    @NotNull(message = "Введите дату")
    private LocalDate targetDate;
    
    @Column(name = "current_amount", nullable = false)
    @NotNull
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @Column(name = "monthly_contribution", nullable = false)
    private BigDecimal monthlyContribution = BigDecimal.ZERO;
    
    @Column(name = "comment")
    private String comment;
    
    @Column(name = "completed", nullable = false)
    private boolean isCompleted = false;

}
