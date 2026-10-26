package ru.german;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentBank {
    private final AtomicInteger accountId = new AtomicInteger(1);
    private final ConcurrentMap<Integer, BankAccount> bankBase = new ConcurrentHashMap<>();

    public BankAccount createAccount(Integer deposit) {
        BankAccount account = new BankAccount(accountId.getAndIncrement());
        account.deposit(deposit);
        bankBase.putIfAbsent(account.getId(), account);
        return bankBase.get(account.getId());
    }

    public void transfer(BankAccount a, BankAccount b, int sum) {
        if (sum <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        if (a == b) {
            return;
        }

        Object firstLock = a.getId() < b.getId() ? a : b;
        Object secondLock = a.getId() < b.getId() ? b : a;

        synchronized (firstLock) {
            synchronized (secondLock) {
                a.withdraw(sum);
                b.deposit(sum);
            }
        }
    }

    public Integer getTotalBalance() {
        int bankBalance = 0;
        if (bankBase.isEmpty()) {
            return bankBalance;
        }
        for (BankAccount accounts : bankBase.values()) {
            bankBalance += accounts.getBalance();
        }
        return bankBalance;
    }
}
