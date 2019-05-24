package multiset;

import static org.junit.Assert.*;

import java.awt.List;
import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HashMultiSetTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void testbuildFromCollection() {
	    HashMultiSet<String, Integer> hmSet = new HashMultiSet<>();
		ArrayList<String> source = new ArrayList<String>();
		source.add("aa");
		source.add("ab");
		source.add("aa");
		source.add("aa");
		if(source==null) {
			exception.expect(IllegalArgumentException.class);
		    exception.expectMessage("Method should be invoked with a non null file path");
		}else {
			hmSet.buildFromCollection(source);
			/*
			 * result should be {aa=3, ab=1}
			 */
			System.out.println(hmSet.frequencyMap);
		}
	    
	}
	
	@Test
	public void testElementFrequency() {
	    HashMultiSet<Integer, Integer> hmSet = new HashMultiSet<>();
	    Integer aux = null;
	    aux = hmSet.addElement(1);
	    aux = hmSet.addElement(1);	    
	    assertEquals("Equal", true, hmSet.getElementFrequency(1) == 2);
	}

}
