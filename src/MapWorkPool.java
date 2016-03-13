import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

//
//Created by Alexandru Antonica on 11/24/15.
//Copyright © 2015 Alexandru Antonica. All rights reserved.
//


public class MapWorkPool {
	int nThreads;                 // nr total de thread-uri worker
	int nWaiting         = 0;     // nr de thread-uri worker care sunt blocate asteptand un task
	public boolean ready = false; // daca s-a terminat complet rezolvarea problemei 

	LinkedList<PartialSolution> tasks									 = new LinkedList<PartialSolution>();
	LinkedList<String> cuvMaximale 						 				 = new LinkedList<>();
	HashMap<String, LinkedList<String>> maxWordsFile 					 = new HashMap<>();
	ConcurrentHashMap<String, LinkedList<HashMap<Integer, Integer>>> map = new ConcurrentHashMap<String, LinkedList<HashMap<Integer, Integer>>>();

	/**
	 * Constructor pentru clasa WorkPool.
	 * @param nThreads - numarul de thread-uri worker
	 */
	public MapWorkPool(int nThreads) {
		this.nThreads = nThreads;

	}

	/*
	 * Initializeaza "map" cu listele in care vom tine
	 * HashMapurile locale alea fragmentelor
	 * **/
	public void initialilzeMapEntry(String file){
		map.put(file, new LinkedList<HashMap<Integer, Integer>>());
	}

	/*
	 * Cum LinkedList nu este thread safe , pentru a adauga
	 * la lista unui fisier de hashmapuri , trebuie sa facem functia
	 * syncronised
	 * **/
	public synchronized void addHashToList(String file,HashMap<Integer,Integer> outcome){
		LinkedList<HashMap<Integer, Integer>> list = (LinkedList<HashMap<Integer,Integer>>)map.get(file);
		list.add(outcome);
	}

	/*
	 * Cum LinkedList nu este thread safe , pentru a adauga
	 * la lista unui string maximal local, trebuie sa facem functia
	 * syncronised
	 * **/
	public synchronized void addMaxWordToList(String file, LinkedList<String> wordsListfile){
		wordsListfile.add(file);
	}

	/**
	 * Functie care incearca obtinera unui task din workpool.
	 * Daca nu sunt task-uri disponibile, functia se blocheaza pana cand 
	 * poate fi furnizat un task sau pana cand rezolvarea problemei este complet
	 * terminata
	 * @return Un task de rezolvat, sau null daca rezolvarea problemei s-a terminat 
	 */
	public synchronized PartialSolution getWork() {
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
	synchronized void putWork(PartialSolution sp) {
		tasks.add(sp);
		/* anuntam unul dintre workerii care asteptau */
		this.notify();

	}


}


