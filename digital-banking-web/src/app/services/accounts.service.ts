import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { IAccountDetaills } from '../model/account.model';

@Injectable({
  providedIn: 'root'
})
export class AccountsService {

  constructor(private http : HttpClient) { }

  public getAccount(accountId: string, page: number, size: number) : Observable<IAccountDetaills> {
    return this.http.get<IAccountDetaills>(environment.backendHost+"/accounts/"+accountId+"/pageOperations?page="+page+"&size="+size);
  } 

  public debit(accountId: string, amount: number, description: string) {
    let data = {accountId: accountId, amount: amount,description: description };
    return this.http.post(environment.backendHost+"/accounts/debit",data);
  } 

  public credit(accountId: string, amount: number, description: string) {
    let data = {accountId: accountId, amount: amount,description: description };
    return this.http.post(environment.backendHost+"/accounts/credit",data);
  } 
  public transfer(accountIdSource: string,accountIdDestination: string ,amount: number, description: string) {
    let data = {accountIdSource, accountIdDestination, amount, description };
    return this.http.post(environment.backendHost+"/accounts/transfer",data);
  } 
}
