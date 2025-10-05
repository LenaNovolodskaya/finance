package com.example.finance.controller;

import com.example.finance.model.Category;
import com.example.finance.model.CategoryType;
import com.example.finance.model.FinancialGoal;
import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.model.User;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.FinancialGoalRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.service.CurrentUserService;
import com.example.finance.service.FinancialGoalService;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/goal")
public class FinancialGoalController {
    private final FinancialGoalRepository financialGoalRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialGoalService financialGoalService;
    private final CurrentUserService currentUserService;

    public FinancialGoalController(FinancialGoalRepository financialGoalRepository,
                                   CategoryRepository categoryRepository,
                                   TransactionRepository transactionRepository,
                                   FinancialGoalService financialGoalService,
                                   CurrentUserService currentUserService) {
        
        this.financialGoalRepository = financialGoalRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.financialGoalService = financialGoalService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/goalList")
    public String listGoals(Model model) {
        User currentUser = currentUserService.getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUserOrderByDateDesc(currentUser);
        List<FinancialGoal> goals = financialGoalRepository.findByUserOrderByTargetDateAsc(currentUser);
        
        BigDecimal incomeSum = transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenseSum = transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = incomeSum.subtract(expenseSum);

        Map<Integer, Double> progressMap = new HashMap<>();
        Map<Integer, Boolean> overdueMap = new HashMap<>();
        LocalDate today = LocalDate.now();

        goals.forEach(goal -> {
            double progress = financialGoalService.calculateProgress(goal);
            progressMap.put(goal.getId(), progress);
            boolean isOverdue = !goal.isCompleted() && goal.getTargetDate().isBefore(today);
            overdueMap.put(goal.getId(), isOverdue);
        });

        model.addAttribute("goals", goals);
        model.addAttribute("progressMap", progressMap);
        model.addAttribute("overdueMap", overdueMap);
        model.addAttribute("incomeSum", incomeSum);
        model.addAttribute("expenseSum", expenseSum);
        model.addAttribute("balance", balance);
        
        return "financialGoalList";
    }

    @GetMapping("/goalAdd")
    public String createGoal(Model model) {
        User currentUser = currentUserService.getCurrentUser();
        FinancialGoal goal = new FinancialGoal();
        goal.setCurrentAmount(BigDecimal.ZERO); 
        goal.setMonthlyContribution(BigDecimal.ZERO);
        goal.setTargetDate(LocalDate.now().plusMonths(1));
        
        List<Category> availableCategories = categoryRepository.findAllSystemAndUserCategories(currentUser);

        model.addAttribute("goal", goal);
        model.addAttribute("standardCategories", availableCategories);
        
        return "financialGoalAdd";
    }
    
    @GetMapping("/goalEdit/{id}")
    public String editGoal(@PathVariable Integer id, Model model) {
        User currentUser = currentUserService.getCurrentUser();
        FinancialGoal goal = financialGoalRepository.findById(id)
                .filter(g -> g.getUser().equals(currentUser))
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID цели или доступ запрещен: " + id));

        List<Category> availableCategories = categoryRepository.findAllSystemAndUserCategories(currentUser);

        model.addAttribute("goal", goal);
        model.addAttribute("standardCategories", availableCategories);
        model.addAttribute("selectedCategoryId", goal.getCategory().getId().toString());
        model.addAttribute("selectedDate", goal.getTargetDate());
        model.addAttribute("formattedDate", goal.getTargetDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

        return "financialGoalAdd";
    }

    @PostMapping("/goalSave")
    public String saveGoal(@ModelAttribute("goal") @Valid FinancialGoal goal,
                           BindingResult result,
                           @RequestParam(name = "categoryId") String categoryId,
                           @RequestParam(required = false) String customCategoryName,
                           Model model) {

        User currentUser = currentUserService.getCurrentUser();
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("customCategoryName", customCategoryName);

        if (goal.getTargetDate() != null) {
             model.addAttribute("formattedDate", goal.getTargetDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        if (goal.getId() == null && goal.getTargetDate() != null && goal.getTargetDate().isBefore(LocalDate.now())) {
            result.rejectValue("targetDate", "date.past", "Дата цели не может быть в прошлом.");
        }

        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(BigDecimal.ZERO);
        }
        if (goal.getTargetAmount() != null && goal.getCurrentAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setCompleted(false);
        } else if (goal.getTargetAmount() != null && goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCompleted(true);
        } else {
             goal.setCompleted(false);
        }

        Category selectedCategory = null;

        if (categoryId == null || categoryId.isEmpty()) {
            result.rejectValue("category", "category.required", "Выберите категорию.");
        } else if ("NEW".equals(categoryId)) {
            if (customCategoryName == null || customCategoryName.trim().isEmpty()) {
                model.addAttribute("hasCustomCategoryError", true);
                result.rejectValue("category", "category.custom.required", "Введите название новой категории.");
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
            model.addAttribute("standardCategories", availableCategories);
            model.addAttribute("transaction", goal);
            return "financialGoalAdd";
        }

        if ("NEW".equals(categoryId) && selectedCategory == null) {
            Category newCustomCategory = new Category();
            newCustomCategory.setName(customCategoryName.trim());
            newCustomCategory.setType(CategoryType.OTHER); 
            newCustomCategory.setCustom(true);
            newCustomCategory.setUser(currentUser);
            selectedCategory = categoryRepository.save(newCustomCategory);
        }

        goal.setUser(currentUser);
        goal.setCategory(selectedCategory);
        financialGoalService.saveGoal(goal);

        return "redirect:/goal/goalList";
    }

    @GetMapping("/contributionAdd/{id}")
    public String showAddContributionForm(@PathVariable Integer id, Model model) {
        FinancialGoal goal = financialGoalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID цели: " + id));

        double progress = financialGoalService.calculateProgress(goal);
        model.addAttribute("progress", progress);

        BigDecimal remainingAmount = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        model.addAttribute("remainingAmount", remainingAmount);

        model.addAttribute("goal", goal);
        model.addAttribute("contributionAmount", BigDecimal.ZERO);
        return "financialGoalContribution";
    }

    @PostMapping("/contributionAdd/{id}")
    public String addContribution(@PathVariable Integer id,
                                  @RequestParam BigDecimal contributionAmount,
                                  Model model) {
        
        User currentUser = currentUserService.getCurrentUser();
        
        FinancialGoal goal = financialGoalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID цели: " + id));

        if (contributionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Сумма взноса должна быть больше нуля.");
            model.addAttribute("goal", goal);
            model.addAttribute("remainingAmount", goal.getTargetAmount().subtract(goal.getCurrentAmount()));
            double progress = financialGoalService.calculateProgress(goal);
            model.addAttribute("progress", progress);
            return "financialGoalContribution";
        }

        goal.setCurrentAmount(goal.getCurrentAmount().add(contributionAmount));
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCompleted(true);
        } else {
            goal.setCompleted(false);
        }
        
        financialGoalService.calculateMonthlyContribution(goal); 
        financialGoalRepository.save(goal);

        Transaction transaction = new Transaction();
        transaction.setUser(currentUser);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAmount(contributionAmount);
        transaction.setCategory(goal.getCategory());
        transaction.setDate(LocalDate.now());
        transaction.setComment("Вклад в цель: " + goal.getTitle());
        transactionRepository.save(transaction);

        return "redirect:/goal/goalList";
    }

    @GetMapping("/goalDelete/{id}")
    public String deleteGoal(@PathVariable Integer id) {
        User currentUser = currentUserService.getCurrentUser();
        
        FinancialGoal goal = financialGoalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID цели: " + id));

        List<Transaction> contributions = transactionRepository.findByUserAndCommentContaining(currentUser, "Вклад в цель: " + goal.getTitle());
        transactionRepository.deleteAll(contributions);

        financialGoalRepository.deleteById(id);

        return "redirect:/goal/goalList";
    }
}