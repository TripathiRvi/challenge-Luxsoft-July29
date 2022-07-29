package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;

import com.db.awmd.challenge.exception.DuplicateAccountException;
import com.db.awmd.challenge.exception.NotEnoughBalanceException;
import com.db.awmd.challenge.exception.SameAccountTransferException;
import com.db.awmd.challenge.exception.AccountNotFoundException;

import com.db.awmd.challenge.service.AccountsService;

import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Account will be created... {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountException duplicateAccountException) {
            return new ResponseEntity<>(duplicateAccountException.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account with id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }

    @PutMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> doTransfer(@RequestBody @Valid Transfer transfer) {
        log.info("Making transfer {}", transfer);

        try {
            this.accountsService.makeTransfer(transfer);
        } catch (AccountNotFoundException ane) {
            return new ResponseEntity<>(ane.getMessage(), HttpStatus.NOT_FOUND);
        } catch (NotEnoughBalanceException nbe) {
            return new ResponseEntity<>(nbe.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (SameAccountTransferException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }


}
