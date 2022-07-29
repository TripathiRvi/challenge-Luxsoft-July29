package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughBalanceException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferValidationImpl implements TransferValidation {

    public void validate(final Account currentAccountFrom, final Account currentAccountTo, final Transfer transfer)
            throws AccountNotFoundException, NotEnoughBalanceException, SameAccountTransferException{

        if (currentAccountFrom == null){
            throw new AccountNotFoundException("Account " + transfer.getAccountFromId() + " not found.");
        }

        if (currentAccountTo == null) {
            throw new AccountNotFoundException("Account " + transfer.getAccountToId() + " not found.");
        }

        if (sameAccount(transfer)){
            throw new SameAccountTransferException("Transfer in same account is not allowed.");
        }

        if (!enoughFunds(currentAccountFrom, transfer.getBalance())){
            throw new NotEnoughBalanceException("Not enough balance in account " + currentAccountFrom.getAccountId() + " balance="+currentAccountFrom.getBalance());
        }
    }

    private boolean sameAccount(final Transfer transfer) {
        return transfer.getAccountFromId().equals(transfer.getAccountToId());
    }


    private boolean enoughFunds(final Account account, final BigDecimal amount) {
        return account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) >= 0;
    }

}
