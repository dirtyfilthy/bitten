package net.dirtyfilthy.Bitten;


public class TransactionEdge {
	
	public Long transaction_id;
	public TransactionNode to;
	public TransactionNode from;
	
	
	
	TransactionEdge(long transaction_id, TransactionNode from, TransactionNode to){
		this.transaction_id=transaction_id;
		this.to=to;
		this.from=from;
	}
	
	public boolean equals(TransactionEdge rhs){
		return (rhs==this || ((rhs instanceof TransactionEdge) && rhs.transaction_id==this.transaction_id && this.to==rhs.to && this.from==rhs.from ) );
	}
	
	public int hashCode(){
		return (int) (99*this.transaction_id+43*to.id+12*from.id);
	}
	
	

}
