package com.example.finance.repository;

import com.example.finance.model.Category;
import com.example.finance.model.CategoryType;
import com.example.finance.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);
    List<Category> findByIsCustom(boolean isCustom);
    Optional<Category> findByName(String name);
    Optional<Category> findByNameAndUser(String name, User user);
    Optional<Category> findByNameAndUserIsNull(String name);
    Optional<Category> findByNameAndTypeAndUserIsNull(String name, CategoryType type);
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user")
    List<Category> findAllSystemAndUserCategories(@Param("user") User user);
    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user = :user) AND c.type = :type")
    List<Category> findAllSystemAndUserCategoriesByType(@Param("user") User user, @Param("type") CategoryType type);
    @Query("SELECT c FROM Category c WHERE c.user = :user AND c.isCustom = true AND c.type = :type")
    List<Category> findCustomCategoriesByUserAndType(@Param("user") User user, @Param("type") CategoryType type);
    @Query("SELECT c FROM Category c WHERE c.user IS NULL AND c.isCustom = false")
    List<Category> findSystemCategories();
    List<Category> findByType(CategoryType type);
    List<Category> findByTypeAndIsCustom(CategoryType type, boolean isCustom);
}
