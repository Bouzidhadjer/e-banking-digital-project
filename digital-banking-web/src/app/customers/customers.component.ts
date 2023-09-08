import { Component, OnInit } from '@angular/core';
import { CustomerService } from '../services/customer.service';
import { Observable, catchError, map, throwError, throwIfEmpty } from 'rxjs';
import { ICustomer } from '../model/customer.model';
import { FormGroup , FormBuilder} from '@angular/forms'
import { Router } from '@angular/router';
@Component({
  selector: 'app-customers',
  templateUrl: './customers.component.html',
  styleUrls: ['./customers.component.css']
})
export class CustomersComponent  implements OnInit{
   customers$! : Observable<Array<ICustomer>>;
   errorMessage!: string;
  searchFormGroup: FormGroup | undefined;
  constructor(private customerService: CustomerService,
    private fb : FormBuilder,
    private router: Router) {

  }

  ngOnInit(): void {
      // this.customerService.getCustomers().subscribe({
      //   next: (data) => {
      //      this.customers = data;
      //   } ,
      //   error: (err) => {
      //    this.errorMessage= err.message;
      //   }
      // });
      this.searchFormGroup = this.fb.group({
        keyword: this.fb.control(""),
      });
      this.handleSearchCustomers();

}

handleSearchCustomers() {
  let kw = this.searchFormGroup?.value.keyword;
  this.customers$ = this.customerService.searchCustomers(kw).pipe(catchError (err => {
    this.errorMessage = err.message;
    return throwError(() => err);
  }));
}
handleDeleteCustomer(customer: ICustomer) {
  let conf = confirm("Are you sure?");
  if(!conf) return;
     this.customerService.DeleteCustomer(customer.id).subscribe(
      {
        next: (Response) => {
         this.customers$ = this.customers$.pipe(
          map(data => {
            let index = data.indexOf(customer);
            data.slice(index, 1);
            return data;
          })
         );
        },
        error: (err) => {
          console.log(err);
        }});
}

handleCustomerAccounts(customer: ICustomer) {
   this.router.navigateByUrl("/customer-accounts/"+customer.id, { state: customer});
}
}
