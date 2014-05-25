package mil.af.rl.util;
public class Incrementer implements Cloneable{
	private int value;

	public Incrementer(int value){
		this.value = value;
	}

	public int preInc(){
		return ++value;
	}
	
	public int postInc(){
		++value;
		return value-1;
	}
	
	public int preDec(){
		return --value;
	}
	
	public int postDec(){
		--value;
		return value+1;
	}
	
	public void setValue(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Incrementer && ((Incrementer)obj).value == value;
	}
	
	@Override
	public int hashCode() {
		return value;
	}
	
	@Override
	public Incrementer clone() {
		Incrementer retVal = null;
		try{
			retVal = (Incrementer) super.clone();
		}catch(CloneNotSupportedException e){
			//Not going to happen.
		}
		return retVal;
	}
	
	@Override
	public String toString() {
		return ""+value;
	}
}