package org.bzd.ebankingbackend.services.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bzd.ebankingbackend.dtos.*;
import org.bzd.ebankingbackend.entities.*;
import org.bzd.ebankingbackend.enums.OperationType;
import org.bzd.ebankingbackend.exceptions.BalanceNotSufficientException;
import org.bzd.ebankingbackend.exceptions.BankAccountNotFoundException;
import org.bzd.ebankingbackend.exceptions.CustomerNotFoundException;
import org.bzd.ebankingbackend.mappers.BankAccountMapperImpl;
import org.bzd.ebankingbackend.repositories.AccountOperationRepository;
import org.bzd.ebankingbackend.repositories.BankAccountRepository;
import org.bzd.ebankingbackend.repositories.CustomerRepository;
import org.bzd.ebankingbackend.services.BankAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl dtoBankAccountMapper;

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer = dtoBankAccountMapper.fromCustomerDTO(customerDTO);
        return dtoBankAccountMapper.fromCustomer(customerRepository.save(customer));
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found");
        } else {
            CurrentAccount currentAccount = new CurrentAccount();
            currentAccount.setId(UUID.randomUUID().toString());
            currentAccount.setCreatedAt(new Date());
            currentAccount.setBalance(initialBalance);
            currentAccount.setCustomer(customer);
            currentAccount.setOverDraft(overDraft);
            return dtoBankAccountMapper.fromCurrentAccount(bankAccountRepository.save(currentAccount));
        }
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found");
        } else {
            SavingAccount savingAccount = new SavingAccount();
            savingAccount.setId(UUID.randomUUID().toString());
            savingAccount.setCreatedAt(new Date());
            savingAccount.setBalance(initialBalance);
            savingAccount.setCustomer(customer);
            savingAccount.setInterestRate(interestRate);
            return dtoBankAccountMapper.fromSavingAccount(bankAccountRepository.save(savingAccount));
        }
    }

    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream().map(customer -> dtoBankAccountMapper.fromCustomer(customer)).collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
      Customer customer =   customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
      return  dtoBankAccountMapper.fromCustomer(customer);
    }
    private BankAccount getBankAccountByAccountId(String accountId) throws BankAccountNotFoundException{
        return  bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
    }
    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount = getBankAccountByAccountId(accountId);
        if(bankAccount instanceof  SavingAccount){
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return  dtoBankAccountMapper.fromSavingAccount(savingAccount);
        } else {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return  dtoBankAccountMapper.fromCurrentAccount(currentAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
            BankAccount bankAccount = getBankAccountByAccountId(accountId);
            if(bankAccount.getBalance() < amount)
                 throw  new BalanceNotSufficientException("Balance not sufficient");
            AccountOperation accountOperation = new AccountOperation();
            accountOperation.setType(OperationType.DEBIT);
            accountOperation.setAmount(amount);
            accountOperation.setDescription(description);
            accountOperation.setOperationDate(new Date());
            accountOperation.setBankAccount(bankAccount);
            accountOperationRepository.save(accountOperation);
            bankAccount.setBalance(bankAccount.getBalance()- amount);
            bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount = getBankAccountByAccountId(accountId);
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource,amount, "Transfer to"+ accountIdDestination);
        credit(accountIdDestination,amount,"Transfer from"+ accountIdSource);
    }

    @Override
    public List<BankAccountDTO> bankAccountList() {
        List<BankAccount> bankAccountList = bankAccountRepository.findAll();
        return  bankAccountList.stream().map(bankAccount -> {
             if(bankAccount instanceof  SavingAccount){
                 SavingAccount savingAccount = (SavingAccount) bankAccount;
                 return  dtoBankAccountMapper.fromSavingAccount(savingAccount);
             } else {
                 CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                 return  dtoBankAccountMapper.fromCurrentAccount(currentAccount);
             }
        }).collect(Collectors.toList());
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer = dtoBankAccountMapper.fromCustomerDTO(customerDTO);
        return dtoBankAccountMapper.fromCustomer(customerRepository.save(customer));
    }

    @Override
    public void deleteCustomer(Long customerId){
        customerRepository.deleteById(customerId);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream().map(op -> dtoBankAccountMapper.fromAccountOperation(op))
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = getBankAccountByAccountId(accountId);
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream().map(op -> dtoBankAccountMapper.fromAccountOperation(op)).collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword){
        List<Customer> customers = customerRepository.searchCustomer(keyword);
        return customers.stream().map(customer -> dtoBankAccountMapper.fromCustomer(customer)).collect(Collectors.toList());

    }

}
