package bm.clustering;

import bm.yass.DistanceMeasure;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;


public class ClusterManager {

    static ForkJoinPool commonPool = new ForkJoinPool(); // Usa tutti i core possibili

    private List<Cluster> clusters;
    private DistanceMeasure d;
    float[] dist;


    /* Formule per la conversione degli indici (occhio che la matrice è flippata)
    * i = n - 2 - floor(sqrt(-8*k + 4*n*(n-1)-7)/2.0 - 0.5)
    * j = k + i + 1 - n*(n-1)/2 + (n-i)*((n-i)-1)/2
    * k = (n*(n-1)/2) - (n-i)*((n-i)-1)/2 + j - i - 1
    * */
    int _k(int i, int j){
        int n = clusters.size();
        int k = (n *(n-1))/2 - ( (n-i)*(n-i-1) )/2 + j - i - 1;
        k = ( n*(n-1) )/2 - 1 - k;
        return k;
    }

    int _i(int k){
        int n = clusters.size();
        k = ( n*(n-1) )/2 - 1 - k; // ritrasformo k
        int i = n - 2 - (int)Math.floor(Math.sqrt(-8*k + 4*n*(n-1)-7)/2 - 0.5);
        if (i == -1){
            int x= 2;
        }
        return  i; // i
    }

    int _j(int k){
        int n = clusters.size();
        int i = _i(k); // _i trasforma k, quindi devo calcolarlo prima
        k = (n*(n-1))/2 - 1 - k; // ritrasformo k
        return k + i + 1 - (n*(n-1))/2 + ((n-i)*((n-i)-1))/2;// j
    }



    public ClusterManager(List<Cluster> clusters, DistanceMeasure d) {
        this.clusters = clusters;
        this.d = d;
        int n = clusters.size();

        int tot = (n*(n-1))/2;
        dist = new float[tot];

        System.out.println("Creo la matrice delle distanze...");
        int printInterval = (int)Math.max(100, tot*0.05);
        int cnt = 0;
        double currentMin = 10;
        long startTime = System.currentTimeMillis();

        BuildDistanceMatrixTask.buildDistanceMatrix(this, d);
        System.out.println("Fine creazione matrice. Tempo necessario "+(System.currentTimeMillis()-startTime)/1000 +" s");
    }

    void deleteClusters(int r, int s){
        // r < s
        // Calcolo il numero di indici che devo cancellare
        // Per ogni indice che cancello tutte le coppie in cui compare.
        // Potenzialmente sono 2n, ma so che sono n, perché le coppie sono ordinate.
        // -1 perché (r,s) è contanta 2 volte in n
        int n = clusters.size();
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
    }

    void insert(Cluster cluster){
        // Non serve modificare la dimensione del vettore, vado ad occupare lo spazio garbage del vettore.
        clusters.add(0, cluster);
        int n = clusters.size();

        // Calcolo le nuove distanze
        int i = 0;  // Ho inserito il cluster in testa, ha indice 0
        for (int j = i+1; j < n; j++) {
            int k = _k(i, j);
            dist[k] = clusters.get(i).distance(clusters.get(j), d);
        }
    }

    public MinDistancePair findMinDistancePair() {
        return FindMinDistancePairTask.findMinDistancePair(this);
    }

    int size() {
        return clusters.size();
    }

    Cluster getCluster(int i){
        return clusters.get(i);
    }
}
