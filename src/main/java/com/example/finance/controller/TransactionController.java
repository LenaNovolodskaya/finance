package com.example.finance.controller;

import com.example.finance.model.Category;
import com.example.finance.model.CategoryType;
import com.example.finance.model.FinancialGoal;
import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.model.User;
import com.example.finance.model.Debt;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.FinancialGoalRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.DebtRepository;
import com.example.finance.service.CurrentUserService;
import com.example.finance.service.FinancialGoalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final FinancialGoalRepository financialGoalRepository;
    private final CurrentUserService currentUserService;
    private final DebtRepository debtRepository;
    private final FinancialGoalService financialGoalService;

    public TransactionController(TransactionRepository transactionRepository,
                                 CategoryRepository categoryRepository,
                                 FinancialGoalRepository financialGoalRepository,
                                 CurrentUserService currentUserService,
                                 DebtRepository debtRepository,
                                 FinancialGoalService financialGoalService) {
        
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.financialGoalRepository = financialGoalRepository;
        this.currentUserService = currentUserService;
        this.debtRepository = debtRepository;
        this.financialGoalService = financialGoalService;
    }

    @GetMapping("/transactionList")
    public String listTransactions(@RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = false) Integer categoryId,
                                   @RequestParam(required = false, defaultValue = "false") boolean excludeDebts,
                                   @RequestParam(required = false, defaultValue = "false") boolean excludeGoals,
                                   Model model) {

        User currentUser = currentUserService.getCurrentUser();
        
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        TransactionType transactionType = parseTransactionType(type);
        
        List<Transaction> transactionsToFilter = transactionRepository.findAllByUserAndFilters(currentUser, start, end, transactionType, categoryId);

        List<Transaction> finalFilteredTransactions;
        if (excludeDebts && excludeGoals) {
            finalFilteredTransactions = transactionsToFilter.stream()
                .filter(t -> !isDebtOrGoal(t))
                .collect(Collectors.toList());
        } else if (excludeDebts) {
            finalFilteredTransactions = transactionsToFilter.stream()
                .filter(t -> !isDebt(t))
                .collect(Collectors.toList());
        } else if (excludeGoals) {
            finalFilteredTransactions = transactionsToFilter.stream()
                .filter(t -> !isGoal(t))
                .collect(Collectors.toList());
        } else {
            finalFilteredTransactions = transactionsToFilter;
        }
    
        List<Category> categories = categoryRepository.findAllSystemAndUserCategories(currentUser);
        
        List<Transaction> transactionsForSums;
        if (start == null && end == null) {
            transactionsForSums = transactionRepository.findByUserOrderByDateDesc(currentUser);
        } else {
            transactionsForSums = transactionsToFilter;
        }

        if (excludeDebts && excludeGoals) {
            transactionsForSums = transactionsForSums.stream().filter(t -> !isDebtOrGoal(t)).collect(Collectors.toList());
        } else if (excludeDebts) {
            transactionsForSums = transactionsForSums.stream().filter(t -> !isDebt(t)).collect(Collectors.toList());
        } else if (excludeGoals) {
            transactionsForSums = transactionsForSums.stream().filter(t -> !isGoal(t)).collect(Collectors.toList());
        }

        BigDecimal incomeSum = transactionsForSums.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenseSum = transactionsForSums.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = incomeSum.subtract(expenseSum);

        model.addAttribute("transactions", finalFilteredTransactions);
        model.addAttribute("incomeSum", incomeSum);
        model.addAttribute("expenseSum", expenseSum);
        model.addAttribute("balance", balance);
        model.addAttribute("categories", categories);
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedType", transactionType);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("excludeDebts", excludeDebts);
        model.addAttribute("excludeGoals", excludeGoals);
        
        return "transactionList";
    }


    @GetMapping("/transactionAdd")
    public String createTransaction(Model model) {
        User currentUser = currentUserService.getCurrentUser();
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.now());
        transaction.setAmount(BigDecimal.ZERO);

        List<Category> availableCategories = categoryRepository.findAllSystemAndUserCategories(currentUser);

        model.addAttribute("transaction", transaction);
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("standardCategories", availableCategories);
        
        return "transactionAdd";
    }
    
    @GetMapping("/transactionEdit/{id}")
    public String editTransaction(@PathVariable Integer id, Model model) {
        User currentUser = currentUserService.getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUser().equals(currentUser))
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID операции или доступ запрещен: " + id));

        List<Category> availableCategories = categoryRepository.findAllSystemAndUserCategories(currentUser);

        model.addAttribute("transaction", transaction);
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("standardCategories", availableCategories);
        model.addAttribute("selectedCategoryId", transaction.getCategory().getId().toString());
        model.addAttribute("selectedDate", transaction.getDate());
        model.addAttribute("formattedDate", transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        return "transactionAdd";
    }

    @PostMapping("/transactionSave")
    public String saveTransaction(@ModelAttribute("transaction") @Valid Transaction transaction,
                                  BindingResult result,
                                  @RequestParam(name = "categoryId") String categoryId,
                                  @RequestParam(required = false) String customCategoryName,
                                  Model model) {
        
        User currentUser = currentUserService.getCurrentUser();
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("customCategoryName", customCategoryName);
        
        if (transaction.getDate() != null) {
            model.addAttribute("formattedDate", transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        Category selectedCategory = null;

        if (categoryId == null || categoryId.isEmpty()) {
            result.rejectValue("category", "category.required", "Выберите категорию");
        } else if ("NEW".equals(categoryId)) {
            if (customCategoryName == null || customCategoryName.trim().isEmpty()) {
                model.addAttribute("hasCustomCategoryError", true);
                result.rejectValue("category", "category.custom.required", "Введите название новой категории");
            } else {
                String trimmedCustomCategoryName = customCategoryName.trim();
                Optional<Category> existingSystemCategory = categoryRepository.findByNameAndUserIsNull(trimmedCustomCategoryName);
                Optional<Category> existingUserCategory = categoryRepository.findByNameAndUser(trimmedCustomCategoryName, currentUser);

                if (existingSystemCategory.isPresent()) {
                    result.rejectValue("category", "category.custom.exists.system", "Категория с таким именем уже существует в системе.");
                    model.addAttribute("hasCustomCategoryError", true);
                } else if (existingUserCategory.isPresent()) {
                    selectedCategory = existingUserCategory.get();
                    categoryId = selectedCategory.getId().toString();
                    model.addAttribute("selectedCategoryId", categoryId);
                }
            }
        } else {
            selectedCategory = categoryRepository.findById(Integer.parseInt(categoryId))
                .filter(cat -> cat.getUser() == null || cat.getUser().equals(currentUser))
                .orElse(null);
            if (selectedCategory == null) {
                 result.rejectValue("category", "category.invalid", "Выбрана неверная категория.");
            }
        }

        if (result.hasErrors() || model.containsAttribute("hasCustomCategoryError")) {
            List<Category> availableCategories = categoryRepository.findAllSystemAndUserCategories(currentUser);
            model.addAttribute("types", TransactionType.values());
            model.addAttribute("standardCategories", availableCategories);
            model.addAttribute("transaction", transaction);
            return "transactionAdd";
        }

        if ("NEW".equals(categoryId) && selectedCategory == null) {
            Category newCustomCategory = new Category();
            newCustomCategory.setName(customCategoryName.trim());
            newCustomCategory.setType(CategoryType.OTHER); 
            newCustomCategory.setCustom(true);
            newCustomCategory.setUser(currentUser);
            selectedCategory = categoryRepository.save(newCustomCategory);
        }

        transaction.setUser(currentUser);
        transaction.setCategory(selectedCategory);
        transactionRepository.save(transaction);
        
        return "redirect:/transaction/transactionList";
    }
    
    @GetMapping("/transactionDelete/{id}")
    public String deleteTransaction(@PathVariable Integer id) {
        User currentUser = currentUserService.getCurrentUser();
        
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUser().equals(currentUser))
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID операции: " + id));

        if (transaction.getComment() != null && transaction.getComment().startsWith("Вклад в цель: ")) {
            List<FinancialGoal> goals = financialGoalRepository.findAll();
            for (FinancialGoal goal : goals) {
                if (transaction.getComment().equals("Вклад в цель: " + goal.getTitle())) {
                    BigDecimal newAmount = goal.getCurrentAmount().subtract(transaction.getAmount());
                    goal.setCurrentAmount(newAmount);

                    if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0) {
                        goal.setCompleted(false);
                    }

                    financialGoalService.calculateMonthlyContribution(goal);
                    financialGoalRepository.save(goal);
                    break;
                }
            }
        }

        if (transaction.getComment() != null) {
            String comment = transaction.getComment();
            BigDecimal transactionAmount = transaction.getAmount();
            List<Debt> potentialDebts = null;
            boolean isLentRepayment = false;
            boolean isBorrowedRepayment = false;

            String lentRepaymentPrefix = "Возврат долга от ";
            String borrowedRepaymentPrefix = "Возврат долга ";

            if (comment.startsWith(lentRepaymentPrefix) && transaction.getType() == TransactionType.INCOME) {
                String contactName = comment.substring(lentRepaymentPrefix.length());
                potentialDebts = debtRepository.findByUserAndContactNameAndIsLent(currentUser, contactName, true);
                isLentRepayment = true;
            } else if (comment.startsWith(borrowedRepaymentPrefix) && !comment.startsWith(lentRepaymentPrefix) && transaction.getType() == TransactionType.EXPENSE) {
                String contactName = comment.substring(borrowedRepaymentPrefix.length());
                potentialDebts = debtRepository.findByUserAndContactNameAndIsLent(currentUser, contactName, false);
                isBorrowedRepayment = true;
            }

            if (potentialDebts != null && !potentialDebts.isEmpty()) {
                Debt debtToUpdate = potentialDebts.get(0);

                if (isLentRepayment || isBorrowedRepayment) {
                    BigDecimal currentRemaining = debtToUpdate.getRemainingAmount();
                    debtToUpdate.setRemainingAmount(currentRemaining.add(transactionAmount));

                    if (debtToUpdate.getIsReturned() && debtToUpdate.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0) {
                        debtToUpdate.setIsReturned(false);
                    }

                    if (debtToUpdate.getRemainingAmount().compareTo(debtToUpdate.getAmount()) > 0) {
                        debtToUpdate.setRemainingAmount(debtToUpdate.getAmount());
                    }
                    debtRepository.save(debtToUpdate);
                }
            }
        }

        transactionRepository.deleteById(id);
        
        return "redirect:/transaction/transactionList";
    }
        
    @GetMapping("/transactionAnalysis")
    public String showAnalysis(Model model,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               @RequestParam(required = false, defaultValue = "false") boolean excludeDebts,
                               @RequestParam(required = false, defaultValue = "false") boolean excludeGoals) {

        User currentUser = currentUserService.getCurrentUser();

        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);

        if (start == null) {
            start = LocalDate.now().withDayOfMonth(1);
        }
        if (end == null) {
            end = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }

        List<Transaction> transactions;
        if (start != null && end != null) {
            transactions = transactionRepository.findByUserAndDateBetweenOrderByDateAsc(currentUser, start, end);
        } else if (start != null) {
            transactions = transactionRepository.findByUserAndDateAfterOrderByDateAsc(currentUser, start.minusDays(1));
        } else if (end != null) {
            transactions = transactionRepository.findByUserAndDateBeforeOrderByDateAsc(currentUser, end.plusDays(1));
        } else {
            transactions = transactionRepository.findByUserOrderByDateAsc(currentUser);
        }

        if (excludeDebts && excludeGoals) {
            transactions = transactions.stream()
                .filter(t -> !isDebtOrGoal(t))
                .collect(Collectors.toList());
        } else if (excludeDebts) {
            transactions = transactions.stream()
                .filter(t -> !isDebt(t))
                .collect(Collectors.toList());
        } else if (excludeGoals) {
            transactions = transactions.stream()
                .filter(t -> !isGoal(t))
                .collect(Collectors.toList());
        }

        Map<String, BigDecimal> expenseByCategory = transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .collect(Collectors.groupingBy(
                t -> t.getCategory().getName(),
                Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));

        BigDecimal totalIncome = transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        Map<LocalDate, BigDecimal> dailyBalance = new TreeMap<>();
        BigDecimal runningTotalBalance;

        BigDecimal initialBalanceBeforePeriod = BigDecimal.ZERO;
        if (start != null) {
            List<Transaction> transactionsBeforeStart = transactionRepository.findByUserAndDateBeforeOrderByDateAsc(currentUser, start);
            initialBalanceBeforePeriod = transactionsBeforeStart.stream()
                .map(t -> t.getType() == TransactionType.INCOME ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        runningTotalBalance = initialBalanceBeforePeriod;

        Map<LocalDate, List<Transaction>> transactionsGroupedByDate = transactions.stream()
            .collect(Collectors.groupingBy(Transaction::getDate, TreeMap::new, Collectors.toList()));

        if (start != null && end != null) {
            LocalDate currentDateInLoop = start;
            while (!currentDateInLoop.isAfter(end)) {
                List<Transaction> txnsOnThisDay = transactionsGroupedByDate.get(currentDateInLoop);
                BigDecimal netChangeForDay = BigDecimal.ZERO;
                if (txnsOnThisDay != null) {
                    netChangeForDay = txnsOnThisDay.stream()
                        .map(t -> t.getType() == TransactionType.INCOME ? t.getAmount() : t.getAmount().negate())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                }
                runningTotalBalance = runningTotalBalance.add(netChangeForDay);
                dailyBalance.put(currentDateInLoop, runningTotalBalance.setScale(2, RoundingMode.HALF_UP));
                currentDateInLoop = currentDateInLoop.plusDays(1);
            }
        } else {
            for (Map.Entry<LocalDate, List<Transaction>> entry : transactionsGroupedByDate.entrySet()) {
                LocalDate dateOfTransactions = entry.getKey();
                List<Transaction> txnsOnThisDay = entry.getValue();

                BigDecimal netChangeForDay = txnsOnThisDay.stream()
                    .map(t -> t.getType() == TransactionType.INCOME ? t.getAmount() : t.getAmount().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                runningTotalBalance = runningTotalBalance.add(netChangeForDay);
                dailyBalance.put(dateOfTransactions, runningTotalBalance.setScale(2, RoundingMode.HALF_UP));
            }
        }

        if (dailyBalance.isEmpty()) {
            if (start != null) {
                dailyBalance.put(start, initialBalanceBeforePeriod.setScale(2, RoundingMode.HALF_UP));
            } else {
                dailyBalance.put(LocalDate.now(), initialBalanceBeforePeriod.setScale(2, RoundingMode.HALF_UP));
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        try {
            model.addAttribute("expenseByCategoryJson", objectMapper.writeValueAsString(expenseByCategory));
            model.addAttribute("dailyBalanceJson", objectMapper.writeValueAsString(
                dailyBalance.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue, (v1,v2)->v1, LinkedHashMap::new))
            ));
        } catch (JsonProcessingException e) {
            model.addAttribute("expenseByCategoryJson", "{}");
            model.addAttribute("dailyBalanceJson", "{}");
        }

        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        
        if (start != null) {
            model.addAttribute("startDate", start.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (end != null) {
            model.addAttribute("endDate", end.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        model.addAttribute("excludeDebts", excludeDebts);
        model.addAttribute("excludeGoals", excludeGoals);

        return "transactionAnalysis";
    }
    
    private LocalDate parseDate(String dateStr) {
        try {
            return dateStr != null ? LocalDate.parse(dateStr) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private TransactionType parseTransactionType(String typeStr) {
        if (typeStr == null) return null;
        try {
            return TransactionType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isDebt(Transaction transaction) {
        Category category = transaction.getCategory();
        return category != null && ("Долг".equals(category.getName()) || (category.getName() != null && category.getName().toLowerCase().contains("долг")));
    }

    private boolean isGoal(Transaction transaction) {
        if (transaction.getComment() != null && 
            (transaction.getComment().startsWith("Вклад в цель: ") || transaction.getComment().startsWith("Закрытие цели: "))) {
            return true;
        }
        return false;
    }

    private boolean isDebtOrGoal(Transaction transaction) {
        return isDebt(transaction) || isGoal(transaction);
    }
   
}

