import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AccountsService } from '../services/accounts.service';
import { Observable, catchError, throwError } from 'rxjs';
import { IAccountDetaills } from '../model/account.model';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-accounts',
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.css']
})
export class AccountsComponent  implements OnInit{
  
  accountFormGroup! : FormGroup;
  currentPage: number = 0;
  pageSize: number = 5
  account$!: Observable<IAccountDetaills>;
  operationFormGroup!: FormGroup;
  errorMessage!: string;
  constructor(private fb: FormBuilder,
    private accountsService : AccountsService,
    protected authService: AuthService){

  }

  ngOnInit(): void {
     this.accountFormGroup = this.fb.group({
        accountId: this.fb.control('')
     });
     this.operationFormGroup = this.fb.group({
      operationType: this.fb.control(null),
      amount: this.fb.control(0),
      description: this.fb.control(null),
      accountDestination: this.fb.control(null)
   });
  }
  handleSearchAccount(){
    let accountId: string = this.accountFormGroup.value.accountId;
      this.account$ = this.accountsService.getAccount(accountId,this.currentPage,this.pageSize).pipe(
        catchError(err => {
          this.errorMessage = err.message;
           return throwError (() => err);
        }
      ) );
  }
  gotoPage(page: number) {
    this.currentPage = page;
    this.handleSearchAccount();
  }
  handleAccountOperation(){
    let accountId: string = this.accountFormGroup.value.accountId;
    let operationType : string = this.operationFormGroup.value.operationType;
    let amount: number = this.operationFormGroup.value.amount;
    let description: string = this.operationFormGroup.value.description;
    if(operationType == 'DEBIT') {
        this.accountsService.debit(accountId,amount,description).subscribe({
        next: () => {  alert("Success  Debit");  this.handleSearchAccount(); 
        this.operationFormGroup.reset();},
        error: (err) => { console.log(err);}
        });
    } else if(operationType == 'CREDIT') {
      this.accountsService.credit(accountId,amount,description).subscribe({
        next: () => {  alert("Success  Credit"); this.handleSearchAccount(); 
        this.operationFormGroup.reset();},
        error: (err) => { console.log(err);}
        });

    } else if(operationType == 'TRANSFER') {
      let accountDestination: string = this.operationFormGroup.value.accountDestination;
      this.accountsService.transfer(accountId,accountDestination,amount,description).subscribe({
        next: () => {  alert("Success  Transfer");  this.handleSearchAccount();
        this.operationFormGroup.reset(); },
        error: (err) => { console.log(err);}
        });
    }

  }
}
