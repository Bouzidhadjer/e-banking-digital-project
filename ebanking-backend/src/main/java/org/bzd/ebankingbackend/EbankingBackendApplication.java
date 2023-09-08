package org.bzd.ebankingbackend;

import org.bzd.ebankingbackend.dtos.BankAccountDTO;
import org.bzd.ebankingbackend.dtos.CurrentBankAccountDTO;
import org.bzd.ebankingbackend.dtos.CustomerDTO;
import org.bzd.ebankingbackend.dtos.SavingBankAccountDTO;
import org.bzd.ebankingbackend.entities.AccountOperation;
import org.bzd.ebankingbackend.entities.CurrentAccount;
import org.bzd.ebankingbackend.entities.Customer;
import org.bzd.ebankingbackend.entities.SavingAccount;
import org.bzd.ebankingbackend.enums.AccountStatus;
import org.bzd.ebankingbackend.enums.OperationType;
import org.bzd.ebankingbackend.exceptions.CustomerNotFoundException;
import org.bzd.ebankingbackend.repositories.AccountOperationRepository;
import org.bzd.ebankingbackend.repositories.BankAccountRepository;
import org.bzd.ebankingbackend.repositories.CustomerRepository;
import org.bzd.ebankingbackend.services.BankAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbankingBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner (BankAccountService bankService) {
		return args -> {
			Stream.of("Hassan","Imane","Mohamed").forEach(name -> {
				CustomerDTO customer = new CustomerDTO();
				customer.setName(name);
				customer.setEmail(name+"@gmail.com");
				bankService.saveCustomer(customer);
			});
			bankService.listCustomers().forEach(customer -> {
				try {
					bankService.saveCurrentBankAccount(Math.random()*9000,9000, customer.getId());
					bankService.saveSavingBankAccount(Math.random()*12000,5.5, customer.getId());
				}  catch (CustomerNotFoundException e) {
					e.printStackTrace();
				}
			});

			List<BankAccountDTO> bankAccountList = bankService.bankAccountList();
			for (BankAccountDTO bankAccount: bankAccountList) {
				for (int i = 0; i < 10; i++) {
					String accountId;
					if(bankAccount instanceof SavingBankAccountDTO) {
						accountId =  ((SavingBankAccountDTO) bankAccount).getId();
					} else {
						accountId =  ((CurrentBankAccountDTO) bankAccount).getId();
					}
					bankService.credit(accountId, 10000 + Math.random() * 120000, "Credit");
					bankService.debit(accountId, 1000 + Math.random() * 9000, "Debit");
				}
			}
		};
	}
	//@Bean
	CommandLineRunner start(CustomerRepository customerRepository,
							AccountOperationRepository accountOperationRepository,
							BankAccountRepository bankAccountRepository) {
		return args -> {
			Stream.of("Hassan","Yassine","Aicha").forEach(name -> {
				Customer customer = new Customer();
				customer.setName(name);
				customer.setEmail(name+"@gmail.com");
				customerRepository.save(customer);
			});
			customerRepository.findAll().forEach(cust -> {
				CurrentAccount currentAccount = new CurrentAccount();
				currentAccount.setId(UUID.randomUUID().toString());
				currentAccount.setBalance(Math.random()*9000);
				currentAccount.setCreatedAt(new Date());
				currentAccount.setStatus(AccountStatus.CREATED);
				currentAccount.setCustomer(cust);
				currentAccount.setOverDraft(9000);
				bankAccountRepository.save(currentAccount);
				SavingAccount savingAccount = new SavingAccount();
				savingAccount.setId(UUID.randomUUID().toString());
				savingAccount.setBalance(Math.random()*9000);
				savingAccount.setCreatedAt(new Date());
				savingAccount.setStatus(AccountStatus.CREATED);
				savingAccount.setCustomer(cust);
				savingAccount.setInterestRate(5.5);
				bankAccountRepository.save(savingAccount);
			});
			bankAccountRepository.findAll().forEach(acc -> {
				for (int i = 0; i < 5; i++) {
					AccountOperation accountOperation = new AccountOperation();
					accountOperation.setOperationDate(new Date());
					accountOperation.setAmount(Math.random() * 12000);
					accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
					accountOperation.setBankAccount(acc);
					accountOperationRepository.save(accountOperation);
				}
				});
		};
	}
}
