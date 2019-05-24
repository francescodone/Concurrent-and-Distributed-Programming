package multiset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>A MultiSet models a data structure containing elements along with their frequency count i.e., </p>
 * <p>the number of times an element is present in the set.</p>
 * <p>HashMultiSet is a Map-based concrete implementation of the MultiSet concept.</p>
 * 
 * <p>MultiSet a = <{1:2}, {2:2}, {3:4}, {10:1}></p>
 * */
public final class HashMultiSet<T, V> {

	/**
	 *XXX: data structure backing this MultiSet implementation. 
	 */
	Map<T, V> frequencyMap = null;
	
	/**
	 * Sole constructor of the class.
	 **/
	public HashMultiSet() {
		frequencyMap = new HashMap<>();
	}
	
	/**
	 * If not present, adds the element to the data structure, otherwise 
	 * simply increments its frequency.
	 * 
	 * @param t T: element to include in the multiset
	 * 
	 * @return V: frequency count of the element in the multiset
	 * */	
	public V addElement(T t) {
		if(t==null) {
			throw new UnsupportedOperationException();
		}
		Integer cont=1;
		Integer last=1;
		if(isPresent(t)==false) {
			//key not found, frequency set to 1
			frequencyMap.put(t, (V) cont);
		}else {
			//key exists, we have to increase its frequency
			last = (Integer) frequencyMap.get(t)+1;
			frequencyMap.put(t, (V) last);
		}
		return getElementFrequency(t);
	}

	/**
	 * Check whether the elements is present in the multiset.
	 * 
	 * @param t T: element
	 * 
	 * @return V: true if the element is present, false otherwise.
	 * */	
	public boolean isPresent(T t) {
		//throw new UnsupportedOperationException();
		boolean found;
		if(frequencyMap.get(t)==null) {
			//key not found
			found=false;
		}else{
			//key exists
			found=true;
		}
		return found;
	}
	
	/**
	 * @param t T: element
	 * @return V: frequency count of parameter t ('0' if not present)
	 * */
	public V getElementFrequency(T t) {
		//throw new UnsupportedOperationException();
		Integer frequency = null;
		if(isPresent(t)==false) {
			//key not found, frequency returned == 0
			frequency = 0;
		}else {
			//key exists, we have to return its frequency
			frequency = (Integer) frequencyMap.get(t);
		}
		return (V) frequency;
	}
	
	
	/**
	 * Builds a multiset from a source data file. The source data file contains
	 * a number comma separated elements. 
	 * Example_1: ab,ab,ba,ba,ac,ac -->  <{ab:2},{ba:2},{ac:2}>
	 * Example 2: 1,2,4,3,1,3,4,7 --> <{1:2},{2:1},{3:2},{4:2},{7:1}>
	 * 
	 * @param source Path: source of the multiset
	 * */
	public void buildFromFile(Path source) throws IOException {
		if(source==null) {
			throw new UnsupportedOperationException();
		}else{
			File fileInput = new File(source.toString());
			BufferedReader br = new BufferedReader(new FileReader(fileInput)); 
			String st;
			String[] splitted = null;
			V aux = null;
			while ((st = br.readLine()) != null){
				splitted = st.split(",");
				for (String i : splitted) { 
					aux = addElement((T)i);
		        } 
			}
		}
	}

	/**
	 * Same as before with the difference being the source type.
	 * @param source List<T>: source of the multiset
	 * */
	public void buildFromCollection(List<? extends T> source) {
		if(source==null) {
			throw new UnsupportedOperationException();
		}else{
			source.forEach((i) -> {
				V aux = addElement(i);
			});
		}
	}
	
	/**
	 * Produces a linearized, unordered version of the MultiSet data structure.
	 * Example: <{1:2},{2:1}, {3:3}> -> 1 1 2 3 3 3 3
	 * 
	 * @return List<T>: linearized version of the multiset represented by this object.
	 */
	public List<T> linearize() {
		//throw new UnsupportedOperationException();
		LinkedList<T> list = new LinkedList<T>();
		Integer i = null;
		for (Map.Entry<T, V> entry : frequencyMap.entrySet()) {
			T key = entry.getKey();
			for(i=(Integer)entry.getValue(); i>0; i++) {
				list.add(key);
			}
		}
		return list;
	}
	
	
}
