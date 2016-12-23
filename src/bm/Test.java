package bm;

import bm.clustering.*;
import bm.yass.DistanceManager;
import bm.yass.DistanceMeasure;
import bm.yass.Experiment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Test {


    private static final int N = 654230;

    static private long _k(int i, int j){
        long n = N;
        long k = (n *(n-1))/2 - ( (n-i)*(n-i-1) )/2 + j - i - 1;
        k = ( n*(n-1) )/2 - 1 - k;
        return k;
    }

    static private int _i(long k){
        long n = N;
        k = ( n*(n-1) )/2 - 1 - k; // ritrasformo k
        long i = n - 2 - (int)Math.floor(Math.sqrt(-8*k + 4*n*(n-1)-7)/2 - 0.5);
        return  (int)i; // i
    }

    static private int _j(long k){
        long n = N;
        int i = _i(k); // _i trasforma k, quindi devo calcolarlo prima
        k = (n*(n-1))/2 - 1 - k; // ritrasformo k
        long j =  (k + i + 1 - (n*(n-1))/2 + ((n-i)*((n-i)-1))/2); // j
        return (int)j;
    }


    public static void main(String[] args) {
        List<String> lines = new ArrayList<>();
        long tot = ((long)N*(N-1))/2;
        for (int i = 0; i < Integer.MAX_VALUE; i++){
            long k = ThreadLocalRandom.current().nextLong(0, tot);
            int i1 = _i(k);
            int j1 = _j(k);
            long k2 = _k(i1,j1);

            long tot2 = (i1*(i1-1))/2;

            try {
                assert k2 == k;
                assert tot2 > 0;
            } catch (Exception e) {
                System.out.println("HAHA");
            }
            if (i % 5000 == 0){
                System.out.println(""+i+" iterazioni senza incidenti");
            }
        }

        /*
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
