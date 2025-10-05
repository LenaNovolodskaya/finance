package com.example.finance.controller;

import com.example.finance.model.Debt;
import com.example.finance.model.User;
import com.example.finance.repository.DebtRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.service.CurrentUserService;
import com.example.finance.service.DebtService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/debt")
public class DebtController {
    private final DebtRepository debtRepository;
    private final DebtService debtService;
    private final CurrentUserService currentUserService;
    private final TransactionRepository transactionRepository;

    public DebtController(DebtRepository debtRepository,
                          DebtService debtService,
                          CurrentUserService currentUserService,
                          TransactionRepository transactionRepository) {
        
        this.debtRepository = debtRepository;
        this.debtService = debtService;
        this.currentUserService = currentUserService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/debtList")
    public String listDebts(Model model) {
        User currentUser = currentUserService.getCurrentUser();
        List<Debt> debts = debtRepository.findByUserOrderByDueDateAsc(currentUser);
        
        BigDecimal totalLent = debtService.calculateTotalDebts(debts, true);
        BigDecimal totalBorrowed = debtService.calculateTotalDebts(debts, false);
        
        model.addAttribute("debts", debts);
        model.addAttribute("totalLent", totalLent);
        model.addAttribute("totalBorrowed", totalBorrowed);
        model.addAttribute("debtService", debtService);
        return "debtList";
    }

    @GetMapping("/debtAdd")
    public String createDebt(Model model) {
        Debt debt = new Debt();
        debt.setIsLent(false);
        debt.setIsReturned(false);
        model.addAttribute("debt", debt);
        return "debtAdd";
    }
    
    @GetMapping("/debtEdit/{id}")
    public String editDebt(@PathVariable Integer id, Model model) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID долга: " + id));
        model.addAttribute("debt", debt);
        model.addAttribute("formattedDate", debt.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        return "debtAdd";
    }

    @PostMapping("/debtSave")
    public String saveDebt(@ModelAttribute("debt") @Valid Debt debt,
                           BindingResult result,
                           Model model) {
        
        if (result.hasErrors()) {
            return "debtAdd";
        }
        
        boolean isNewDebt = debt.getId() == null;

        if (debt.getIsLent() == null) {
            debt.setIsLent(false);
        }
        if (debt.getIsReturned() == null) {
            debt.setIsReturned(false);
        }
        
        if (isNewDebt && debt.getRemainingAmount() == null) {
            debt.setRemainingAmount(debt.getAmount());
        }

        debt.setUser(currentUserService.getCurrentUser());
        Debt savedDebt = debtRepository.save(debt);

        if (isNewDebt) {
            debtService.createDebtTransaction(savedDebt);
        }
        
        return "redirect:/debt/debtList";
    }
    
    @GetMapping("/partialRepayment/{id}")
    public String showPartialRepaymentForm(@PathVariable Integer id, Model model) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID долга: " + id));
        
        model.addAttribute("debt", debt);
        model.addAttribute("remainingAmount", debt.getRemainingAmount() != null ? debt.getRemainingAmount() : debt.getAmount());
        model.addAttribute("repaymentAmount", BigDecimal.ZERO);
        
        return "debtPartialRepayment";
    }

    @PostMapping("/partialRepayment/{id}")
    public String processPartialRepayment(@PathVariable Integer id,
                                          @RequestParam BigDecimal repaymentAmount,
                                          Model model) {
        
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID долга: " + id));
        
        if (repaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Сумма должна быть больше 0");
            model.addAttribute("debt", debt);
            model.addAttribute("remainingAmount", debt.getRemainingAmount() != null ? debt.getRemainingAmount() : debt.getAmount());
            return "debtPartialRepayment";
        }
        
        if (repaymentAmount.compareTo(debt.getRemainingAmount() != null ? debt.getRemainingAmount() : debt.getAmount()) > 0) {
            model.addAttribute("error", "Сумма не может превышать остаток долга");
            model.addAttribute("debt", debt);
            model.addAttribute("remainingAmount", debt.getRemainingAmount() != null ? debt.getRemainingAmount() : debt.getAmount());
            return "debtPartialRepayment";
        }
        
        try {
            debtService.processDebtRepayment(debt, repaymentAmount);
            debtRepository.save(debt);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("debt", debt);
            model.addAttribute("remainingAmount", debt.getRemainingAmount() != null ? debt.getRemainingAmount() : debt.getAmount());
            return "debtPartialRepayment";
        }
        
        return "redirect:/debt/debtList";
    }

    @GetMapping("/markReturned/{id}")
    public String markAsReturned(@PathVariable Integer id) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID долга: " + id));

        if (!debt.getIsReturned()) {
            BigDecimal remainingAmount = debt.getRemainingAmount() != null ? debt.getRemainingAmount() : debt.getAmount();
            if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                debtService.processDebtRepayment(debt, remainingAmount);
            }
            debt.setIsReturned(true);
            debtRepository.save(debt);
        }
        return "redirect:/debt/debtList";
    }

    @GetMapping("/debtDelete/{id}")
    public String deleteDebt(@PathVariable Integer id) {
        User currentUser = currentUserService.getCurrentUser();
        
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID долга: " + id));

        String commentSubstring;
        if (debt.getIsLent()) {
            commentSubstring = "Заём для " + debt.getContactName();
        } else {
            commentSubstring = "Заём от " + debt.getContactName();
        }
        String repaymentCommentSubstring;
        if (debt.getIsLent()) {
            repaymentCommentSubstring = "Возврат долга от " + debt.getContactName();
        } else {
            repaymentCommentSubstring = "Возврат долга " + debt.getContactName();
        }

        transactionRepository.deleteByUserAndCommentContaining(currentUser, commentSubstring);
        transactionRepository.deleteByUserAndCommentContaining(currentUser, repaymentCommentSubstring);

        debtRepository.deleteById(id);
        return "redirect:/debt/debtList";
    }
}
