package bm.clustering;


import java.util.ArrayList;
import java.util.List;

public class MyCustomBigArray {

    private static final int ARRAY_SIZE = 10;
    //private static final int ARRAY_SIZE = Integer.MAX_VALUE/2;

    private long size;
    private int arraysCount;
    private List<float[]> arrays;

    public MyCustomBigArray(long size){
        this.size = size;
        arraysCount = (int)Math.ceil(size / (double)ARRAY_SIZE);
        arrays = new ArrayList<>();
        // creo n-1 array di dimensione MAX_VALUE e l'ultimo puù piccolo
        for (int i = 0; i < arraysCount-1; i++) {
            arrays.add(new float[ARRAY_SIZE]);
        }
        int reminder = (int)(size - (arraysCount-1)* ARRAY_SIZE);
        arrays.add(new float[reminder]);
    }

    public void set(long index, float value) {
        int arrayIndex = (int) (index/ARRAY_SIZE);
        int innerIndex = (int) (index - ((long)arrayIndex * ARRAY_SIZE));

        try {
            arrays.get(arrayIndex)[innerIndex] = value;
        } catch (Exception e) {
            System.err.println(index);
        }
    }

    public float get(long index) {
        int arrayIndex = (int) (index/ARRAY_SIZE);
        int innerIndex = (int) (index - ((long)arrayIndex * ARRAY_SIZE));
        try{
            return arrays.get(arrayIndex)[innerIndex];
        } catch (Exception e){
            System.err.println("A");
            return 0;
        }
    }

    public void resize(long newSize) {
        // Se ci sono degli array extra li butta via
        int newArraysCount = (int)Math.ceil(newSize / (double)ARRAY_SIZE);
        if (newArraysCount < arraysCount) {
            arrays = arrays.subList(0, newArraysCount);
            assert arrays.size() == newArraysCount;
            arraysCount = newArraysCount;
            size = newSize;
        }
    }

    public long getSize() {
        return size;
    }
}
