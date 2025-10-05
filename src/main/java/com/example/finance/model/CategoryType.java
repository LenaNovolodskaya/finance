package com.example.finance.model;

public enum CategoryType {
    FOOD("Еда"),
    HEALTH ("Здоровье"),
    EDUCATION("Образование"),
    TRANSPORT("Транспорт"), 
    ENTERTAINMENT("Развлечения"), 
    TECHNIC("Техника"),
    SALARY("Зарплата"), 
    OTHER("Другое");
    
    private final String displayName;

    CategoryType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }

}
