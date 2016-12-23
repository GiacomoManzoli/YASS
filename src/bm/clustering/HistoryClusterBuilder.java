package bm.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe che costruisce ricostruisce vari insiemi di clusters a partire da uno storico delle operazioni
 * di merge dell'algoritmo di clustering gerarchico.
 * */
public class HistoryClusterBuilder {

    /**
          Costruisce vari ClusterSet secondo lo storico di merge ricevuto come parametro.
          E' importante che l'ordine delle parole sia lo stesso con cui è stata calcolato lo storico
          dei merge, altrimenti si verificano situazioni di inconsistenza.

          Inoltre, si assume che gli elementi della lista seguano l'ordine dei vari merge (i.e. il primo elemento
          dello storico corrisponde con il primo merge, ecc.)

          @param words lista di termini da clusterizzare.
          @param history lista contentene lo storico dei merge.
          @param thresholds lista di distanze alle quali creare ClusterSet.
          @return lista con i vari ClusterSet creati.

      * */
    public static List<ClusterSet> buildSetsFromHistory(List<String> words, List<MergeHistoryRecord> history, float[] thresholds){

        List<ClusterSet> snapshots = new ArrayList<>();

        ClusterSet clusterSet = new ClusterSet(0);

        // Ricostruisce lo stato inziale dell'algoritmo di clustering
        for (int i = 0; i < words.size(); i++) {
            List<String> cw = new ArrayList<>();
            cw.add(words.get(i));
            clusterSet.addCluster(new Cluster(i, cw));
        }
        int nextId = words.size();

        // Ordina i thresholds in ordine crescente
        Arrays.sort(thresholds);
        int cntThreshold = 0;

        // Ripercorre lo storico dei merge per costruire i vari set
        for (MergeHistoryRecord record : history) {
            if (cntThreshold < thresholds.length && record.getDist() > thresholds[cntThreshold]) {
                /*
                *   Il merge che sto per fare è a distanza più grande della soglia minima di snapshot.
                *   creo quindi uno snapshot del set, facendo una copia profonda dello stato corrente
                *   e lo aggiungo in lista
                * */
                ClusterSet newSet = clusterSet.copy();
                newSet.setThreshold(thresholds[cntThreshold]);
                snapshots.add(newSet);
                cntThreshold++;
            }

            Cluster cluster1 = clusterSet.getCluster(record.getC1());
            Cluster cluster2 = clusterSet.getCluster(record.getC2());
            Cluster merged = Cluster.merge(nextId, cluster1, cluster2);
            nextId++;
            clusterSet.removeCluster(record.getC1());
            clusterSet.removeCluster(record.getC2());
            clusterSet.addCluster(merged);
        }

        return snapshots;
    }
}
