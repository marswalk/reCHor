package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class TestDetailUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load timetable
        TimeTable timeTable = FileTimeTable.in(Path.of("timetable"));

        // Set up test parameters similar to JourneyExtractorTest
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int arrStationId = 11486; // Gruyères
        int depStationId = 7872;  // Ecublens VD, EPFL

        // Read the profile from file (same as in JourneyExtractorTest)
        Profile profile = readProfile(timeTable, date, arrStationId);

        // Extract journeys using JourneyExtractor
        List<Journey> journeys = JourneyExtractor.journeys(profile, depStationId);

        // Get journey at index 32 (as in the tests)
        Journey journey = journeys.get(32);

        // Create DetailUI and display it
        DetailUI detailUI = new DetailUI(journey);
        Pane root = new BorderPane(detailUI.createNodes());

        primaryStage.setTitle("Journey Detail Test");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    // Helper method from JourneyExtractorTest to read pre-computed profile
    private Profile readProfile(TimeTable timeTable, LocalDate date, int arrStationId) throws IOException {
        Path path = Path.of("test/ch/epfl/rechor/journey/profile_" + date + "_" + arrStationId + ".txt");
        try (BufferedReader r = Files.newBufferedReader(path)) {
            Profile.Builder profileB = new Profile.Builder(timeTable, date, arrStationId);
            int stationId = -1;
            String line;
            while ((line = r.readLine()) != null) {
                stationId += 1;
                if (line.isEmpty()) continue;
                ParetoFront.Builder frontB = new ParetoFront.Builder();
                for (String t : line.split(","))
                    frontB.add(Long.parseLong(t, 16));
                profileB.setForStation(stationId, frontB);
            }
            return profileB.build();
        }
    }
}