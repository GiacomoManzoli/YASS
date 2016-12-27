package bm.yass;



public class DistanceManager {

    private static float INFINITY = Float.POSITIVE_INFINITY;

    private static int p(String x, String y, int i){
        int minLen = Math.min(x.length(), y.length());
        if (i >= minLen){
            return 1;
        }
        if (x.charAt(i) == y.charAt(i)) {
            return 0;
        }
        return 1;
    }

    private static int firstMismatch(String x, String y){
        int minLen = Math.min(x.length(), y.length());
        for (int i = 0; i < minLen; i++) {
            if (x.charAt(i) != y.charAt(i)){
                return i;
            }
        }
        return minLen;
    }

    public static DistanceMeasure d1(){
        return new DistanceMeasure() {
            @Override
            public float calculate(String w1, String w2) {
                int maxLen = Math.max(w1.length(), w2.length());
                float d = 0;
                for (int i = 0; i < maxLen; i++) {
                    d += p(w1,w2,i) / Math.pow(2, i);
                }
                return d;
            }
            @Override
            public String getName() {
                return "d1";
            }
        };
    }

    public static DistanceMeasure d2(){
        return new DistanceMeasure() {
            @Override
            public float calculate(String w1, String w2) {
                int maxLen = Math.max(w1.length(), w2.length());
                float d = 0;
                int m = firstMismatch(w1,w2);

                if (m == 0){ return INFINITY;}
                for (int i = m; i < maxLen; i++) {
                    d += 1 / Math.pow(2,i-m);
                }

                return d / (float)m;
            }
            @Override
            public String getName() {
                return "d2";
            }
        };
    }

    public static DistanceMeasure d3(){
        return new DistanceMeasure() {
            @Override
            public float calculate(String w1, String w2) {
                int maxLen = Math.max(w1.length(), w2.length());
                float d = 0;
                int n = maxLen -1;
                int m = firstMismatch(w1,w2);

                if (m == 0){ return INFINITY;}
                for (int i = m; i < maxLen; i++) {
                    d += 1 / Math.pow(2,i-m);
                }

                return (d * (n-m+1)) / (float)m;
            }
            @Override
            public String getName() {
                return "d3";
            }
        };
    }

    public static DistanceMeasure d4(){
        return new DistanceMeasure() {
            @Override
            public float calculate(String w1, String w2) {
                int maxLen = Math.max(w1.length(), w2.length());
                float d = 0;
                int m = firstMismatch(w1,w2);
                int n = maxLen - 1;

                if (m == 0){ return INFINITY;}
                for (int i = m; i < maxLen; i++) {
                    d += 1 / Math.pow(2,i-m);
                }

                return (d * (n-m+1)) / (float)(n+1);
            }

            @Override
            public String getName() {
                return "d4";
            }
        };
    }
}
