Antonica Alexandru 333CC

Pentru inceput am folosit laboratorul 5 pentru a avea acces la  templatetul de
workpool si worker.

Pasi si explicatii :

1. Se citesc mai intai parametrii in metoda main din clasa Test.

2. Retin fisierele din input file intr-o lista "fileList", pentru utilizari ulterioare.

3. Pentru fiecare fisier din "fileList"
      |->Pentru a retine cuvintele maximale , folosesc un hashmap avand ca si cheie numele fisierului
         iar ca valoare , o lista de cuvinte maximale.

      |->Apelez functia "getLimits" ce primeste ca parametri numele fisierului si dimensiunea unui bloc de citit
          |-> aceasta functie intoarce o lista de indici reprezentand startOffSet , si endOffSet
              :-> de unde si pana unde trebuie sa citeasca fiecare fragment
          |-> pe baza listei de mai sus , creez cate un "PartialSolution" ce primeste ca parametri
                  "startOffSet" si dimensiunea blocului trebuie citit.
                  |-> Toate obiectele de tipul partial solution le adaug in MapWorkPool.

4. Creez o lista cu MapWorkeri pe care ii pornesc.

    4.1 Explicatie proccessPartialSolution:
          |-> Fiecare worker primeste un Partial solution ce contine
              numele fisierului , pozitia de startOffSet si numarul de bytes pe care trebuie cititi
          |-> Ne pozitionam in fisier la startOffSet si citim "size" bytes.
          |-> Daca startOffSet este 0 , inseamna ca suntem la inceputul fisierului
              si cititim "size" bytes + toate caracterele pana la urmatorul delimitator.
              |->Altfel , skippam primele caractere pana la primul delimitator,
                 si citim pana la urmatorul delimitator de pe pozitie mai mare ca
                 startOffSet + size;
              ||-> In ambele cazuri , apelam functia doMagic() ce primeste ca parametri un string
                   si un hashMap .
                          |-> acest hashmap , este unul local al fragmentului (voi explica in continuare utilizarea lui)
                          |-> doMagic() {

                                Stringul primit ca parametru este impartit folosind delimitatorii.

                                Se retine intr'o lista locala , toate cuvintele maximale , la fiecare trecere si reexaminare
                                a celui mai mare cuvant din lista.

                                In hashmap se retin de cate ori apar cuvintele de x lungime , unde x reprezinta cheia.

                                Cuvintele din lista de cuvinte maximale locala , se adauga in
                                lista mare de cuvinte maximale ce se afla in clasa WorkPool(exista maxWordsFile
                                ce este un hashmap cu key: fileName si valoare : lista de cuvinteMaximale) , daca nu exista deja.
                                    |-> ulterior voi face o triere a cuvintelor pentru a pastra numai cuvintele cu lungime maxima.

                                Hashul calculat(local) , este si el adaugat unui hashMare care se afla tot in workPool
                                cu cheie "fileName" si valoare o lista de hashMapuri locale.
                                    |-> adaugarea se face de catre o lista syncronised deoarece LinkedListul din hashMapul mare
                                        nu este threadsafe.
                          }

5. Pentru fiecare worker din lista dau join() pentru a-i astepta sa termine si sa trec la pasul de reduce

6. Creed un pool de reduce : ReduceWorkPool.

7. Pentru fiecare key din MapWorkPool.map , creez o ReducePartialSolution , ce primeste ca parametri
    o lista de hashMapuri si numele fisierului din care fac parte.

8. Pornesc reduce workerii.
    8.1 Explicatie proccessPartialSolution din ReduceWorker:
          |->combinationProcess() {

              Intoarce un hashmap final in care s-aru adunat toate valorile
              hashmapurilor din lista primita ca parametru

              Tot aici editez lista cu cuvintemaximale corespunzatoare fisierului primit ca parametru
              pastrand cel/cele mai lungi cuvinte.

              }
          |->processProcess(){

              Calculez rankul fisierului conform formulei din enunt
              pe hashul rezultat in etapa ce combinationProcess

          }
          |-> Rezultatul din processProcess il adaug in hashMapul fileRank din ReduceWorkPool
          ce contine numele fisierului si rankul sau.

9. Apelez join pentru fiecare thread pentru a astepta sa se termine taskurile din reduce work pool

10. Sortez hashMapul "fileRank" conform specificatiilor si scriu in fisier rezultatele.
