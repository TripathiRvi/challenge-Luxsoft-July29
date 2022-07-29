package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountUpdate;
import com.db.awmd.challenge.exception.DuplicateAccountException;

import java.util.List;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountException;

  Account getAccount(String accountId);

  void clearAccounts();

  boolean updateAccounts(List<AccountUpdate> accountUpdates);

}
