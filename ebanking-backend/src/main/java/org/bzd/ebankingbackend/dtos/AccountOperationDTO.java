package org.bzd.ebankingbackend.dtos;

import lombok.Data;
import org.bzd.ebankingbackend.entities.BankAccount;
import org.bzd.ebankingbackend.enums.OperationType;

import java.util.Date;

@Data
public class AccountOperationDTO {

    private Long id;
    private Date operationDate;
    private double amount;
    private OperationType type;
    private String description;
}
