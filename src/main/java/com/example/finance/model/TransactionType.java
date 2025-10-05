package com.example.finance.model;


public enum TransactionType {
    INCOME("Доход"), 
    EXPENSE("Расход");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
