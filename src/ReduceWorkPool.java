import java.util.HashMap;
import java.util.LinkedList;

//
//Created by Alexandru Antonica on 11/24/15.
//Copyright © 2015 Alexandru Antonica. All rights reserved.
//

public class ReduceWorkPool {
	int nThreads; // nr total de thread-uri worker
	int nWaiting = 0; // nr de thread-uri worker care sunt blocate asteptand un task
	public boolean ready = false; // daca s-a terminat complet rezolvarea problemei 
	
	LinkedList<ReducePartialSolution> tasks = new LinkedList<ReducePartialSolution>();
	HashMap<String, HashMap<Integer,Integer>> reducedValsFile = new HashMap<>();
	static HashMap<String, String> fileRank = new HashMap<>();
	/**
	 * Constructor pentru clasa WorkPool.
	 * @param nThreads - numarul de thread-uri worker
	 */
	public ReduceWorkPool(int nThreads) {
		this.nThreads = nThreads;
	}

	public static synchronized void addEntry(String key , String val){
		
		fileRank.put(key, val);
		
	}
	
	/**
	 * Functie care incearca obtinera unui task din workpool.
	 * Daca nu sunt task-uri disponibile, functia se blocheaza pana cand 
	 * poate fi furnizat un task sau pana cand rezolvarea problemei este complet
	 * terminata
	 * @return Un task de rezolvat, sau null daca rezolvarea problemei s-a terminat 
	 */
	public synchronized ReducePartialSolution getWork() {
		if (tasks.size() == 0) { // workpool gol
			nWaiting++;
			/* condtitie de terminare:
			 * nu mai exista nici un task in workpool si nici un worker nu e activ 
			 */
			if (nWaiting == nThreads) {
				ready = true;
				/* problema s-a terminat, anunt toti ceilalti workeri */
				notifyAll();
				return null;
			} else {
				while (!ready && tasks.size() == 0) {
					try {
						this.wait();
					} catch(Exception e) {e.printStackTrace();}
				}
				
				if (ready){
					//System.out.println(ReduceWorkPool.fileRank);
					/* s-a terminat prelucrarea */
				    return null;
				}
				nWaiting--;
			}
		}
		return tasks.remove();
	}


	/**
	 * Functie care introduce un task in workpool.
	 * @param sp - task-ul care trebuie introdus 
	 */
	synchronized void putWork(ReducePartialSolution sp) {
		tasks.add(sp);
		/* anuntam unul dintre workerii care asteptau */
		this.notify();

	}
}


