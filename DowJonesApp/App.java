package DowJonesApp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class App {
    private static Queue<StockData> stockQueue = new LinkedList<>();
    private static final Dotenv dotenv = Dotenv.configure().directory("./").load();
    private static final String API_KEY = dotenv.get("ALPHA_VANTAGE_API_KEY");
    private static final String SYMBOL = "DIS";
    private static final String FUNCTION = "TIME_SERIES_DAILY";
    private static final String OUTPUTSIZE = "compact";
    private static final String URL = String.format(
            "https://www.alphavantage.co/query?function=%s&symbol=%s&outputsize=%s&apikey=%s",
            FUNCTION, SYMBOL, OUTPUTSIZE, API_KEY);

    public static void main(String[] args) {
        try {
            // Print all environment variables to verify
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

            // Print the API key to verify it's being read
            System.out.println("API_KEY: " + API_KEY);

            if (API_KEY == null || API_KEY.isEmpty()) {
                System.err.println("API key is not set. Please set the ALPHA_VANTAGE_API_KEY environment variable.");
                return;
            }

            Timer timer = new Timer();
            timer.schedule(new StockPriceFetcher(), 0, 86400000); // 86400000 ms = 24 hours
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class StockPriceFetcher extends TimerTask {
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

                stockQueue.add(new StockData(price, timestamp));
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
