package bm.clustering;


import bm.yass.DistanceMeasure;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta un Cluster di parole (stringhe)
 * */
public class Cluster {

    /**
     * Esegue il merge di due cluster, creando un nuovo cluster con l'{@code id} ricervuto come parametro.
     * Il nuovo cluster viene creato aggiungendo alla lista di {@code c1} le parole presenti su {@code c2}.
     *
     *  @param id identificativo del nuovo cluster.
     *  @param c1 primo cluster da mergiare.
     *  @param c2 secondo cluster da mergiare.
     *  @return il nuovo cluster ottenuto mergiando i due cluster ricevuti come parametro.
     * */
    static Cluster merge(int id, Cluster c1, Cluster c2) {
        List<String> newWords = (ArrayList<String>)((ArrayList<String>)c1.words).clone();
        newWords.addAll(c2.words);
        return new Cluster(id, newWords);
    }

    private int id;
    private List<String> words;
    private String longestPrefix;

    /**
     * Costruisce un nuovo cluster utilizzando le parole presenti in {@code words}.
     * @param id identificativo del nuovo cluster.
     * @param words parole da inserire nel nuovo cluster.
     * */
    Cluster(int id, List<String> words) {
        this.id = id;
        this.words = words;

        String longestWord = words.get(0);
        String shortestWord = words.get(0);

        for (String w : this.words) {
            if (w.length() > longestWord.length()){
                longestWord = w;
            }
            if (w.length() < shortestWord.length()){
                shortestWord = w;
            }
        }
        /* Ricerca del più lungo prefisso in comune tra le parole del cluster. */
        this.longestPrefix = "";
        for (int i = 0; i < shortestWord.length(); i++){
            Character c = shortestWord.charAt(i);
            boolean stop = false;
            for (String w : this.words) {
                if (w.charAt(i) != c){
                    stop = true;
                    break;
                }
            }
            if (! stop){
                this.longestPrefix = this.longestPrefix.concat(c.toString());
            } else {
                break;
            }
        }
    }

    /**
     * Calcola la distanza, secondo complete linkagem con il cluster {@code nextCluster} utilizzando la
     * misura di distanza {@code d}.
     * Se i cluster contengono molte parole, il calcolo della distanza viene effettuato in parallelo.
     * @param nextCluster cluster con cui calcolare la distanza.
     * @param d misura di distanza da utilizzare.
     * @return distanza dei due cluster
     * */
    float distance(Cluster nextCluster, DistanceMeasure d) {
        if (this.words.size() == 1 && nextCluster.words.size() == 1) {
            // Così evito di scomodare il parallelismo mentre calcolo la
            // matrice delle distanze
            return d.calculate(this.words.get(0), nextCluster.words.get(0));
        } else if (this.words.size() * nextCluster.words.size() < 5000){
            float maxDist = 0;
            for (String w1 : this.words){
                for (String w2: nextCluster.words){
                    float dist = d.calculate(w1,w2);
                    if (dist > maxDist) {
                        maxDist = dist;
                    }
                }
            }
            return maxDist;
        } else{
            // Calcolo parallelo
            return ClusterDistanceTask.calculateClusterDistance(this, nextCluster, d);
        }
    }

    /**
     * Ritorna la parola centrale del cluster, ovvero il massimo prefisso comune tra tutte le parole presenti.
     * @return parola centrale del cluster.
     * */
    public String getCentralWord(){
        return this.longestPrefix;
    }

    int getId() {
        return id;
    }

    public List<String> getWords() {
        return words;
    }
}
