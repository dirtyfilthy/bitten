package net.dirtyfilthy.Bitten;

import com.google.bitcoin.core.SqlTransactionOutput;

public class TransactionNode {
	public final static int TRANSACTION=1;
	public final static int OUTPUT=2;
	public int type;
	public long id;
	
	TransactionNode(SqlTransactionOutput s){
		type=OUTPUT;
		id=s.id;
	}
	
	TransactionNode(long transaction_id){
		type=TRANSACTION;
		id=transaction_id;
	}
	
	
	
	public boolean equals(Object rhs){
		return (rhs==this || ((rhs instanceof TransactionNode) && ((TransactionNode) rhs).type==this.type && ((TransactionNode) rhs).id==this.id));
	}
	
	public int hashCode(){
		return (int) (25*this.type+99*this.id);
	}
	
	
	
	
	
}
