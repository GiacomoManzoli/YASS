package bm.yass;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Classe che rappresenta un esperimento.
 * */
public class Experiment{

    // Chiavi per il file JSON
    private static final String P_NAME = "experiment.name";
    private static final String P_LEXICON_PATH = "experiment.lexicon_path";
    private static final String P_PIPELINE = "experiment.pipeline";
    private static final String P_LEXICON_RANGE_START = "experiment.lexicon_range.start";
    private static final String P_LEXICON_RANGE_END = "experiment.lexicon_range.end";
    private static final String P_DISTANCE = "experiment.distance";
    private static final String P_DISTANCE_THRESHOLDS = "experiment.thresholds";
    private static final String P_DISCARD_NUMBERS = "experiment.discard_numbers";
    private static final String P_STOPWORDS_PATH = "experiment.stopwords";
    private static final String P_ALLOW_SPLIT = "experiment.allow_split";
    private static final String P_TERRIER_LEXICON = "experiment.terrier_lexicon";

    public static Experiment loadFromFile(String filePath){
        Experiment e = new Experiment();

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(filePath);

            // load a properties file
            prop.load(input);

            e.name = prop.getProperty(P_NAME);
            e.lexiconPath = prop.getProperty(P_LEXICON_PATH);
            e.lexiconRangeStart = Integer.parseInt(prop.getProperty(P_LEXICON_RANGE_START, "-1"));
            e.lexiconRangeEnd = Integer.parseInt(prop.getProperty(P_LEXICON_RANGE_END, "-1"));
            e.stopwordsPath = prop.getProperty(P_STOPWORDS_PATH);
            e.discardNumbers = Boolean.parseBoolean(prop.getProperty(P_DISCARD_NUMBERS));
            e.allowSplit = Boolean.parseBoolean(prop.getProperty(P_ALLOW_SPLIT));
            e.terrierLexicon = Boolean.parseBoolean(prop.getProperty(P_TERRIER_LEXICON));

            String distanceName = prop.getProperty(P_DISTANCE);
            Method distanceCreator = DistanceManager.class.getMethod(distanceName);
            e.distanceMeasure = (DistanceMeasure) distanceCreator.invoke(null);

            String temp = prop.getProperty(P_DISTANCE_THRESHOLDS);
            String[] t2 = temp.split(",");
            float[] thresholds = new float[t2.length];
            for (int i = 0; i < thresholds.length; i++) {
                thresholds[i] = Float.parseFloat(t2[i].trim());
            }
            e.thresholds = thresholds;

            temp = prop.getProperty(P_PIPELINE);
            t2 = temp.split(",");
            e.pipeline = new ArrayList<>();
            for (String aT2 : t2) {
                e.pipeline.add(aT2.trim());
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }

        return e;
    }


    private String name;
    private String lexiconPath;
    private List<String> pipeline;
    private int lexiconRangeStart;
    private int lexiconRangeEnd;
    private DistanceMeasure distanceMeasure;
    private float[] thresholds;
    private boolean discardNumbers;
    private String stopwordsPath;
    private boolean allowSplit;
    private boolean terrierLexicon;


    public String getName() {
        return name;
    }

    public String getLexiconPath() {
        return lexiconPath;
    }

    public List<String> getPipeline() {
        return pipeline;
    }

    public int getLexiconRangeStart() {
        return lexiconRangeStart;
    }

    public int getLexiconRangeEnd() {
        return lexiconRangeEnd;
    }

    public DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    public float[] getThresholds() {
        return thresholds;
    }

    public boolean isDiscardNumbers() {
        return discardNumbers;
    }

    public String getStopwordsPath() {
        return stopwordsPath;
    }

    public boolean isSplitAllowed() {
        return allowSplit;
    }

    public boolean isTerrierLexicon() {
        return terrierLexicon;
    }
}
