package bm.yass;

import bm.clustering.Cluster;
import bm.clustering.ClusterSet;

import java.util.HashMap;
import java.util.Map;


public class YASS {

    public static Map<String, String> stemFromClusterSet(ClusterSet clusters){
        Map<String, String> stemmedDict = new HashMap<>();

        for (Integer clusterId : clusters.getClustersId()) {
            Cluster cluster = clusters.getCluster(clusterId);
            for (String w : cluster.getWords()){
                stemmedDict.put(w, cluster.getCentralWord());
            }
        }
        return stemmedDict;
    }
}
