import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.*;
import java.util.List;

public class Main extends Application {
    static final int N = 2000;
    static final int EXPERIMENTS = 10;
    static final int[] CACHE_SIZE = {20, 50, 70, 100, 200};
    static List<Double> averageRandHitRateList;
    static List<Double> averageOptHitRateList;

    //(i):
    static List<Integer> generate_80_20ReadWorkload() {
        List<Integer> _80_20readWorkload = new ArrayList<>();

        int top20Pages = (int) (N * 0.2);
        int remaining80Pages = N - top20Pages;

        Random random = new Random();

        for (int i = 0; i < 800; i++) {
            _80_20readWorkload.add(random.nextInt(top20Pages));
        }

        for (int i = 0; i < 200; i++) {
            _80_20readWorkload.add(random.nextInt(remaining80Pages) + top20Pages);
        }

        return _80_20readWorkload;
    }

    static double randCache(List<Integer> workload, int cacheSize){
        Set<Integer> randCache = new HashSet<>();

        Random random = new Random();

        double hitRate;
        double hits = 0;
        double misses = 0;

        for (int page : workload) {
            if (!randCache.contains(page)) {
                misses++;
                if (randCache.size() >= cacheSize) {
                    int randomPage = new ArrayList<>(randCache).get(random.nextInt(randCache.size()));
                    randCache.remove(randomPage);
                }
                randCache.add(page);
            }
            else{
                hits++;
            }
        }

        hitRate = hits / (hits + misses);
        return hitRate;
    }

    static double optCache(List<Integer> workload, int cacheSize){
        Set<Integer> optCache = new HashSet<>();

        double hitRate;
        double hits = 0;
        double misses = 0;

        for (int i = 0; i < workload.size(); i++) {
            int page = workload.get(i);
            if (!optCache.contains(page)) {
                misses++;
                if (optCache.size() >= cacheSize) {
                    int farthestPage = getGreatestForwardDistancePage(workload, optCache, i);
                    optCache.remove(farthestPage);
                }
                optCache.add(page);
            }
            else{
                hits++;
            }
        }

        hitRate = hits / (hits + misses);
        return hitRate;
    }

    static int getGreatestForwardDistancePage(List<Integer> workload, Set<Integer> cache, int i){
        int farthestPage = -1;
        int farthestDistance = -1;

        for (int cachePage : cache) {
            int nextIndex = workload.subList(i, workload.size()).indexOf(cachePage);

            if (nextIndex == -1) {
                farthestPage = cachePage;
                break;
            }
            if (nextIndex > farthestDistance) {
                farthestPage = cachePage;
                farthestDistance = nextIndex;
            }
        }
        return farthestPage;
    }

    public static void main(String[] args) {
        averageRandHitRateList = new ArrayList<>();
        averageOptHitRateList = new ArrayList<>();

        //(ii):
        for(int cacheSize: CACHE_SIZE) {
            //(ii.1):
            double sumRandHitRate = 0;
            double sumOptHitRate = 0;

            System.out.println("Cache Size: " + cacheSize);
            System.out.println("-------------------------");

            for (int i = 0; i < EXPERIMENTS; i++) {
                List<Integer> _80_20readWorkload = generate_80_20ReadWorkload();

                //(ii.2):
                double randHitRate = randCache(_80_20readWorkload, cacheSize);
                double optHitRate = optCache(_80_20readWorkload, cacheSize);

                System.out.println("Experiment: " + (int)(i+1));
                System.out.println("-------------------------");
                System.out.println("RAND hit rate: " + randHitRate);
                System.out.println("OPT hit rate: " + optHitRate);
                System.out.println("-------------------------");

                sumRandHitRate = sumRandHitRate + randHitRate;
                sumOptHitRate = sumOptHitRate + optHitRate;
            }
            //(ii.3):
            double averageRandHitRate = sumRandHitRate / EXPERIMENTS;
            double averageOptHitRate = sumOptHitRate / EXPERIMENTS;

            System.out.println("RAND average hit rate: " + averageRandHitRate);
            System.out.println("OPT average hit rate: " + averageOptHitRate);

            averageRandHitRateList.add(averageRandHitRate);
            averageOptHitRateList.add(averageOptHitRate);

            System.out.println("-------------------------");
            System.out.println("-------------------------");
        }

        launch(args);
    }

    //(iii):
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Average Hit Rate vs Cache Size");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Cache Size");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Average Hit Rate");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Average Hit Rate vs Cache Size");

        XYChart.Series<Number, Number> randSeries = new XYChart.Series<>();
        randSeries.setName("RAND");

        XYChart.Series<Number, Number> optSeries = new XYChart.Series<>();
        optSeries.setName("OPT");

        int i = 0;
        for(int cacheSize: CACHE_SIZE){
            randSeries.getData().add(new XYChart.Data<>(cacheSize, averageRandHitRateList.get(i)));
            optSeries.getData().add(new XYChart.Data<>(cacheSize, averageOptHitRateList.get(i)));

            i++;
        }

        lineChart.getData().addAll(randSeries, optSeries);

        Scene scene = new Scene(lineChart, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}