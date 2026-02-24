package tasks.c_MultiAccount_Wallet.Fintech;

import java.util.*;

public abstract class BonusAccount {
    protected String accountNumber;
    protected double balance;
    protected LinkedList<String> history;

    public Account(String accountNumber, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        if (accountNumber == null) {
            throw new IllegalArgumentException("Account number cannot be null");
        }
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.history = new LinkedList<>();


    }
}
