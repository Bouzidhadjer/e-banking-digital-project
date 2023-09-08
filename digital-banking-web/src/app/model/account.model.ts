export interface IAccountDetaills {
   accountId: string;
   balance: number;
   currentPage: number;
   totalPages: number;
   pageSize: number;
   accountOperationDTOS: IAccountOperation[];

}

export interface IAccountOperation {
  
     id: number;
     operationDate: Date;
     amount: number;
     type: string;
     description: string;

}