package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Main application class for ReCHor journey planner.
 * <p>
 * This class integrates all UI components (query interface, journey summary, and journey details)
 * and handles the journey search logic.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public class Main extends Application {

    private ObservableValue<List<Journey>> journeysObservable;
    private TimeTable timeTable;
    private Router router;
    private Profile lastProfile;
    private String lastDestination;
    private LocalDate lastDate;

    /**
     * The entry point of the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the application and constructs the user interface.
     *
     * @param primaryStage the primary stage for the application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load timetable data
            timeTable = FileTimeTable.in(Path.of("timetable/timetable_18"));
            router = new Router(timeTable);

            // Create stop index for search functionality
            List<String> stationNames = new ArrayList<>();
            Map<String, String> alternativeNames = new HashMap<>();

            // Add station names to the index
            for (int i = 0; i < timeTable.stations().size(); i++) {
                stationNames.add(timeTable.stations().name(i));
            }

            // Add station aliases to the index
            for (int i = 0; i < timeTable.stationAliases().size(); i++) {
                alternativeNames.put(
                    timeTable.stationAliases().alias(i),
                    timeTable.stationAliases().stationName(i)
                );
            }

            StopIndex stopIndex = new StopIndex(stationNames, alternativeNames);

            // Create the UI components
            QueryUI queryUI = QueryUI.create(stopIndex);

            // Create observable that provides journeys based on query parameters
            journeysObservable = Bindings.createObjectBinding(
                () -> {
                    String depStop = queryUI.depStopO().getValue();
                    String arrStop = queryUI.arrStopO().getValue();
                    LocalDate date = queryUI.dateO().getValue();

                    // If any required values are missing, return empty list
                    if (depStop == null || depStop.isEmpty() ||
                        arrStop == null || arrStop.isEmpty() ||
                        date == null) {
                        return Collections.emptyList();
                    }

                    // Find station IDs
                    int depStationId = findStationId(depStop);
                    int arrStationId = findStationId(arrStop);

                    if (depStationId < 0 || arrStationId < 0) {
                        return Collections.emptyList();
                    }

                    // Reuse previous profile if possible
                    if (lastProfile == null ||
                        !Objects.equals(lastDestination, arrStop) ||
                        !Objects.equals(lastDate, date)) {

                        lastProfile = router.profile(date, arrStationId);
                        lastDestination = arrStop;
                        lastDate = date;
                    }

                    // Extract journeys for the departure station
                    return JourneyExtractor.journeys(lastProfile, depStationId);
                },
                queryUI.depStopO(), queryUI.arrStopO(), queryUI.dateO()
            );

            // Create summary and detail views
            SummaryUI summaryUI = SummaryUI.create(journeysObservable, queryUI.timeO());
            DetailUI detailUI = DetailUI.create(summaryUI.selectedJourney());

            // Create the split pane containing summary and detail
            SplitPane splitPane = new SplitPane();
            splitPane.getItems().addAll(summaryUI.rootNode(), detailUI.rootNode());
            splitPane.setDividerPositions(0.4);

            // Create the main border pane
            BorderPane mainPane = new BorderPane();
            mainPane.setCenter(splitPane);
            mainPane.setTop(queryUI.rootNode());

            // Create the scene
            Scene scene = new Scene(mainPane);

            // Configure the primary stage
            primaryStage.setTitle("ReCHor");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Set focus to departure stop field
            Platform.runLater(() -> scene.lookup("#depStop").requestFocus());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds the station ID corresponding to a station name.
     *
     * @param stationName the name of the station
     * @return the station ID, or -1 if not found
     */
    private int findStationId(String stationName) {
        // Search in stations
        for (int i = 0; i < timeTable.stations().size(); i++) {
            if (timeTable.stations().name(i).equals(stationName)) {
                return i;
            }
        }

        // Search in aliases
        for (int i = 0; i < timeTable.stationAliases().size(); i++) {
            if (timeTable.stationAliases().stationName(i).equals(stationName) ||
                timeTable.stationAliases().alias(i).equals(stationName)) {

                // Find the station ID for this alias
                String officialName = timeTable.stationAliases().stationName(i);

                for (int j = 0; j < timeTable.stations().size(); j++) {
                    if (timeTable.stations().name(j).equals(officialName)) {
                        return j;
                    }
                }
            }
        }

        return -1;
    }
}