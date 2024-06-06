package DowJonesApp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class App extends Application {
    private static Queue<StockData> stockQueue = new LinkedList<>();
    private static final Dotenv dotenv = Dotenv.configure().directory("./").load();
    private static final String API_KEY = dotenv.get("ALPHA_VANTAGE_API_KEY");
    private static final String SYMBOL = "DIS";
    private static final String FUNCTION = "TIME_SERIES_DAILY";
    private static final String OUTPUTSIZE = "compact";
    private static final String URL = String.format(
            "https://www.alphavantage.co/query?function=%s&symbol=%s&outputsize=%s&apikey=%s",
            FUNCTION, SYMBOL, OUTPUTSIZE, API_KEY);

    private LineChart<Number, Number> lineChart;
    private XYChart.Series<Number, Number> series;
    private int timeCounter = 0;  // Counter for the x-axis values

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Create the axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Stock Price");

        // Create the line chart
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Stock Monitoring, " + SYMBOL);

        // Create the series to hold data
        series = new XYChart.Series<>();
        series.setName("Daily Closing Prices");

        // Add series to chart
        lineChart.getData().add(series);

        // Create the scene and set the stage
        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Stock Price Monitoring Application");
        stage.show();

        // Start the timer task to fetch stock data
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("API key is not set. Please set the ALPHA_VANTAGE_API_KEY environment variable.");
            return;
        }

        Timer timer = new Timer();
        timer.schedule(new StockPriceFetcher(this), 0, 86400000); // 86400000 ms = 24 hours
    }

    private void updateGraph(StockData stockData) {
        Platform.runLater(() -> {
            series.getData().add(new XYChart.Data<>(timeCounter++, stockData.getPrice()));
        });
    }

    static class StockPriceFetcher extends TimerTask {
        private final App app;

        public StockPriceFetcher(App app) {
            this.app = app;
        }

        @Override
        public void run() {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(URL))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    System.err.println("Error: Received non-200 response code: " + response.statusCode());
                    return;
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode timeSeries = root.path("Time Series (Daily)");

                if (timeSeries.isMissingNode()) {
                    System.err.println("No data available for symbol: " + SYMBOL);
                    return;
                }

                String latestTimestamp = timeSeries.fieldNames().next();
                JsonNode latestData = timeSeries.path(latestTimestamp);
                double price = latestData.path("4. close").asDouble();
                long timestamp = System.currentTimeMillis();

                StockData stockData = new StockData(price, timestamp);
                stockQueue.add(stockData);

                // Update graph with new data
                app.updateGraph(stockData);

                System.out.println("Stock Price: " + price + " at " + timestamp);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class StockData {
        private final double price;
        private final long timestamp;

        public StockData(double price, long timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }

        public double getPrice() {
            return price;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
