package bm;

import bm.clustering.ClusterSet;
import bm.clustering.HierarchicalClustering;
import bm.clustering.HistoryClusterBuilder;
import bm.clustering.MergeHistoryRecord;
import bm.yass.Experiment;
import bm.yass.YASS;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Main {

    // Possibili moduli per la pipeline
    private static final String PIPE_CLUSTERING = "clustering";
    private static final String PIPE_SAVE_GRAPH_DATA = "save_graph_data";
    private static final String PIPE_SAVE_HISTORY = "save_history";
    private static final String PIPE_LOAD_HISTORY = "load_history";
    private static final String PIPE_MERGE_HISTORY = "merge_history";
    private static final String PIPE_YASS_STEMMING = "YASS_stemming";
    //Nomi delle directory
    private static final String D_OUTPUTS = "outputs";
    private static final String DN_STEMMED_DICT = "stemmed_dict";
    private static final String DN_HISTORIES = "histories";
    private static final String DN_DATA = "graph_data";


    private static void makeExperimentOutputDirs(String propertiesFilePath, String experimentName) {
        String experimentPath = D_OUTPUTS +"/" +experimentName+"/";
        String dictPath = experimentPath + DN_STEMMED_DICT;
        String historiesPath = experimentPath + DN_HISTORIES;
        String dataPath = experimentPath + DN_DATA;

        try {
            Files.createDirectories(Paths.get(experimentPath));
            Files.createDirectories(Paths.get(dictPath));
            Files.createDirectories(Paths.get(historiesPath));
            Files.createDirectories(Paths.get(dataPath));
            Files.copy(Paths.get(propertiesFilePath), Paths.get(experimentPath), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (DirectoryNotEmptyException de) {
            System.out.println("Directory già esistenti.");
        }
        catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    private static void saveGraphData(String expName, String distanceName, List<MergeHistoryRecord> mergeHistory) {
        System.out.println("Salvo i dati per la distanza "+ distanceName+"...");
        String filePath = D_OUTPUTS + "/"+expName+"/"+DN_DATA+"/data_"+distanceName+".csv";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), "utf-8"))) {
            writer.write("distance;cluster\n");
            for (MergeHistoryRecord m : mergeHistory){
                writer.write(m.getDist()+";"+m.getCnt()+"\n");
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    private static void saveMergeHistory(String expName, String distanceName, List<MergeHistoryRecord> mergeHistory){
        System.out.println("Salvo i merge history per la distanza "+ distanceName+"...");
        String filePath = D_OUTPUTS + "/"+expName+"/"+DN_HISTORIES+"/merge_history_"+distanceName+".mh";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), "utf-8"))) {
            //  f.write("%s,%s,%s,%s\n" % (str(m.c1), str(m.c2), str(m.dist), str(m.cnt)))
            for (MergeHistoryRecord m : mergeHistory){
                writer.write(m.getC1()+ "," + m.getC2()+","+m.getCres() +","+m.getDist()+","+m.getCnt()+"\n");
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

    }

    private static List<MergeHistoryRecord> loadMergeHistory(String expName, String distanceName) {
        System.out.println("Carico il merge history per la distanza "+distanceName+"...");
        List<MergeHistoryRecord> history = new ArrayList<>();

        String filePath = D_OUTPUTS + "/"+expName+"/"+DN_HISTORIES+"/merge_history_"+distanceName+".mh";
        try {
            File fileDir = new File(filePath);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fileDir), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("")) continue;
                String[] parts = line.split(",");
                history.add(new MergeHistoryRecord(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Float.parseFloat(parts[3]),
                        Integer.parseInt(parts[4])
                ));
            }
            in.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return history;
    }

    private static void saveStemmedDict(String expName, Map<String, String> dictionary, String distanceName, float threshold) {
        Object[] keys = dictionary.keySet().toArray();
        Arrays.sort(keys);
        String filePath = D_OUTPUTS + "/"+expName+"/"+DN_STEMMED_DICT+"/sd_"+distanceName+"_"+threshold+".dict";

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), "utf-8"))) {
            //  f.write("%s,%s,%s,%s\n" % (str(m.c1), str(m.c2), str(m.dist), str(m.cnt)))
            for (Object key: keys){
                String k = (String)key;
                writer.write(k +"\t" + dictionary.get(k));
                // Stando alle specifiche, l'ultima linea non deve avere il \n
                if (! k.equals(keys[keys.length-1])){
                    writer.write("\n");
                }
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2){
            System.out.println("---------------------");
            System.out.println("        YASS         ");
            System.out.println("---------------------");
            System.out.println("Per eseguire lo script è necessario fornire la descrizione di un esperimento");
            System.out.println("specificandola con il flag --file (-f)");
            System.out.println("");
            System.out.println("$ java main.class -f <experiment_file_path>");
            return;
        }
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Numero di core disponibili: "+cores);

        String propertiesFilePath = args[1];
        System.out.println("Carico esperimento da: "+ propertiesFilePath);

        // Carico il file con le proprietà
        Experiment exp = Experiment.loadFromFile(propertiesFilePath);

        makeExperimentOutputDirs(propertiesFilePath, exp.getName());

        // Carico le eventuali stopword da rimuovere
        List<String> stopwords = new ArrayList<>();
        if (!exp.getStopwordsPath().equals("") && exp.getStopwordsPath() != null) {
            try {
                File fileDir = new File(exp.getStopwordsPath());
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(fileDir), "UTF8"));
                String line;
                while ((line = in.readLine()) != null) {
                    stopwords.add(line);
                }
                in.close();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        long startTime = System.currentTimeMillis();
        // Carico il Lexicon
        System.out.println("Carico il lexicon...");
        List<String> lexicon = new ArrayList<>();
        int discardedNumbers = 0;
        int discardedStopwords = 0;
        try {
            File fileDir = new File(exp.getLexiconPath());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fileDir), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                line = line.split(",")[0];

                // Se devo scartare i numeri, scarto i numeri
                if (exp.isDiscardNumbers() && Character.isDigit(line.charAt(0))) {
                    discardedNumbers++;
                    continue;
                }

                // Se devo scartare le stopword, le scarto
                if (stopwords.contains(line)){
                    discardedStopwords++;
                    continue;
                }
                lexicon.add(line);
            }
            in.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.out.println("Caricato lexicon di "+lexicon.size()+" termini. Tempo trascorso: " + (System.currentTimeMillis() - startTime)/1000 + " s");
        System.out.println("Scartati "+discardedNumbers + " numeri e "+discardedStopwords +" stopwords");

        // Riduco il lexicon
        if (exp.getLexiconRangeStart() != -1 && exp.getLexiconRangeEnd() != -1) {
            lexicon = lexicon.subList(exp.getLexiconRangeStart(), exp.getLexiconRangeEnd());
        }

        List<MergeHistoryRecord> mergeHistory = new ArrayList<>();
        if (exp.getPipeline().contains(PIPE_CLUSTERING)) {
            System.out.println("Eseguo l'algoritmo di clustering con la misura "+exp.getDistanceMeasure().getName());

            if (exp.isSplitAllowed()){
                mergeHistory = HierarchicalClustering.calculateClustersSplitting(exp.getDistanceMeasure(), lexicon);
            } else {
                mergeHistory = HierarchicalClustering.calculateClustersSplitting(exp.getDistanceMeasure(), lexicon);
            }

            System.out.println("Completato clustering! Tempo trascorso: " + (System.currentTimeMillis() - startTime)/1000);

            if (exp.getPipeline().contains(PIPE_SAVE_GRAPH_DATA)) {
                System.out.println("Salvo i dati per il grafico");
                saveGraphData(exp.getName(), exp.getDistanceMeasure().getName(), mergeHistory);
            }

            if (exp.getPipeline().contains(PIPE_SAVE_HISTORY)) {
                System.out.println("Salvo lo storico dei merge...");
                saveMergeHistory(exp.getName(), exp.getDistanceMeasure().getName(), mergeHistory);
            }
        }

        if (exp.getPipeline().contains(PIPE_LOAD_HISTORY)){
            System.out.println("Carico lo storico dei merge...");
            mergeHistory = loadMergeHistory(exp.getName(), exp.getDistanceMeasure().getName());
        }

        if (exp.getPipeline().contains(PIPE_MERGE_HISTORY)) {
            System.out.println("Ricostruisco i cluster...");
            List<ClusterSet> snapshots = HistoryClusterBuilder.buildSetsFromHistory(lexicon, mergeHistory, exp.getThresholds());

            if (exp.getPipeline().contains(PIPE_YASS_STEMMING)) {
                for (ClusterSet cs : snapshots) {
                    Map<String, String> stemmedDict = YASS.stemFromClusterSet(cs);
                    saveStemmedDict(exp.getName(), stemmedDict, exp.getDistanceMeasure().getName(), cs.getThreshold());
                }
            }
        }

        System.out.println("Esecuzione completata. Durata totale: "+(System.currentTimeMillis() - startTime)/1000+ " s");
    }
}
