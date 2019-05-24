package pcd2018.exe2;

import pcd2018.threads.ThreadSupplier;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Classe da completare per l'esercizio 2.
 */
public class DiffieHellman {

  /**
   * Limite massimo dei valori segreti da cercare
   */
  private static final int LIMIT = 65536;

  static  long p;
  static  long g;
  //Two Linked HashMaps with ordered keyset and <key, value> = <Sa, secretA> or <Sb, secretB>
  Map<Long, Integer> Sa = new LinkedHashMap<Long, Integer>();
  Map<Long, Integer> Sb = new LinkedHashMap<Long, Integer>();

  public DiffieHellman(long p, long g) {
    this.p = p;
    this.g = g;
  }

  public Thread getThread(long publicAB, boolean ab) {
    return new Thread(() -> {
      /*Local variables for each thread:
       *  counter for cycling from 0 to LIMIT-1
       *  sw is a switch that allows the thread to use publicA instead of publicB and viceversa
       *  publicABLocal is publicA for first thread and publicB for second Thread, saved locally
       */

      // START INIT THREAD LOCAL VARIABLES
      ThreadLocal<Integer> counter = new ThreadLocal<Integer>();
      ThreadLocal<Integer> sw = new ThreadLocal<Integer>();
      ThreadLocal<Long> publicABLocal = new ThreadLocal<Long>();
      publicABLocal.set(publicAB);
      counter.set(0);
      if(!ab){
        sw.set(0);
      }else{
        sw.set(1);
      }
      // END INIT THREAD LOCAL VARIABLES

      //from 0 to LIMIT-1, put on the right map the couple S and secret
      while(counter.get()<LIMIT){
        if(sw.get().equals(1)) {
          //S calculated by B^a mod p, S calculated with secretA is stored in Sa[secretA]
          Sa.put(DiffieHellmanUtils.modPow(publicABLocal.get(), counter.get(), p), counter.get());
        }else{
          //S calculated by A^b mod p, S calculated with secretB is stored in Sb[secretB]
          Sb.put(DiffieHellmanUtils.modPow(publicABLocal.get(), counter.get(), p), counter.get());
        }
        counter.set(counter.get()+1);
      }
    });
  }


  /**
   * Metodo da completare
   * 
   * @param publicA valore di A
   * @param publicB valore di B
   * @return tutte le coppie di possibili segreti a,b
   */
  public List<Integer> crack(long publicA, long publicB) {
    List<Integer> res = new ArrayList<Integer>();
    //generate 2 thread (Intel i5 7200u - dual core: a thread for each core)
    Thread a = getThread(publicA, false); //first thread with publicA
    Thread b = getThread(publicB, true); //second thread with publicB
    //start them
    a.start();
    b.start();
    //pause the Main 'till the end of thread a and thread b
    try {
      a.join();
      b.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //Iterate over 2 maps and check the intersection between them,
    //so we'll have a list of secretA and secretB with same S
    for (Long key : Sa.keySet()) {
      if (Sb.containsKey(key)) {
        //adding couple to res
        res.add(Sa.get(key)); //adding secretA
        res.add(Sb.get(key)); //adding secretB
      }
    }
    //run in 33sec
    return res;
  }
}
