package mil.af.rl.util;

import java.util.Comparator;

/**
 * This class is just a helper for determining the nearest instances to a given
 * instance.
 * @author sloscal1
 *
 */
public class DoubleIndexPair<T extends Comparable<T>, T2 extends Comparable<T2>>  implements Comparator<DoubleIndexPair<T, T2>>, Comparable<DoubleIndexPair<T, T2>>{
	/** The e1 of the considered instance */
	private T e1;
	/** The e2 from the target instance to instance (e1) */
	private T2 e2;
	
	/**
	 * Do not create empty Pair objects.
	 */
	public DoubleIndexPair(){};
	
	/**
	 * This constructor makes a new Pair object to the given
	 * parameters
	 * @param e1 the instance e1 to store
	 * @param e2 the e2 from instance(e1) to r
	 */
	public DoubleIndexPair(T index, T2 distance){
		this.e1 = index;
		this.e2 = distance;
	}

	public DoubleIndexPair(DoubleIndexPair<T, T2> other){
		this.e1 = other.e1;
		this.e2 = other.e2;
	}
	
	public T getElement1(){
		return e1;
	}
	
	public T2 getElement2(){
		return e2;
	}
	
	@Override
	public int hashCode() {
		return e1.hashCode();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other){
		return e1 == (T)((DoubleIndexPair<T, T2>)other).e1;
	}
	
	/**
	 * Compares 2 Pair objects.  If e2 of arg0 is less than
	 * e2 of arg1 then the method will return -1; 0 if equal, and
	 * 1 otherwise.
	 */
	public int compare(DoubleIndexPair<T, T2> arg0, DoubleIndexPair<T, T2> arg1) {
		if(arg0.e2.compareTo(arg1.e2) < 0)
			return -1;
		else if(arg0.e2.compareTo(arg1.e2) > 0)
			return 1;
		return 0;
	}

	public int compareTo(DoubleIndexPair<T, T2> arg0) {
		if(this.e2.compareTo(arg0.e2) < 0)
			return -1;
		else if(this.e2.compareTo(arg0.e2) > 0)
			return 1;
		else if(this.e1.compareTo(arg0.e1) < 0)
			return -1;
		else if(this.e1.compareTo(arg0.e1) > 0)
			return 1;
		return 0;
	}
	
	public String toString(){
		return "e1: "+e1+", e2: "+e2;
	}

	public void setElement2(T2 newValue) {
		this.e2 = newValue;		
	}
	
	public void setElement1(T newValue) {
		this.e1 = newValue;		
	}
	
	public static class FirstElementComparator <T extends Comparable<T>, T2 extends Comparable<T2>> implements Comparator<DoubleIndexPair<T, T2>>{
		@Override
		public int compare(DoubleIndexPair<T, T2> o1, DoubleIndexPair<T, T2> o2) {
			return o1.e1.compareTo(o2.e1);
		}
	}
}
