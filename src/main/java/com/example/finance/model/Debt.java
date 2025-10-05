package com.example.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "debt")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "contact_name", nullable = false)
    @NotBlank(message = "Введите имя контакта")
    private String contactName;
    
    @Column(name = "amount", nullable = false)
    @NotNull(message = "Введите сумму")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;
    
    @Column(name = "lent", nullable = false)
    @NotNull(message = "Укажите тип долга")
    private Boolean isLent;
    
    @Column(name = "due_date", nullable = false)
    @NotNull(message = "Введите дату возврата")
    private LocalDate dueDate;
    
    @Column(name = "returned", nullable = false)
    private Boolean isReturned = false;
    
    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount;
    
    @Column(name = "comment")
    private String comment;
    
    @PrePersist
    @PreUpdate
    public void initializeRemainingAmount() {
        if (remainingAmount == null) {
            remainingAmount = amount;
        }
    }
}
