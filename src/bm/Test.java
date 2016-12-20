package bm;

import bm.clustering.*;
import bm.yass.DistanceManager;
import bm.yass.DistanceMeasure;
import bm.yass.Experiment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Test {


    // Possibili moduli per la pipeline
    private static final String PIPE_CLUSTERING = "clustering";
    private static final String PIPE_SAVE_GRAPH_DATA = "save_graph_data";
    private static final String PIPE_SAVE_HISTORY = "save_history";
    private static final String PIPE_LOAD_HISTORY = "load_history";
    private static final String PIPE_MERGE_HISTORY = "merge_history";
    private static final String PIPE_YASS_STEMMING = "YASS_stemming";
    //Nomi delle directory
    private static final String D_OUTPUTS = "../outputs";
    private static final String DN_STEMMED_DICT = "stemmed_dict";
    private static final String DN_HISTORIES = "histories";
    private static final String DN_DATA = "graph_data";



    public static void main(String[] args) {
        List<String> lines = new ArrayList<>();


        try {
            File fileDir = new File("/Users/gmanzoli/ideaProjects/java-IR/lexicon/italian/AGZ1994.txt");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fileDir), "UTF8"));
            String str;
            while ((str = in.readLine()) != null) {
                String line = str;
                line = line.trim();
                line = line.split(",")[0];
                lines.add(line);
            }
            in.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(lines.size());
        System.out.println(lines.get(lines.size()-1));
        lines = lines.subList(50000,55539);

        MyCustomBigArray bigArray = new MyCustomBigArray((long)Integer.MAX_VALUE + (long)1);
        for (long i = 0; i < bigArray.getSize(); i++){
            bigArray.set(i, i);
            assert bigArray.get(i) == i;
        }

        bigArray.resize(19);
        for (long i = 0; i < bigArray.getSize(); i++){
            bigArray.set(i, i);
            assert bigArray.get(i) == i;
        }

        /*List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++){
            List<String> clusterWords = new ArrayList<>();
            clusterWords.add(lines.get(i));
            clusters.add(new Cluster(i, clusterWords));
        }
        int nextId = lines.size();
        ClusterManager manager = new ClusterManager(clusters, DistanceManager.d3());

        long startTime = System.currentTimeMillis();
        MinDistancePair minDistancePair = manager.findMinDistancePair();
        System.out.println(System.currentTimeMillis() - startTime);
        System.out.println(minDistancePair.toString());*/

    }
}
