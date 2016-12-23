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
    @SuppressWarnings("Duplicates")
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
        int nextId = n;

        // Crea il cluster manager, l'oggetto che contiene la matrice delle distanze.
        ClusterManager manager = new ClusterManager(clusters, d);

        List<MergeHistoryRecord> historyRecords = new ArrayList<>();
        int cntIter = 0;
        long startTime = System.currentTimeMillis();
        while (manager.size() != 1){
            // Cerca le coppie di cluster a distanza minima
            List<MinDistancePair> minDistancePairs = manager.findMinDistancePairs();

            // Tengo traccia dei cluster che ho mergiato in questa iterazione
            // Questo perché lo stesso cluster può comparire in più coppie a distanza minima
            Set<Integer> mergedCluster = new HashSet<>();
            List<Cluster> newClusters = new ArrayList<>();

            // Effettua il merge di tutte le coppie a distanza minima
            for (MinDistancePair pair: minDistancePairs) {
                int r = pair.getR();
                int s = pair.getS();
                if (mergedCluster.contains(r) || mergedCluster.contains(s)){
                    continue; // Se uno dei due indici è già stato mergiato, salto la coppia
                }
                // r < s
                newClusters.add(Cluster.merge(nextId, manager.getCluster(r), manager.getCluster(s)));
                mergedCluster.add(r);
                mergedCluster.add(s);
                nextId++;

                historyRecords.add(new MergeHistoryRecord(
                        manager.getCluster(r).getId(),
                        manager.getCluster(s).getId(),
                        pair.getDist(),
                        manager.size() - newClusters.size())
                );

            }

            // Cancella dal manager i cluster che sono stati mergiati tra loro.
            List<Integer> toDelete = new ArrayList<>();
            toDelete.addAll(mergedCluster);
            manager.deleteClusters(toDelete);

            // Aggiunge al manager i nuovi cluster.
            //
            // IMPORTANTE: prima di effettuare l'inserimento devono essere stati eliminati i vecchi cluster, altrimenti
            //             non c'è posto all'interno della matrice delle distanze del manager.
            //
            for (Cluster c : newClusters) {
                manager.insert(c);
            }

            // Per questa iterazione ho finito tutte le delete ed insert, posso quindi effettuare
            // il resize della matrice e provare a liberare un po' di memoria.
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

    @SuppressWarnings("Duplicates")
    public List<MergeHistoryRecord> calculateClustersSplitting(DistanceMeasure d, List<String> words) {
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


        for (int wordIndex = 0; wordIndex < words.size(); wordIndex++) {
            int startIndex= 0;
            Character currentChar = words.get(startIndex).charAt(0);
            int q = 1;
            while (words.get(startIndex + q).charAt(0) == currentChar)
                q++;
            int endIndex = startIndex +q; // indice della prima parola che inizia con una lettera diversa.
            System.out.println("Clustering per le parole da "+ words.get(startIndex) + " a " + words.get(endIndex-1));
            try {
                System.out.println("Il prossimo partirà da: " + words.get(endIndex));
            } catch (Exception e) {
                System.out.println("Non ci sono ulteriori sub-set");
            }
            try {
                @SuppressWarnings("unchecked")
                List<String> wordsSubset = (ArrayList<String>) ((ArrayList<String>) words.subList(startIndex, endIndex)).clone();

                int n = wordsSubset.size();
                int printInterval = (int)Math.max(100, n*0.00005);

                // Creo i cluster
                List<Cluster> clusters = new ArrayList<>();
                for (int i = nextIntialIndex; i < wordsSubset.size(); i++){
                    List<String> clusterWords = new ArrayList<>();
                    clusterWords.add(wordsSubset.get(i));
                    clusters.add(new Cluster(i, clusterWords));
                }
                nextIntialIndex = nextIntialIndex + wordsSubset.size();
                ClusterManager manager = new ClusterManager(clusters, d);


                int cntIter = 0;
                long startTime = System.currentTimeMillis();
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
                        mergedCluster.add(r);
                        mergedCluster.add(s);
                        nextId++;

                        historyRecords.add(new MergeHistoryRecord(
                                manager.getCluster(r).getId(),
                                manager.getCluster(s).getId(),
                                pair.getDist(),
                                manager.size() - newClusters.size())
                        );

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



            } catch (ClassCastException e){
                System.err.println(e.toString());
            }
        }
        // historyRecord contiene tutti i con i cluster, ma devo correggere gli indici.
        // Come prima cosa ordino i record per distanza di merge.

        //noinspection unchecked
        Collections.sort(historyRecords);
        int nextCorrectId = TOTAL_NUMER_OF_STARTING_CLUTERS;
        Map<Integer, Integer> idMapping = new HashMap<>(); // <oldId, newId>
        for (int j = 0; j < historyRecords.size(); j++){
            MergeHistoryRecord current = historyRecords.get(j);
            // Aggiunto l'indice c1.
            if (idMapping.containsKey(current.getC1())){
                current.setC1(idMapping.get(current.getC1()));
            } else {
                assert current.getC1() >= TOTAL_NUMER_OF_STARTING_CLUTERS*2 -1;
                idMapping.put(current.getC1(), nextCorrectId);
                current.setC1(nextCorrectId);
                nextCorrectId++;
            }
            // Aggiunto l'indice c2.
            if (idMapping.containsKey(current.getC2())){
                current.setC1(idMapping.get(current.getC2()));
            } else {
                assert current.getC2() >= TOTAL_NUMER_OF_STARTING_CLUTERS*2 -1;
                idMapping.put(current.getC2(), nextCorrectId);
                current.setC2(nextCorrectId);
                nextCorrectId++;
            }
        }
        assert nextCorrectId == TOTAL_NUMER_OF_STARTING_CLUTERS*2 -1;
        return historyRecords;
    }
}
