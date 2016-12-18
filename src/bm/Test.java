package bm;

import bm.clustering.HierarchicalClustering;
import bm.clustering.MergeHistoryRecord;
import bm.yass.DistanceManager;
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


    private static void saveMergeHistory(String expName, String distanceName, List<MergeHistoryRecord> mergeHistory){
        System.out.println("Salvo i merge history per la distanza "+ distanceName+"...");
        String filePath = D_OUTPUTS + "/"+expName+"/"+DN_HISTORIES+"/merge_history_"+distanceName+".mh";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("dn.txt"), "utf-8"))) {
            //  f.write("%s,%s,%s,%s\n" % (str(m.c1), str(m.c2), str(m.dist), str(m.cnt)))
            for (MergeHistoryRecord m : mergeHistory){
                writer.write(m.getC1()+ "," + m.getC2()+","+m.getDist()+","+m.getCnt()+"\n");
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

    }

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
        lines = lines.subList(50000,51000);

        List<MergeHistoryRecord> historyRecords = HierarchicalClustering.calculateClusters(DistanceManager.d3(), lines);
        saveMergeHistory("",DistanceManager.d3().getName(),historyRecords);
    }
}
