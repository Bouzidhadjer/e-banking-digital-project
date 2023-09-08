package org.bzd.ebankingbackend.repositories;

import org.bzd.ebankingbackend.entities.BankAccount;
import org.bzd.ebankingbackend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount,String> {

}
