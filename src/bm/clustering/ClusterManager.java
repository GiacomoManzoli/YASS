package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * Classe che si occupa di gestire i cluster e la relativa matrice delle distanze.
 * */
class ClusterManager {

    static ForkJoinPool commonPool = new ForkJoinPool(); // Usa tutti i core possibili

    private List<Cluster> clusters;
    private DistanceMeasure d;
    MyCustomBigArray dist;


    /* Formule per la conversione degli indici (da notare che la matrice è flippata, quindi è necessario invertire k)
    * i = n - 2 - floor(sqrt(-8*k + 4*n*(n-1)-7)/2.0 - 0.5)
    * j = k + i + 1 - n*(n-1)/2 + (n-i)*((n-i)-1)/2
    * k = (n*(n-1)/2) - (n-i)*((n-i)-1)/2 + j - i - 1
    * */
    long _k(int i, int j){
        long n = clusters.size();
        long k = (n *(n-1))/2 - ( (n-i)*(n-i-1) )/2 + j - i - 1;
        k = ( n*(n-1) )/2 - 1 - k;
        return k;
    }

    int _i(long k){
        long n = clusters.size();
        k = ( n*(n-1) )/2 - 1 - k; // ritrasformo k
        long i = n - 2 - (int)Math.floor(Math.sqrt(-8*k + 4*n*(n-1)-7)/2 - 0.5);
        return  (int)i; // i
    }

    int _j(long k){
        long n = clusters.size();
        int i = _i(k); // _i trasforma k, quindi devo calcolarlo prima
        k = (n*(n-1))/2 - 1 - k; // ritrasformo k
        long j =  (k + i + 1 - (n*(n-1))/2 + ((n-i)*((n-i)-1))/2); // j
        return (int)j;
    }

    /**
     * Crea un nuovo manager per i cluster presenti in {@code clusters}, utilizzando come misura di distanza {@code d}.
     * @param clusters clusters da inserire nel managare.
     * @param d misura di distanza da utilizzare per definire la matrice.
     * */
    ClusterManager(List<Cluster> clusters, DistanceMeasure d) {
        this.clusters = clusters;
        this.d = d;
        // n in realtà sarebbe un int, ma lo dichiaro come long per evitare overflow nel calcolo di tot.
        long n = clusters.size();
        long tot = (n*(n-1))/2;
        dist = new MyCustomBigArray(tot);
        System.out.println("Creo la matrice delle distanze...");

        long startTime = System.currentTimeMillis();
        BuildDistanceMatrixTask.buildDistanceMatrix(this, d);
        System.out.println("Fine creazione matrice. Tempo necessario "
                                +(System.currentTimeMillis()-startTime)/1000 +" s");
    }

    /**
     * Rimuove dal manager i cluster il cui indice è presente nella lista {@code indexes}.
     * Da notare che la rimozione viene fatta per indice e non per id dei cluster.
     * @param indexes indici dei cluster da rimuovere
     * */
    void deleteClusters(List<Integer> indexes){
        /*
        * PROBLEMA: le strutture dati di supporto a questo metodo possono richiede un'elevata quantità di spazio.
        * Viene infatti utilizzato un Set, implementato con un hashmap, indicizzata per chiavi di tipo long.
        *
        * SOLUZIONE: l'eliminazione viene fatta in più passate. Così facendo l'occupazione in memoria è ridotta,
        * anche se questo rende l'operazione meno efficiente in termini di tempo.
        * */

        // Ordino gli indici in ordine crescente
        Collections.sort(indexes);
        actuallyDeleteClusters(indexes);
        //int start;
        //for (start = 0; start + 4 < indexes.size(); start+= 4){
        //    List<Integer> ar = new ArrayList<>();
        //    for (Integer i:  indexes.subList(start, start+4)) {
        //        ar.add(i);
        //    }
        //    actuallyDeleteClusters(ar);
        //}
        //List<Integer> ar = new ArrayList<>();
        //for (Integer i:  indexes.subList(start, indexes.size())) {
        //    ar.add(i);
        //}
        //try {
        //    if(ar.size() > 0)
        //        actuallyDeleteClusters(ar);
        //} catch (Exception e) {
        //    System.out.println(e.toString());
        //    System.out.println();
        //}
    }

