package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountUpdate;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughBalanceException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Getter
    private final NotificationService notificationService;

    @Autowired
    private TransferValidation transferValidator;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {

        return this.accountsRepository.getAccount(accountId);
    }


    public void makeTransfer(Transfer transfer) throws AccountNotFoundException, NotEnoughBalanceException, SameAccountTransferException {

        final Account accountFrom = accountsRepository.getAccount(transfer.getAccountFromId());
        final Account accountTo = accountsRepository.getAccount(transfer.getAccountToId());
        final BigDecimal amount = transfer.getBalance();

        if(accountFrom.getAccountId().compareTo(accountTo.getAccountId()) < 1){
            synchronized (accountFrom){
                synchronized (accountTo){
                    transferValidator.validate(accountFrom, accountTo, transfer);
                    boolean succeeded = accountsRepository.updateAccounts(Arrays.asList(
                            new AccountUpdate(accountFrom.getAccountId(), amount.negate()),
                            new AccountUpdate(accountTo.getAccountId(), amount)
                    ));
                    if (succeeded) {
                        notificationService.notifyAboutTransfer(accountFrom, "Transfer for AccountId "
                                + accountTo.getAccountId() + " is done with balance of " + transfer.getBalance());
                        notificationService.notifyAboutTransfer(accountTo, "Transfer from AccountId + "
                                + accountFrom.getAccountId() + "has done with balance of " + transfer.getBalance() + " in your account.");
                    }
                }
            }

        } else {
            synchronized (accountTo){
                synchronized (accountFrom){
                    transferValidator.validate(accountFrom, accountTo, transfer);
                    boolean succeeded = accountsRepository.updateAccounts(Arrays.asList(
                            new AccountUpdate(accountFrom.getAccountId(), amount.negate()),
                            new AccountUpdate(accountTo.getAccountId(), amount)
                    ));
                    if (succeeded) {
                        notificationService.notifyAboutTransfer(accountFrom, "Transfer for AccountId "
                                + accountTo.getAccountId() + " is done with balance of " + transfer.getBalance());
                        notificationService.notifyAboutTransfer(accountTo, "Transfer from AccountId + "
                                + accountFrom.getAccountId() + "has done with balance of " + transfer.getBalance() + " in your account.");
                    }
                }
            }
        }




    }

}
