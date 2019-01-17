package memorymonitor;



// *** JAVA FX IMPORTS ***

import javafx.application.Application;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.stage.Stage;

// for memory monitor //

import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MemoryMonitor<E extends Comparable<E>> extends Application //implements Runnable
{
    private static Stage window;

    /** Testing MemoryChartPane **/
    private static Stage chartWindow;
    private static Scene chartScene;
    private static BorderPane chartPane;


    // *** Java FX UI *** STAGE ***
    @Override
    public void start(Stage primaryStage)
    {
        //    window = primaryStage;

        chartWindow = new Stage();



        // create & show the memory monitor in another window
        buildChartWindow();

        // For the application window
        //    window.setScene(getFirstScene());
        //    window.setTitle("FlashMonkey");
        //    window.show();

    }

    /**
     *  First Scene returns the ... first application scene
     *   - Currently the first scene is ...
     *
     * @return Returns a scene
     */
    public static Scene getFirstScene()
    {
        Scene firstScene;
        BorderPane firstPane = new BorderPane();
        GridPane btnBox = new GridPane();

        // For the lower panel on initial window
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setPercentWidth(50);
        btnBox.getColumnConstraints().add(col0);
        btnBox.setId("buttonBox");

        btnBox.setPadding(new Insets(15, 15, 15, 15));

        firstPane.setBottom(getExitBox());
        firstScene = new Scene(firstPane, 300, 600);

        return firstScene;
    }


    /**
     * DESCRIPTION: Creates an HBox with the menu and exit buttons.
     * @return HBox
     */
    public static GridPane getExitBox()
    {
        GridPane buttonBox = new GridPane(); // HBox with spacing provided

        Button menuButton = new Button("Menu");

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setPercentWidth(50);
        buttonBox.setId("buttonBox");
        buttonBox.getColumnConstraints().add(col0);
        buttonBox.setPadding(new Insets(15, 15, 15, 15));
        buttonBox.addColumn(1, menuButton);

        return buttonBox;
    }



    protected void buildChartWindow()
    {

        System.out.println("*^*^* BuidTreeWindow called *^*^*");

        chartPane = createMemoryMonitor();

        chartScene = new Scene(chartPane);
        chartWindow.setTitle("MemoryMonitor");
        chartWindow.setScene(chartScene);
        chartWindow.show();
    }

    int i = 0;
    Long[] changeAry = new Long[360];
    int size = 0;
    int count = 1;
    Long oldUsedMemory = 0l;
    long calculatedAvgGrowth = 8661; // Start with this number

    /**
     * Attempts to create a bias for this monitor. This monitor uses memory, thus quantifying the amount of memory used
     * by the monitor is important for understanding memory used by the host application.
     * @return
     */
    private Long avgMemoryConsumed() {

        Long sum = 0l;
        Long bias = 0l;

        // removing the first and last elements
        for(int i = 1; i < size; i++) {

            if (changeAry[i] > 10)
            {
                sum += changeAry[i];
                count++;

            } else { // used for debugging
                //System.out.println("Mem used is < 10");
                //System.out.println(changeAry[i]);
            }
        }

        sum /= count;
        bias = sum - calculatedAvgGrowth;
        /**
         * Once the monitors avg growth is calculated. Uncomment below and return change.
         * the second line calculatedAvgGrowth ... may be commented out once it is quantified.
         * The amount that is quantified should be used in a constant, if you believe it is stable.
         */
        //change = sum - calculatedAvgGrowth;
        calculatedAvgGrowth = sum;

        System.out.println("Sum = " + sum + "\n\n");
        //return change;
        return bias;
    }

    /**
     * Creates and displays the memory monitor. Note that the
     * @return
     */
    private BorderPane createMemoryMonitor() {

        // For estimating the total memory consumed by the host and this monitor
        LongProperty totalMemory = new SimpleLongProperty(Runtime.getRuntime().totalMemory());
        LongProperty freeMemory  = new SimpleLongProperty(Runtime.getRuntime().freeMemory());
        LongProperty maxMemory   = new SimpleLongProperty(Runtime.getRuntime().maxMemory());
        NumberBinding usedMemory = totalMemory.subtract(freeMemory);
        // for estimating the amount of memory consumed by this application.
        LongProperty memoryUse   = new SimpleLongProperty();

        Label usedMemoryLabel = new Label();
        usedMemoryLabel.textProperty().bind(usedMemory.asString("Used memory: %,d "));
        Label freeMemoryLabel = new Label();
        freeMemoryLabel.textProperty().bind(freeMemory.asString("Free memory: %,d"));
        Label totalMemoryLabel = new Label();
        totalMemoryLabel.textProperty().bind(totalMemory.asString("Total memory: %,d"));
        Label maxMemoryLabel = new Label();
        maxMemoryLabel.textProperty().bind(maxMemory.asString("Max memory: %,d"));

        Label memoryUseLabel = new Label();
        memoryUseLabel.textProperty().bind(memoryUse.asString("Avg Memory Consumed: %,d"));

        Series<Number, Number> series = new Series<>();
        series.setName("Used memory");

        AtomicInteger time = new AtomicInteger();

        Timeline updateMemory = new Timeline(new KeyFrame(Duration.seconds(1), event -> {

            totalMemory.set(Runtime.getRuntime().totalMemory());
            freeMemory.set(Runtime.getRuntime().freeMemory());
            maxMemory.set(Runtime.getRuntime().maxMemory());
            series.getData().add(new Data<>(time.incrementAndGet(), usedMemory.getValue()));
            if (series.getData().size() > 100) {
                series.getData().subList(0, series.getData().size() - 100).clear();
            }

            //System.out.println(" usedMemory " + usedMemory.longValue());
            //System.out.println(" oldUsedMemory " + oldUsedMemory);
            //System.out.println(usedMemory.longValue() - oldUsedMemory);

            changeAry[i] = usedMemory.longValue() - oldUsedMemory;
            //System.out.println("memoryUsed[i] = " + changeAry[i]);

            // When the GC collects memory used is less than 0
            // Remove cycles that are 0 or less for a more accurate
            // understanding of this monitors average use.
            if(i > 0) {
                memoryUse.set(avgMemoryConsumed());
            }
            i++;
            size++;
            oldUsedMemory = usedMemory.longValue();
        }));

        // Seconds ... or number of cycles to test
        updateMemory.setCycleCount(359);
        updateMemory.play();

        // Display the labels above the chart
        VBox labels = new VBox(usedMemoryLabel, freeMemoryLabel, totalMemoryLabel, maxMemoryLabel, memoryUseLabel);
        labels.setAlignment(Pos.CENTER);

        // The chart
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        NumberAxis yAxsis = new NumberAxis();
        yAxsis.setLabel("Memory");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxsis);
        chart.setAnimated(false);
        chart.getData().add(series);

        BorderPane chartPane = new BorderPane(chart, labels, null, null, null);

        return chartPane;
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
