# Stock Price Monitoring Application

This project, developed for the Citi Technology Software Development Job Simulation on Forage, is a JavaFX application that fetches and displays the daily closing prices of the Walt Disney Company (DIS) stock. Originally intended to utilise the Yahoo! Finance API, the application now integrates with the Alpha Vantage API for data retrieval.

## Overview

The Stock Price Monitoring Application offers a visual representation of daily stock prices for the Walt Disney Company. Leveraging JavaFX, the application displays stock prices on a dynamic chart, updating in real-time with the latest data from the [Alpha Vantage API](https://www.alphavantage.co).

## Features

- **Daily Updates**: Automatically fetches and displays daily closing prices for DIS stock.
- **Graphical Representation**: Utilises a chart to visualise stock price trends over time.
- **Real-time Data**: Updates the chart with new data every 24 hours.
- **Secure API Management**: Manages the Alpha Vantage API key securely using the `dotenv` library.

## Usage

This application is designed for demonstration purposes and is not intended for public deployment. The API key is securely managed and is not provided in this repository.

## Technology Stack

- **JavaFX**: For creating a responsive and interactive graphical user interface.
- **Alpha Vantage API**: For fetching daily stock prices (originally intended for the Yahoo! Finance API).
- **Jackson**: For efficient parsing of JSON data.
- **dotenv**: For secure management of environment variables.

## Acknowledgements

- **Citi Technology**: For providing the simulation framework and opportunity.
- **Forage**: For facilitating the job simulation programme.

## Code Highlights

- **Modern Java Features**: Utilises the `HttpClient` class for HTTP requests and responses.
- **Concurrency Handling**: Ensures real-time updates with the `Timer` class for scheduling tasks.
- **Thread Safety**: Employs `Platform.runLater()` to safely update the JavaFX UI from background threads.
- **Error Handling**: Implements effective error handling for API response status and data parsing.

## Future Enhancements

- **Enhanced Error Handling**: Improved granularity in exception handling and error messages.
- **Extended Data Visualisation**: Additional charts and analytics for deeper insights.
- **User Interface Improvements**: More interactive and user-friendly UI elements.