    private void actuallyDeleteClusters(List<Integer> indexes) {
        int n = clusters.size();
        // La stessa coppia può comparire più di una volta, quindi le memorizzo in un set per evitare duplicati.
        // Anziché memorizzare direttamente la coppia, calcolo subito l'indice della coppia nella matrice linearizzata.
        //Set<Long> toDelete = new HashSet<>();
        List<Long> toDeleteIndexes = new ArrayList<>();
        // Per ogni indice calcolo le coppie in cui compare
        for (int r : indexes) {
            // calcolo le coppie del tipo (*,r)
            for (int i = 0; i < r; i++) {
                long index = _k(i,r);
                if (index >= 0 && index < dist.getSize() ){
                    //toDelete.add(index);
                    if (! toDeleteIndexes.contains(index))
                        toDeleteIndexes.add(index);
                }
            }
            // calcolo le coppie del tipo (r,*) (c'è (r,s))
            // sono consecutive e ce ne sono n-r-1
            for (int j = r+1; j < r+1+(n-r-1); j++) {
                long index = _k(r,j);
                if (index >= 0 && index < dist.getSize()){
                    //toDelete.add(index);
                    if (! toDeleteIndexes.contains(index))
                        toDeleteIndexes.add(index);
                }
            }
        }

        //List<Long> toDeleteIndexes = new ArrayList<>();
        //toDeleteIndexes.addAll(toDelete);
        // Ordino gli indici da cancellare in ordine decrescente
        Collections.sort(toDeleteIndexes);

        // Sovrascrivo i valori da cancellare compattando il vettore.
        // Anche qui può esserci un overflow, perché l'espressione per il calcolo di tot
        // tipa come int se non metto il cast a Long
        long tot = ((long)n*(n-1))/2;
        int cntDeleted = 0;

        for (long it = 0; it < tot; it++) {
            // Se non ho ancora cancellato niente e non
            // devo cancellare l'indice corrente, passo all'elemento successivo
            if (cntDeleted == 0 && it != toDeleteIndexes.get(cntDeleted)) { continue; }

            //if (cntDeleted < toDelete.size() && it == toDeleteIndexes.get(cntDeleted))
            if (cntDeleted < toDeleteIndexes.size() && it == toDeleteIndexes.get(cntDeleted))
                cntDeleted += 1;

            // Prima di copiare il prossimo indice, controllo di non copiare
            // un indice che poi deve essere cancellato
            //while (cntDeleted < toDelete.size() && it + cntDeleted == toDeleteIndexes.get(cntDeleted))
            while (cntDeleted < toDeleteIndexes.size() && it + cntDeleted == toDeleteIndexes.get(cntDeleted))

                cntDeleted += 1;

            if (it + cntDeleted < tot)
                dist.set(it, dist.get(it+cntDeleted));
            else
                break;
        }

        // Cancancello i cluster anche dalla lista
        for (int i = 0; i < indexes.size(); i++) {
            int adjustedIndex = indexes.get(i) - i;
            clusters.remove(adjustedIndex);
        }
    }

    /**
     * Inserire il cluster all'interno del manger.
     * @param cluster cluster da inserire nel manager.
     * */
    void insert(Cluster cluster){
        // Non serve modificare la dimensione del vettore, vado ad occupare lo spazio garbage del vettore.
        clusters.add(0, cluster);
        int n = clusters.size();

        // Calcolo le nuove distanze
        int i = 0;  // Ho inserito il cluster in testa, ha indice 0
        for (int j = i+1; j < n; j++) {
            long k = _k(i, j);
            dist.set(k, clusters.get(i).distance(clusters.get(j), d));
        }
    }

    /**
     * Ricerca in modo parallelo le coppie di cluster presenti nel manager che sono a distanza minima.
     * @return lista di coppie di cluster presenti nel manager che sono a distanza minima.
     * */
    List<MinDistancePair> findMinDistancePairs() {
        return FindMinDistancePairTask.findMinDistancePairs(this);
    }

    int size() {
        return clusters.size();
    }

    Cluster getCluster(int i){
        return clusters.get(i);
    }

    /**
     * Effettua il resize della matrice delle distanze.
     * Serve per liberare della memoria dopo che sono state effettuate un certo numero di iterazioni.
     * */
    void resize() {
        long n = this.clusters.size();
        long newSize = (n*(n-1))/2;
        dist.resize(newSize);
    }
}
