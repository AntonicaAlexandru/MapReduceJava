import java.util.HashMap;
import java.util.LinkedList;

//
//Created by Alexandru Antonica on 11/24/15.
//Copyright © 2015 Alexandru Antonica. All rights reserved.
//

public class ReducePartialSolution {
	
	LinkedList<HashMap<Integer, Integer>> listHash = null;
	String fileName ="";
	public ReducePartialSolution(LinkedList<HashMap<Integer, Integer>> listHash,String file){
	       this.listHash = listHash;
	       this.fileName = file;
	}
	
	public String getFile(){
		return this.fileName;
	}
}
