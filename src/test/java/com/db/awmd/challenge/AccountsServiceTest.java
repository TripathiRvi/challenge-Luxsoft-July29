package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.DuplicateAccountException;
import com.db.awmd.challenge.exception.NotEnoughBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private NotificationService notificationService;

    @Test
    public void addAccount() throws Exception {
        Account account = new Account("Id000-ravi-1");
        account.setBalance(new BigDecimal(50000));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id000-ravi-1")).isEqualTo(account);
    }

    @Test
    public void addAccountShouldFailOnDuplicateId() throws Exception {
        String uniqueId = "Id000-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Adding duplicate account not allowed");
        } catch (DuplicateAccountException ex) {
            assertThat(ex.getMessage()).isEqualTo("Provided Account Id " + uniqueId + " is already exists into system.");
        }

    }

    @Test
    public void transferShouldFailWhenAccountNotExist() {
        final String accountFromId = UUID.randomUUID().toString();
        final String accountToId = UUID.randomUUID().toString();
        this.accountsService.createAccount(new Account(accountFromId));
        Transfer transfer = new Transfer(accountFromId, accountToId, new BigDecimal(5000));
        try {
            this.accountsService.makeTransfer(transfer);
            fail("Transfer should fail if account not exist.");
        } catch (AccountNotFoundException accountNotFoundException) {
            assertThat(accountNotFoundException.getMessage()).isEqualTo("Account " + accountToId + " not found.");
        }
        verifyZeroInteractions(notificationService);
    }

    @Test
    public void makeTransfer_should_fail_when_accountNotEnoughFunds() {
        final String accountFromId = UUID.randomUUID().toString();
        final String accountToId = UUID.randomUUID().toString();
        this.accountsService.createAccount(new Account(accountFromId));
        this.accountsService.createAccount(new Account(accountToId));
        Transfer transfer = new Transfer(accountFromId, accountToId, new BigDecimal(5000));
        try {
            this.accountsService.makeTransfer(transfer);
            fail("Insufficient Balance in Account");
        } catch (NotEnoughBalanceException notEnoughBalanceException) {
            assertThat(notEnoughBalanceException.getMessage()).isEqualTo("Insufficient Balance in Account " + accountFromId + " balance=0");
        }
        verifyZeroInteractions(notificationService);
    }

    @Test
    public void makeTransferOfFunds() {
        final String accountFromId = UUID.randomUUID().toString();
        final String accountToId = UUID.randomUUID().toString();
        final Account accountFrom = new Account(accountFromId, new BigDecimal("1000.00"));
        final Account accountTo = new Account(accountToId, new BigDecimal("500.00"));

        this.accountsService.createAccount(accountFrom);
        this.accountsService.createAccount(accountTo);

        Transfer transfer = new Transfer(accountFromId, accountToId, new BigDecimal("1000.00"));

        this.accountsService.makeTransfer(transfer);

        assertThat(this.accountsService.getAccount(accountFromId).getBalance()).isEqualTo(new BigDecimal("4000.00"));
        assertThat(this.accountsService.getAccount(accountToId).getBalance()).isEqualTo(new BigDecimal("3000.00"));

        verifyNotifications(accountFrom, accountTo, transfer);
    }

    @Test
    public void makeTransfer_should_transferFunds_when_balanceJustEnough() {

        final String accountFromId = UUID.randomUUID().toString();
        final String accountToId = UUID.randomUUID().toString();
        final Account accountFrom = new Account(accountFromId, new BigDecimal("5000.00"));
        final Account accountTo = new Account(accountToId, new BigDecimal("1000.00"));

        this.accountsService.createAccount(accountFrom);
        this.accountsService.createAccount(accountTo);

        Transfer transfer = new Transfer(accountFromId, accountToId, new BigDecimal("1000.00"));

        this.accountsService.makeTransfer(transfer);

        assertThat(this.accountsService.getAccount(accountFromId).getBalance()).isEqualTo(new BigDecimal("0.00"));
        assertThat(this.accountsService.getAccount(accountToId).getBalance()).isEqualTo(new BigDecimal("1000.00"));

        verifyNotifications(accountFrom, accountTo, transfer);
    }

    private void verifyNotifications(final Account accountFrom, final Account accountTo, final Transfer transfer) {
        verify(notificationService, Mockito.times(1)).notifyAboutTransfer(accountFrom, "Transfer for AccountId " + accountTo.getAccountId() + " is done with balance of " + transfer.getBalance() + ".");
        verify(notificationService, Mockito.times(1)).notifyAboutTransfer(accountTo, "Account with AccountId " + accountFrom.getAccountId() + " is transferred with balance of " + transfer.getBalance() + " in your account.");
    }


}
