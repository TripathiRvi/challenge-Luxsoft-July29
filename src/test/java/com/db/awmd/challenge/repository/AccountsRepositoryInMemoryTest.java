package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountUpdate;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountsRepositoryInMemoryTest {

    private AccountsRepository accountsRepository;

    @Before
    public void setUp(){
        accountsRepository = new AccountsRepositoryInMemory();
    }

    @Test
    public void updateAccountsBatch_should_updateAllAccounts() throws Exception {

        accountsRepository.createAccount(new Account("Id000-ravi-1", BigDecimal.ZERO));
        accountsRepository.createAccount(new Account("Id000-ravi-2", new BigDecimal("500.00")));

        List<AccountUpdate> accountUpdates = Arrays.asList(
                new AccountUpdate("Id000-ravi-1", BigDecimal.ZERO),
                new AccountUpdate("Id000-ravi-2", new BigDecimal("-100"))
        );

        accountsRepository.updateAccounts(accountUpdates);
        assertBalance("Id000-ravi-1", BigDecimal.ZERO);
        assertBalance("Id000-ravi-2", new BigDecimal("400.00"));
    }

    private void assertBalance(String accountId, BigDecimal balance){
        assertThat(accountsRepository.getAccount(accountId).getBalance()).isEqualTo(balance);
    }

}