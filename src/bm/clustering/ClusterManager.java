package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by gmanzoli on 17/12/16.
 */
class ClusterManager {

    private List<Cluster> clusters;
    private DistanceMeasure d;
    private int n;
    private float[] dist;

    private int _k(int i, int j){
        int k = (n*(n-1)/2) - (n-i)*((n-i)-1)/2 + j - i - 1;
        k = (n*(n-1)/2) - 1 - k;
        return k;
    }

    ClusterManager(List<Cluster> clusters, DistanceMeasure d) {
        this.clusters = clusters;
        this.d = d;
        this.n = clusters.size();

        int tot = (n*(n-1))/2;
        dist = new float[tot];

        System.out.println("Creo la matrice delle distanze...");
        int printInterval = (int)Math.max(100, tot*0.05);
        int cnt = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j<n; j++) {
                int k = _k(i,j);
                dist[k] = clusters.get(i).distance(clusters.get(j), this.d);
                cnt += 1;
                if (cnt % (printInterval)  == 0) {
                    System.out.println("Creati " + cnt + " nodi su "+ tot + " - Tempo trascorso: "+ (System.currentTimeMillis() - startTime)/1000 + "s");
                }
            }
        }
        System.out.println("Fine creazione matrice");
    }

    void deleteClusters(int r, int s){
        // r < s
        // Calcolo il numero di indici che devo cancellare
        // Per ogni indice che cancello tutte le coppie in cui compare.
        // Potenzialmente sono 2n, ma so che sono n, perché le coppie sono ordinate.
        // -1 perché (r,s) è contanta 2 volte in n
        int[] toDelete = new int[2*(n-1) -1]; // una coppia è presente 2 volte
        int toDeleteCnt = 0;
        // vale sempre r < s
        // calcolo le coppie del tipo (*,r)
        for (int i = 0; i < r; i++) {
            int index = _k(i,r);
            if (index >= 0 && index < dist.length){
                toDelete[toDeleteCnt] = index;
                toDeleteCnt++;
            }
        }
        // calcolo le coppie del tipo (r,*) (c'è (r,s))
        // sono consecutive e ce ne sono n-r-1
        for (int j = r+1; j < r+1+(n-r-1); j++) {
            int index = _k(r,j);
            if (index >= 0 && index < dist.length){
                toDelete[toDeleteCnt] = index;
                toDeleteCnt++;
            }
        }
        // calcolo le coppie del tipo (*,s)
        for (int i = 0; i < s; i++) {
            if (i == r)// # la coppia (r,s) l'ho già contata
                continue;
            int index = _k(i,s);
            if (index >= 0 && index < dist.length){
                toDelete[toDeleteCnt] = index;
                toDeleteCnt++;
            }
        }
        // calcolo le coppie del tipo (s,*) (c'è (r,s))
        // sono consecutive e ce ne sono n-r-1
        for (int j = s+1; j < s+1+(n-s-1); j++) {
            int index = _k(s,j);
            if (index >= 0 && index < dist.length){
                toDelete[toDeleteCnt] = index;
                toDeleteCnt++;
            }
        }
        // Ordino gli indici da cancellare in ordine decrescente
        Arrays.sort(toDelete);
        // Sovrascrivo i valori da cancellare compattando il vettore.
        // Da notare la dimensione in memoria del vettore non decresce, questo per evitare di dover farne una copia.
        // Un possibile miglioramento può essere che quando la parte garbage è tanto grande, si può effettuare il resize
        int tot = (n*(n-1))/2;
        int cntDeleted = 0;

        for (int it = 0; it < tot; it++) {
            // Se non ho ancora cancellato niente e non
            // devo cancellare l'indice corrente, passo all'elemento successivo
            if (cntDeleted == 0 && it != toDelete[cntDeleted]) { continue; }

            if (cntDeleted < toDelete.length && it == toDelete[cntDeleted])
                cntDeleted += 1;

            // Prima di copiare il prossimo indice, controllo di non copiare
            // un indice che poi deve essere cancellato
            while (cntDeleted < toDelete.length && it + cntDeleted == toDelete[cntDeleted])
                cntDeleted += 1;

            if (it + cntDeleted < tot)
                dist[it] = dist[it + cntDeleted];
            else
                break;
        }

        clusters.remove(r);
        clusters.remove(s -1);
        this.n = clusters.size();
    }

    void insert(Cluster cluster){
        // Non serve modificare la dimensione del vettore, vado ad occupare lo spazio garbage del vettore.
        clusters.add(0, cluster);
        this.n = clusters.size();

        // Calcolo le nuove distanze
        int i = 0;  // Ho inserito il cluster in testa, ha indice 0
        for (int j = i+1; j < n; j++) {
            int k = _k(i, j);
            dist[k] = clusters.get(i).distance(clusters.get(j), d);
        }
    }

    MinDistancePair findMinDistancePair() {
        float minDist = Float.POSITIVE_INFINITY;
        int r = -1;
        int s = -1;  // Coppia di cluster a distanza minima

        for (int i = 0; i < n; i++){ // non considero la digonale
            for (int j = i + 1; j < n; j++){
                int k = _k(i,j);
                if (dist[k] <= minDist){ // <= perché la distanza può essere infinita!
                    minDist = dist[k];
                    r = i;
                    s = j;
                }
            }
        }

        // Devo stare attento all'ordine in cui cancello i dati
        // perché dopo che ho tolto un elemento l'indice dell'altro cambia.
        // assum0 che r sia l'indice minore
        if (s < r) {
            int t = r;
            r = s;
            s = t;
        }
        return new MinDistancePair(r,s, minDist);
    }

    int size() {
        return clusters.size();
    }

    Cluster getCluster(int i){
        return clusters.get(i);
    }
}
