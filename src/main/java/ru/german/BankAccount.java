package ru.german;

public class BankAccount {
    private Integer id;
    private Integer balance = 0;

    public BankAccount(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Integer getBalance() {
        return balance;
    }

    public void deposit(Integer balance) {
        this.balance += balance;
    }

    public void withdraw(Integer sum) {
        if (balance >= sum) {
            this.balance = balance - sum;
        } else {
            throw new IllegalArgumentException("Недостаточно средств на счёте " + getId());
        }
    }
}
