import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

//
//Created by Alexandru Antonica on 11/24/15.
//Copyright © 2015 Alexandru Antonica. All rights reserved.
//

public class MapWorker extends Thread {

	MapWorkPool wp;
	static String s = ";a:a/a?a~a\\a.a,a>a<a~a`a[a]a{a}a(a)a!a@a#a$a%a^a&a-a_a+a'a=a*a\"a|a a\ta\n";
	static String ss = ";:/?~\\.,><~`[]{}()!@#$%^&-_+'=*\"| \t\n";
	static ArrayList<String> delims = new ArrayList<>(Arrays.asList(s.split("a")));
	String fileName = "";
	public MapWorker(MapWorkPool wp){
		this.wp = wp;
	}


	/*	Aici are loc numararea literelor din fiecare cuvant 
	Trebuie sa avem grija sa luam in calcul cazurile
    cand fragmentele se opresc in interiorul cuvintelor 
    si nu la un delimitator*/

	void processPartialSolution(PartialSolution ps) {
		HashMap<Integer, Integer> hash = new HashMap<>();
		RandomAccessFile accFile = null;

		String file 		= ps.getFileName();
		this.fileName = file;
		int size    		= ps.getToRead();
		byte[] charsRead 	= new byte[size];

		try {
			//Folosim RandomAccessFile pentru a deschide fisierul
			accFile = new RandomAccessFile(file, "r");

			//Folosind offsetul se ne deplasam in fisier la pozitia respectiva
			// si citim de acolo "size" bytes
			accFile.seek(ps.getStartOffSet());
			accFile.read(charsRead);

			//Tratare caz intersectare fragmente
			/* 
			 * Fiecare fragment trebuie sa sara peste primele caractere
			 * pana intalneste un delimitator . Aceasta regula NU se aplica 
			 * si primului fragment
			 * **/


			if(ps.getStartOffSet() == 0){			//Primul fragment

				//Citim mai mult de size , pana intalnim un delimitator

				String result = new String(charsRead);
				char c = (char) accFile.readByte();
				String aux = c+"";


				while(!delims.contains(aux)){     // continuam sa citim
					//Retinem tot ce citim
					result += aux;

					//Citim urmatorul caracter
					c = (char) accFile.readByte();
					aux = c+"";
				}

				//Aici avem ce am citit prima data + result
				// Le concatenam
				String resAux = "";
				resAux += result.toLowerCase();
				accFile.close();
				doMagic(resAux,hash);

			}else{									//Fragment oarecare

				//Sare peste caractere pana intalneste un delimitator
				// Si incepe sa citeasca de acolo
				char c;

				int pos = 0;
				@SuppressWarnings("unused")
				String h = "";

				for(int i = 0 ; i < charsRead.length ; i++){
					c = (char) charsRead[i];
					if(!delims.contains(c+""))
						pos++;
					else
						break;
				}

				//In res retinem caracterele de la primul delimitator gasit

				String res = new String(charsRead).substring(pos);

				//Citim dupa endOffSet pentru a fi sigur ca respectam regula de mai sus
				String result = "";
				if(ps.getEndOffSet() != accFile.length()-1){ // daca ultimul caracter citit nu e delimitator
					c = (char) accFile.readByte();
					String aux = c+"";

					while(!delims.contains(aux)){     // continuam sa citim
						//Retinem tot ce citim
						result += aux;

						//Citim urmatorul caracter
						c = (char) accFile.readByte();
						aux = c+"";
					}
				}
				res += result;
				accFile.close();
				doMagic(res,hash);

			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	private void doMagic(String resAux, HashMap<Integer, Integer> hash) {


		LinkedList<String> localMax = new LinkedList<>();

		/* Imparte stringul dupa delims
		 * Mai intai inlocuiesc toate caracterele din delim cu spatiu
		 * si apoi splituiesc stringul dupa spatiu
		 */
		for (Character oldChar : ss.toCharArray()) {
			resAux = resAux.replace(oldChar, ' ');
		}

		String[] parts = resAux.split("\\s");
		int     szStr;
		Integer valExisting;
		String  string;
		int     max = 0;

		for (String stringx : parts) {
			string = stringx.toLowerCase();
			szStr = string.length();

			if(szStr > 0){

				//Retinem cuvintele maximale pentru fiecare tura 
				if(szStr > max){
					localMax.clear();
					localMax.add(string);
					max = szStr;
				}else{
					if(szStr == max)
						if(!localMax.contains(string)){
							localMax.add(string);
						}
				}

				valExisting = hash.get(szStr);

				if(valExisting != null && valExisting.intValue() > 0){
					valExisting ++;
					hash.put(szStr, valExisting);
				}else{
					hash.put(szStr, 1);
				}
			}
		}


		LinkedList<String>  wordsListfile  = this.wp.maxWordsFile.get(fileName);
		for (String string2 : localMax) {
			if(!wordsListfile.contains(string2)){
				this.wp.addMaxWordToList(string2,wordsListfile);
			}
		}
		this.wp.addHashToList(this.fileName, hash);
	}

	public void run() {
		while (true) {
			PartialSolution ps = wp.getWork();
			if (ps == null)
				break;

			processPartialSolution(ps);
		}
	}


}

