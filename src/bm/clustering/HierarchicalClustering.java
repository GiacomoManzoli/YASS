package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.*;

/**
 * Classe che implementa l'algoritmo di clustering agglomerativo gerarchico in complete linkage.
 * */
public class HierarchicalClustering {

    /**
     * Esegue il clustering gerarchico utilizzando come misura di distanza {@code d} e come parole quelle presenti
     * nella lista {@code words}.
     * @param d misura di distanza.
     * @param words lista di parole da clusterizzare. Deve essere ordinata in ordine lessicografico.
     * @return storico delle operazioni di merge effettuate dall'algoritmo.
     * */
    public static List<MergeHistoryRecord> calculateClusters(DistanceMeasure d, List<String> words){
        int n = words.size();
        int printInterval = (int)Math.max(10, n*0.00005);

        // Crea n cluster, ognuno contenente una parola
        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < words.size(); i++){
            List<String> clusterWords = new ArrayList<>();
            clusterWords.add(words.get(i));
            clusters.add(new Cluster(i, clusterWords));
        }
        // nextId = n
        return clusterer(d, clusters, n);
    }

    /**
     * Esegue il clustering gerarchico utilizzando come misura di distanza {@code d} e come parole quelle presenti
     * nella lista {@code words}.
     * La lista delle parole viene oppurunamente splittata per ridurre la complessità dell'algoritmo.
     * @param d misura di distanza.
     * @param words lista di parole da clusterizzare. Deve essere ordinata in ordine lessicografico.
     * @return storico delle operazioni di merge effettuate dall'algoritmo.
     * */
    @SuppressWarnings("Duplicates")
    public static List<MergeHistoryRecord> calculateClustersSplitting(DistanceMeasure d, List<String> words) {
        // Divido per lettera inziale e faccio il clustering per ogni sottoinsieme.

        final int TOTAL_NUMER_OF_STARTING_CLUTERS = words.size();
        int nextId = TOTAL_NUMER_OF_STARTING_CLUTERS*2 -1; // di sicuro questi indici non saranno mai usati dall'algoritmo classico
        int nextIntialIndex = 0;
        List<MergeHistoryRecord> historyRecords = new ArrayList<>();

        /*
        * La gestione degli indici dei cluster in questo caso non è triviale, perché è necessario che siano simili a
        * quelli che verrebbero assegnati eseguendo l'algoritmo di clsutering su tutto il lexicon completo, altrimenti
        * sorgono dei problemi nel ricostruire i cluster.
        *
        * Eseguo quindi questa divisione:
        * - A tutti i cluster che contengono una sola parola assegno gli indici nell'intevallo [0,N-1] (estremi inclusi)
        *   Manentendo così la corrispondenza "id del clustar == indice della parola nel file di testo con il lexcion completo"
        * - A tutti i cluster che vegnono creati assegno indici a partire da 2N-1 (incluso), ovvero indici che normalmente non sarebbero
        *   assegnati dall'algoritmo di clustering.
        * - Gli indici [N, 2N-2] restano quindi liberi e possono essere assegnati in modo che sia simile a quello che verrebbe fatto
        *   normalmente, ovvero secondo l'ordine in cui sono stati effettuati i merge.
        * */


        for (int startIndex = 0; startIndex < words.size(); ) {
            //int startIndex= 0;
            Character currentChar = words.get(startIndex).charAt(0);
            int q = 1;
            while (startIndex + q < words.size() && words.get(startIndex + q).charAt(0) == currentChar)
                q++;
            int endIndex = startIndex +q; // indice della prima parola che inizia con una lettera diversa.
            System.out.println("Clustering per le parole da "+ words.get(startIndex) + " a " + words.get(endIndex-1) + " ("+ (endIndex-startIndex )+")");
            try {
                System.out.println("Il prossimo partirà da: " + words.get(endIndex));
            } catch (Exception e) {
                System.out.println("Non ci sono ulteriori sub-set");
            }

            List<String> wordsSubset = words.subList(startIndex, endIndex);

            // Creo i cluster
            List<Cluster> clusters = new ArrayList<>();
            for (int i = 0; i < wordsSubset.size(); i++){
                List<String> clusterWords = new ArrayList<>();
                clusterWords.add(wordsSubset.get(i));
                clusters.add(new Cluster(nextIntialIndex + i, clusterWords));
            }
            nextIntialIndex = nextIntialIndex + wordsSubset.size();
            int lastCreatedId = nextId-1;
            List<MergeHistoryRecord> newRecords = HierarchicalClustering.clusterer(d, clusters, nextId);
            historyRecords.addAll(newRecords);
            nextId += newRecords.size();
            if (lastCreatedId != TOTAL_NUMER_OF_STARTING_CLUTERS*2 -2 ) { // alla prima iterazione non devo inserire il bridge
                int c2;
                if (newRecords.size() > 0){
                    c2 = newRecords.get(newRecords.size() -1).getCres(); // id dell'utlimo cluster creato
                } else {
                    c2 = nextIntialIndex - 1; // è l'unico cluster con quella lettera iniziale
                }
                MergeHistoryRecord bridgeRecord = new MergeHistoryRecord(lastCreatedId, c2, nextId, Float.POSITIVE_INFINITY, 1);
                nextId++;
                historyRecords.add(bridgeRecord);
            }
            startIndex = endIndex;
        }
        // historyRecord contiene tutti i con i cluster, ma devo correggere gli indici.
        // Come prima cosa ordino i record per distanza di merge.

        //noinspection unchecked
        Collections.sort(historyRecords);
        int nextCorrectId = TOTAL_NUMER_OF_STARTING_CLUTERS;
        Map<Integer, Integer> idMapping = new HashMap<>(); // <oldId, newId>
        for (int j = 0; j < historyRecords.size(); j++){
            MergeHistoryRecord current = historyRecords.get(j);
            current.setCnt(TOTAL_NUMER_OF_STARTING_CLUTERS - j -1);
            idMapping.put(current.getCres(), nextCorrectId);
            current.setCres(nextCorrectId);
            nextCorrectId++;

            // Aggiusto l'indice c1.
            if (current.getC1() >= TOTAL_NUMER_OF_STARTING_CLUTERS) {
                if (idMapping.containsKey(current.getC1())){
                    current.setC1(idMapping.get(current.getC1()));
                } else {
                    assert false;
                }
            }
            // Aggiunto l'indice c2.
            if (current.getC2() >= TOTAL_NUMER_OF_STARTING_CLUTERS)  {
                if (idMapping.containsKey(current.getC2())) {
                    current.setC2(idMapping.get(current.getC2()));
                } else {
                    assert false;
                }
            }
        }
        assert nextCorrectId == TOTAL_NUMER_OF_STARTING_CLUTERS*2 -1;
        return historyRecords;
    }

    /**
     * Metodo di supporto che effettua il clustering su una lista di cluster,
     * iniziando ad assengare ai cluster gli id a partire da {@code nextId}
     * @param d misura di distanza.
     * @param clusters lista iniziale di cluster.
     * @return storico delle operazioni di merge effettuate dall'algoritmo.
     * */
    private static List<MergeHistoryRecord> clusterer(DistanceMeasure d, List<Cluster> clusters, int nextId) {
        int printInterval = (int)Math.max(10, clusters.size()*0.00005);
        ClusterManager manager = new ClusterManager(clusters, d);
        int cntIter = 0;
        long startTime = System.currentTimeMillis();
        List<MergeHistoryRecord> historyRecords = new ArrayList<>();
        while (manager.size() != 1){
            List<MinDistancePair> minDistancePairs = manager.findMinDistancePairs();
            Set<Integer> mergedCluster = new HashSet<>();
            List<Cluster> newClusters = new ArrayList<>();

            for (MinDistancePair pair: minDistancePairs) {
                int r = pair.getR();
                int s = pair.getS();
                if (mergedCluster.contains(r) || mergedCluster.contains(s)){
                    continue; // Se uno dei due indici è già stato mergiato, salto la coppia
                }
                newClusters.add(Cluster.merge(nextId, manager.getCluster(r), manager.getCluster(s)));
                historyRecords.add(new MergeHistoryRecord(
                        manager.getCluster(r).getId(),
                        manager.getCluster(s).getId(),
                        nextId,
                        pair.getDist(),
                        manager.size() - newClusters.size())
                );
                mergedCluster.add(r);
                mergedCluster.add(s);
                nextId++;

            }
            List<Integer> toDelete = new ArrayList<>();
            toDelete.addAll(mergedCluster);
            manager.deleteClusters(toDelete);
            //
            // IMPORTANTE: prima di effettuare l'inserimento devono essere stati eliminati i vecchi cluster, altrimenti
            //             non c'è posto all'interno della matrice delle distanze del manager.
            //
            for (Cluster c : newClusters) {
                manager.insert(c);
            }
            //
            // IMPORTANTE: questa operazione deve essere fatta DOPO tutte le cancellazioni e insierimenti
            //
            manager.resize();
            cntIter++;
            if (cntIter % (printInterval)  == 0) {
                System.out.println("Iterazione: " + cntIter + " numero di cluster presenti: "+ manager.size() +
                        " - Tempo trascorso: "+ (System.currentTimeMillis() - startTime)/1000 + " s");
            }
        }
        System.out.println("Iterazioni necessarie: "+cntIter);
        return historyRecords;
    }
}

