import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ICustomer } from '../model/customer.model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  constructor(private http: HttpClient) { }
  
  public getCustomers(): Observable<Array<ICustomer>> {
    return this.http.get<Array<ICustomer>>(environment.backendHost+"/customers");
  }

   
  public searchCustomers(keyword: string): Observable<Array<ICustomer>> {
    return this.http.get<Array<ICustomer>>(environment.backendHost+"/customers/search?keyword="+keyword);
  }

  public saveCustomer(customer: ICustomer): Observable<ICustomer> {
    return this.http.post<ICustomer>(environment.backendHost+"/customers",customer);
  }

  public DeleteCustomer(id: number) {
    return this.http.delete<ICustomer>(environment.backendHost+"/customers/"+id);
  }
}
