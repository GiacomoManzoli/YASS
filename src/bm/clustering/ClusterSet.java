package bm.clustering;

import java.util.*;


public class ClusterSet {

    private Map<Integer, Cluster> dict;
    private float threshold;

    ClusterSet(float threshold) {
        this.dict = new HashMap<>();
        this.threshold = threshold;
    }

    public float getThreshold() {
        return threshold;
    }

    void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    void addCluster(Cluster cluster){
        dict.put(cluster.getId(), cluster);
    }

    void removeCluster(int clusterId) {
        if (dict.keySet().contains(clusterId)) {
            dict.remove(clusterId);
        }
    }

    public Cluster getCluster(int clusterId) {
        return dict.get(clusterId);
    }

    public Set<Integer> getClustersId() {
        return dict.keySet();
    }

    ClusterSet copy() {
        ClusterSet newCopy = new ClusterSet(threshold);

        for (Integer key : dict.keySet()) {
            Cluster newCluster = new Cluster( dict.get(key).getId(), new ArrayList<>(dict.get(key).getWords()));
            newCopy.addCluster(newCluster);
        }

        return newCopy;
    }
}
