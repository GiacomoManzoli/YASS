package bm.yass;

/**
 * Interfaccia per le distanze del paper YASS
 * */
public interface DistanceMeasure {
    float calculate(String w1, String w2);
    String getName();
}
