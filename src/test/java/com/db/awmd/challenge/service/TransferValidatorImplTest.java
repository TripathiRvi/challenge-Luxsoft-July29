package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughBalanceException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


public class TransferValidatorImplTest {

    private TransferValidation transferValidator;

    @Before
    public void setUp() {
        transferValidator = new TransferValidationImpl();
    }

    @Test
    public void shouldThrowExceptionWhenAccountNotFound() throws Exception {
        final Account accountTo = new Account("Id000-ravi-2");
        final Transfer transfer = new Transfer("Id000-ravi-1", accountTo.getAccountId(), new BigDecimal("100.00"));

        try {
            transferValidator.validate(null, new Account("Id000-ravi-2"), transfer);
            fail("Account with AccountId Id000-ravi-1 not be found.");
        } catch (AccountNotFoundException ace) {
            assertThat(ace.getMessage()).isEqualTo("Account Id000-ravi-1 not found.");
        }
    }

    @Test
    public void validate_should_throwException_when_accountToNotFound() throws Exception {
        final Account accountFrom = new Account("Id000-ravi-1");
        final Transfer transfer = new Transfer("Id000-ravi-1", "Id000-ravi-3", new BigDecimal("100.00"));

        try {
            transferValidator.validate(accountFrom, null, transfer);
            fail("Account with Id000-ravi-3 should not be found");
        } catch (AccountNotFoundException ace) {
            assertThat(ace.getMessage()).isEqualTo("Account Id000-ravi-3 not found.");
        }
    }

    @Test
    public void validate_should_throwException_when_NotEnoughFunds() throws Exception {
        final Account accountFrom = new Account("Id-1");
        final Account accountTo = new Account("Id-2");
        final Transfer transfer = new Transfer("Id-1", "Id-2", new BigDecimal("2.00"));

        try {
            transferValidator.validate(accountFrom, accountTo, transfer);
            fail("Not enough funds");
        } catch (NotEnoughBalanceException nbe) {
            assertThat(nbe.getMessage()).isEqualTo("NInsufficient Balance in account Id-1 balance=0");
        }
    }

    @Test
    public void validate_should_throwException_when_transferBetweenSameAccount() throws Exception {
        final Account accountFrom = new Account("Id-1", new BigDecimal("20.00"));
        final Account accountTo = new Account("Id-1");
        final Transfer transfer = new Transfer("Id-1", "Id-1", new BigDecimal("2.00"));

        try {
            transferValidator.validate(accountFrom, accountTo, transfer);
            fail("Same account transfer");
        } catch (SameAccountTransferException sameAccountTransferException) {
            assertThat(sameAccountTransferException.getMessage()).isEqualTo("Transfer to self not permitted.");
        }
    }

    @Test
    public void validate_should_allowValidTransferBetweenAccounts() throws Exception {
        final Account accountFrom = new Account("Id-1", new BigDecimal("20.00"));
        final Account accountTo = new Account("Id-2");
        final Transfer transfer = new Transfer("Id-1", "Id-2", new BigDecimal("2.00"));

        transferValidator.validate(accountFrom, accountTo, transfer);
    }

}