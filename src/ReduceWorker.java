import java.util.HashMap;
import java.util.LinkedList;

//
//Created by Alexandru Antonica on 11/24/15.
//Copyright © 2015 Alexandru Antonica. All rights reserved.
//

public class ReduceWorker extends Thread {

	ReduceWorkPool wp;
	private MapWorkPool mapWorkPool;
	static int[] fibb = {0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181,6765, 10946,17711,28657,46368,75025,121393,196418,317811,514229,832040};
	
	public ReduceWorker(ReduceWorkPool wp,MapWorkPool w){
		this.wp = wp;
		this.mapWorkPool = w;
	}

	void processPartialSolution(ReducePartialSolution ps) {

		HashMap<Integer,Integer> hash;
		String rank;
		hash = combinationProcess(ps.listHash);

		wp.reducedValsFile.put(ps.getFile(), hash);
		rank = processProcess(hash);

		ReduceWorkPool.addEntry(ps.fileName, rank);
	}



	private HashMap<Integer, Integer> combinationProcess(LinkedList<HashMap<Integer, Integer>> listHash) {
		HashMap<Integer,Integer> hash = new HashMap<>();

		for (HashMap<Integer, Integer> hashMap : listHash) {

			for (Integer key : hashMap.keySet()) {
				Integer existingVal = hash.get(key);
				if(existingVal == null){
					hash.put(key, hashMap.get(key));
				}else{
					hash.put(key, hashMap.get(key)+existingVal);
				}
			}

		}



		//Reducere lista cuvinte maximale
		for (String file : this.mapWorkPool.maxWordsFile.keySet()) {
			LinkedList<String> listaCuvMaxAux = this.mapWorkPool.maxWordsFile.get(file);
			LinkedList<String> listaCuvMax = new LinkedList<>();
			int max = 0;
			for (String string : listaCuvMaxAux) {
				if(string.length() > max)
					max = string.length();
			}

			for(String str : listaCuvMaxAux){
				if(str.length() == max)
					listaCuvMax.add(str);
			}
			this.mapWorkPool.maxWordsFile.put(file, listaCuvMax);
		}

		return hash;
	}


	private String processProcess(HashMap<Integer,Integer> hash){

		Double rank = new Double(0);

		int totalWords = getTotalWords(hash);

		try {
			for (Integer key : hash.keySet()) {
				rank += fibb[key + 1] * hash.get(key).intValue();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("KEY:"+hash.keySet());
		}
		rank = rank/totalWords;

		String trunked = rank.toString().substring(0,5);

		return trunked;
	}


	private synchronized int getTotalWords(HashMap<Integer, Integer> hash) {
		int sum = 0;

		for (Integer key : hash.keySet()) {
			sum += hash.get(key).intValue();
		}
		return sum;
	}

	public void run() {
		while (true) {
			ReducePartialSolution ps = wp.getWork();
			if (ps == null || ps.listHash == null)
				break;

			processPartialSolution(ps);
		}
	}


}
