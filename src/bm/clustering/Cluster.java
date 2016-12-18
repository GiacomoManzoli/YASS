package bm.clustering;


import bm.yass.DistanceMeasure;

import java.util.List;

public class Cluster {

    public static Cluster merge(int id, Cluster c1, Cluster c2) {
        List<String> newWords = c1.words.subList(0, c1.words.size());
        newWords.addAll(c2.words);
        return new Cluster(id, newWords);
    }

    private int id;
    private List<String> words;
    private String longestWord;
    private String shortestWord;
    private String longestPrefix;


    public Cluster(int id, List<String> words) {
        this.id = id;
        this.words = words;

        this.longestWord = words.get(0);
        this.shortestWord = words.get(0);

        for (String w : this.words) {
            if (w.length() > this.longestWord.length()){
                this.longestWord = w;
            }
            if (w.length() < this.shortestWord.length()){
                this.shortestWord = w;
            }
        }

        this.longestPrefix = "";
        for (int i = 0; i < shortestWord.length(); i++){
            Character c = this.shortestWord.charAt(i);
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

    public float distance(Cluster nextCluster, DistanceMeasure d){
        float maxDist = 0;
        for (String w1 : this.words){
            for (String w2 : nextCluster.words){
                float dist = d.calculate(w1,w2);
                if (dist > maxDist) {
                    maxDist = dist;
                }
            }
        }
        return maxDist;
    }

    public String getCentralWord(){
        return this.longestPrefix;
    }


    public boolean contains(String w) {
        return this.words.contains(w);
    }

    public int getId() {
        return id;
    }

    public List<String> getWords() {
        return words;
    }
}
