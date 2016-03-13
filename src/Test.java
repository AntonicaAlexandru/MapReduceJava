import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//
//Created by Alexandru Antonica on 11/24/15.
//Copyright © 2015 Alexandru Antonica. All rights reserved.
//

public class Test {
    
    
    static LinkedList<String> fileList = new LinkedList<>();
    int dim;
    int numFiles;
    String inputFile,outputFile;
    private static List<Map.Entry<String, String>> listFinalHash;
    public static void main(String[] args) throws InterruptedException {
        
        long startTime = System.currentTimeMillis();
        System.out.println("---------------------------------");
        System.out.println("Testing: "+args[1] + " with "+args[0]+" thread(s)");
        Test test = new Test();
        test.setInput(args[1]);
        test.setOutput(args[2]);
        
        //Seteaza dim in PartialSolution class
        try {
            test.readInput();
        } catch (IOException e) {
            System.out.println("ERROR: Problem readind inputFile");
        }
        
        
        //MAP WorkPool
        int threadsNum = Integer.parseInt(args[0]);
        MapWorkPool mapWorkPool = new MapWorkPool(threadsNum);
        ArrayList<Long> fragmentsIndexes = null;
        
        //Cream partial solution pentru fiecare fragment din firecare document
        for (String file : Test.fileList) {
            
            if(fragmentsIndexes != null)
                fragmentsIndexes.clear();
            
            
            fragmentsIndexes = test.getLimits(file , test.dim);
            mapWorkPool.maxWordsFile.put(file, new LinkedList<String>());
            
            mapWorkPool.initialilzeMapEntry(file);
            for (int i = 0; i < fragmentsIndexes.size()-1; i++) {
                //Cream un task pentru fiecare fragment
                PartialSolution ps = new PartialSolution(file,fragmentsIndexes.get(i),
                                                         (int)(fragmentsIndexes.get(i+1) - fragmentsIndexes.get(i)));
                
                //il adaugam in workpool
                mapWorkPool.putWork(ps);
            }
        }
        
        // Creeam un numar de workeri egal nu cu numarul de threaduri
        LinkedList<MapWorker> mapWorkers = new LinkedList<>();
        for(int i=0; i<threadsNum; i++){
            MapWorker mapWorker = new MapWorker(mapWorkPool);
            mapWorkers.add(mapWorker);
        }
        
        for (MapWorker mapWorker : mapWorkers) {
            mapWorker.start();
        }
        
        
        for (MapWorker mapWorker : mapWorkers) {
            mapWorker.join();
        }
        //System.out.println("Toti workerii au terminat MapWorkPool-ul");
        
        
        /*
         * Listele cu cuvinte maximale contin toate cuvintele maximale locale pentru
         * fiecare fragment si pentru fiecare runda de calculare a unui maxim.
         * De aceea rtebuie sa vedem care este mximul din lista si sa pastram
         * doar acele cuvinte care au lungimea egala cu maximul dintre ele.
         * **/
        
        
        
        //REDUCE
        ReduceWorkPool reduceWorkPool = new ReduceWorkPool(threadsNum);
        for(String file : mapWorkPool.map.keySet()){
            
            LinkedList<HashMap<Integer,Integer>> lh = mapWorkPool.map.get(file);
            ReducePartialSolution rd = new ReducePartialSolution(lh, file);
            reduceWorkPool.putWork(rd);
            
            
        }
        
        
        
        // Creeam un numar de workeri egal nu cu numarul de threaduri
        LinkedList<ReduceWorker> reduceWorkers = new LinkedList<>();
        for(int i=0; i<threadsNum; i++){
            ReduceWorker reduceWorker = new ReduceWorker(reduceWorkPool,mapWorkPool);
            reduceWorkers.add(reduceWorker);
        }
        
        for (ReduceWorker reduceWorker : reduceWorkers) {
            reduceWorker.start();
        }
        for (ReduceWorker reduceWorker : reduceWorkers) {
            reduceWorker.join();
        }
        
        //System.out.println("Toti workerii au terminat ReduceWorkPoolul-ul");
        
        
        listFinalHash = new ArrayList<Map.Entry<String, String>>();
        for(Entry<String, String> entry : ReduceWorkPool.fileRank.entrySet()){
            listFinalHash.add(entry);
        }
        Collections.sort(listFinalHash, new Comparator<Entry<String, String>>() {
            
            @Override
            public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                Double o1Val = Double.parseDouble(o1.getValue());
                Double o2Val = Double.parseDouble(o2.getValue());
                
                int result = -o1Val.compareTo(o2Val);
                
                if(result == 0){
                    String fileOne = o1.getKey();
                    String fileTwo = o2.getKey();
                    Integer firstIndex = Test.fileList.indexOf(fileOne);
                    Integer secondIndex = Test.fileList.indexOf(fileTwo);
                    //	System.out.println("FisierA :"+ fileOne +" FisierB:"+fileTwo +
                    //							" Valori :"+firstIndex + " "+secondIndex);
                    
                    return firstIndex.compareTo(secondIndex);
                    
                }
                return result;
                
            }
        });
        
        long stopTime = System.currentTimeMillis();
        long time = (stopTime - startTime)/1000;
        System.out.println("TIMP: "+time+" seconds");
        System.out.println("---------------------------------");
        writeToFile(test.outputFile,mapWorkPool);
        
    }
    
    
    private static void writeToFile(String outputFile, MapWorkPool mapWorkPool) {
        BufferedWriter bw ;
        try {
            bw = new BufferedWriter(new FileWriter(outputFile));
            
            for (Map.Entry<String, String> entry : listFinalHash) {
                LinkedList<String> temp = mapWorkPool.maxWordsFile.get(entry.getKey());
                String str = temp.get(0);
                bw.write(entry.getKey()+";"+entry.getValue()+";[" + 
                         str.length()+","+temp.size()+"]\n");
            }
            
            bw.close();
        } catch (IOException e) {
            System.out.println("ERROR: Nu s-a putut deschide fisierul de scriere");
            return;
        }
    }
    
    
    ArrayList<Long> getLimits(String file , int dim){
        ArrayList<Long> limits = new ArrayList<>();
        
        
        File f = new File(file);
        long size = f.length();
        long max = 0;
        for(long i = 0; i < size-1; i += dim){
            max = i;
            limits.add(i);
            
        }
        
        //Verifica daca a mai ramas vreun fragment mai mic decat dim
        if(max < size-1){
            limits.add(size-1);
        }
        return limits;
    }
    
    void setDim(int dim){
        this.dim = dim;
    }
    
    void setnumFiles(int numFiles){
        this.numFiles = numFiles;
    }
    
    void setInput(String input){
        this.inputFile = input;
    }
    
    void setOutput(String output){
        this.outputFile = output;
        
    }
    
    void readInput() throws IOException{
        
        BufferedReader br = new BufferedReader(new FileReader(this.inputFile));
        String ln = "";
        
        //Read dim
        ln = br.readLine();
        this.dim = Integer.parseInt(ln);
        
        //Read numFiles
        ln = br.readLine();
        this.numFiles = Integer.parseInt(ln);
        
        //Read files
        int i = 0;
        while(i < numFiles){
            ln = br.readLine();
            fileList.add(ln);
            
            i++;
            
        }
        br.close();
    }
    
}
