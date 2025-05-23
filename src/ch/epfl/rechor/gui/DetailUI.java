package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Journey.Leg;
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.JourneyIcalConverter;

import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.scene.control.ScrollPane;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the detailed view of a journey in the GUI.
 * <p>
 * This class is responsible for displaying the details of a selected journey,
 * including its legs, intermediate stops, and additional functionalities such as
 * viewing the journey on a map or exporting it as an iCalendar event.
 * </p>
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record DetailUI(Node rootNode) {

    /**
     * Creates a new instance of {@code DetailUI} for the given observable journey.
     * <p>
     * The UI dynamically updates to reflect changes in the observable journey.
     * If no journey is available, a "No journey" message is displayed.
     * </p>
     *
     * @param journeyObservable the observable value containing the journey to display
     * @return a new {@code DetailUI} instance
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservable) {
        // Create the root scroll pane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setId("detail");
        scrollPane.getStylesheets().add("detail.css");

        // Create the stack pane containing the journey view and "no journey" message
        StackPane stackPane = new StackPane();
        scrollPane.setContent(stackPane);

        // Create the "no journey" message UI
        VBox noJourneyBox = new VBox();
        noJourneyBox.setId("no-journey");
        Text noJourneyText = new Text("Aucun voyage");
        noJourneyBox.getChildren().add(noJourneyText);
        stackPane.getChildren().add(noJourneyBox);

        // Create the journey detail UI
        VBox journeyBox = new VBox();

        // Main container for the journey content
        StackPane mainContent = new StackPane();

        // Create annotations pane for the connecting lines between circles
        Pane annotationsPane = new Pane();
        annotationsPane.setId("annotations");

        // Create journey legs grid
        LegsGridPane legsGrid = new LegsGridPane();
        legsGrid.setId("legs");

        // Add both to the main content
        mainContent.getChildren().addAll(annotationsPane, legsGrid);

        // Create buttons
        HBox buttonsBox = new HBox();
        buttonsBox.setId("buttons");

        Button mapButton = new Button("Carte");
        Button calendarButton = new Button("Calendrier");
        buttonsBox.getChildren().addAll(mapButton, calendarButton);

        // Add main content and buttons to journey box
        journeyBox.getChildren().addAll(mainContent, buttonsBox);

        // Add journey box to stack pane
        stackPane.getChildren().add(journeyBox);

        // Update UI when journey changes
        journeyObservable.subscribe((newJourney) -> {
            if (newJourney == null) {
                noJourneyBox.setVisible(true);
                journeyBox.setVisible(false);
            } else {
                noJourneyBox.setVisible(false);
                journeyBox.setVisible(true);

                // Update the legs grid with new journey
                legsGrid.getChildren().clear();
                legsGrid.circlePairs.clear();
                displayJourneyLegs(newJourney, legsGrid);

                // Set up button handlers
                mapButton.setOnAction(e -> showJourneyMap(newJourney));
                calendarButton.setOnAction(e -> saveJourneyCalendar(newJourney));
            }
        });

        // Initial update
        Journey initialJourney = journeyObservable.getValue();
        if (initialJourney == null) {
            noJourneyBox.setVisible(true);
            journeyBox.setVisible(false);
        } else {
            noJourneyBox.setVisible(false);
            journeyBox.setVisible(true);
            displayJourneyLegs(initialJourney, legsGrid);

            // Set up button handlers
            mapButton.setOnAction(e -> showJourneyMap(initialJourney));
            calendarButton.setOnAction(e -> saveJourneyCalendar(initialJourney));
        }

        return new DetailUI(scrollPane);
    }

    /**
     * Custom {@link GridPane} for displaying journey legs and drawing connecting lines
     * between departure and arrival points.
     */
    private static class LegsGridPane extends GridPane {
        final List<Circle[]> circlePairs = new ArrayList<>();

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            // Clear and redraw all connecting lines
            Pane annotationsPane = (Pane) getParent().getChildrenUnmodifiable().getFirst();
            annotationsPane.getChildren().clear();

            for (Circle[] pair : circlePairs) {
                Circle depCircle = pair[0];
                Circle arrCircle = pair[1];

                // Get the coordinates in the parent's coordinate system
                double startX = depCircle.getBoundsInParent().getCenterX();
                double startY = depCircle.getBoundsInParent().getCenterY();
                double endX = arrCircle.getBoundsInParent().getCenterX();
                double endY = arrCircle.getBoundsInParent().getCenterY();

                Line line = new Line(startX, startY, endX, endY);
                line.setStroke(Color.RED);
                line.setStrokeWidth(2);

                annotationsPane.getChildren().add(line);
            }
        }
    }

    /**
     * Populates the grid with the legs of the given journey.
     * <p>
     * Each leg is displayed with its relevant details, such as departure and arrival
     * times, stops, and vehicle information. Intermediate stops are displayed in an
     * expandable section.
     * </p>
     *
     * @param journey  the journey whose legs are to be displayed
     * @param legsGrid the grid pane to populate with journey leg details
     */
    private static void displayJourneyLegs(Journey journey, LegsGridPane legsGrid) {
        int rowIndex = 0;

        for (Leg leg : journey.legs()) {
            if (leg instanceof Leg.Foot footLeg) {
                // Add foot leg as a simple text spanning columns 2 and 3
                Text footText = new Text(FormatterFr.formatLeg(footLeg));
                legsGrid.add(footText, 2, rowIndex, 2, 1);
                rowIndex += 1;
            } else if (leg instanceof Leg.Transport transportLeg) {
                // Departure info
                Text depTimeText = new Text(FormatterFr.formatTime(transportLeg.depTime()));
                depTimeText.getStyleClass().add("departure");
                GridPane.setHalignment(depTimeText, HPos.RIGHT);

                Circle depCircle = new Circle(3);
                depCircle.setStroke(Color.BLACK);

                Text depStopText = new Text(transportLeg.depStop().name());

                String depPlatform = FormatterFr.formatPlatformName(transportLeg.depStop());
                Text depPlatformText = new Text(depPlatform);
                depPlatformText.getStyleClass().add("departure");

                legsGrid.add(depTimeText, 0, rowIndex);
                legsGrid.add(depCircle, 1, rowIndex);
                legsGrid.add(depStopText, 2, rowIndex);
                if (!depPlatform.isEmpty()) {
                    legsGrid.add(depPlatformText, 3, rowIndex);
                }
                rowIndex += 1;

                // Vehicle icon and route destination
                ImageView vehicleIcon = new ImageView(VehicleIcons.iconFor(transportLeg.vehicle()));
                vehicleIcon.setFitWidth(31);
                vehicleIcon.setFitHeight(31);
                GridPane.setHalignment(vehicleIcon, HPos.CENTER);
                GridPane.setValignment(vehicleIcon, VPos.CENTER);

                Text routeDestText = new Text(FormatterFr.formatRouteDestination(transportLeg));

                // Determine if we need to span multiple rows for the icon
                boolean hasIntermediateStops = !transportLeg.intermediateStops().isEmpty();
                if (hasIntermediateStops) {
                    legsGrid.add(vehicleIcon, 0, rowIndex, 1, 2);
                } else {
                    legsGrid.add(vehicleIcon, 0, rowIndex);
                }

                legsGrid.add(routeDestText, 2, rowIndex, 2, 1);
                rowIndex += 1;

                // Intermediate stops
                if (hasIntermediateStops) {
                    // Create accordion for intermediate stops
                    Accordion accordion = new Accordion();

                    // Create content
                    GridPane stopsGrid = new GridPane();
                    stopsGrid.getStyleClass().add("intermediate-stops");

                    int stopRowIndex = 0;
                    for (Leg.IntermediateStop stop : transportLeg.intermediateStops()) {
                        Text arrTimeText = new Text(FormatterFr.formatTime(stop.arrTime()));
                        Text intermediateDepTimeText = new Text(FormatterFr.formatTime(stop.depTime()));
                        Text stopNameText = new Text(stop.stop().name());

                        stopsGrid.add(arrTimeText, 0, stopRowIndex);
                        stopsGrid.add(intermediateDepTimeText, 1, stopRowIndex);
                        stopsGrid.add(stopNameText, 2, stopRowIndex);

                        stopRowIndex++;
                    }

                    // Create the title with number of stops and duration
                    String title = transportLeg.intermediateStops().size() + " arrêts (" +
                                  FormatterFr.formatDuration(transportLeg.duration()) + ")";

                    TitledPane titledPane = new TitledPane(title, stopsGrid);
                    accordion.getPanes().add(titledPane);

                    legsGrid.add(accordion, 2, rowIndex, 2, 1);
                    rowIndex += 1;
                }

                // Arrival info
                Text arrTimeText = new Text(FormatterFr.formatTime(transportLeg.arrTime()));
                GridPane.setHalignment(arrTimeText, HPos.RIGHT);

                Circle arrCircle = new Circle(3);
                arrCircle.setStroke(Color.BLACK);

                Text arrStopText = new Text(transportLeg.arrStop().name());

                String arrPlatform = FormatterFr.formatPlatformName(transportLeg.arrStop());
                Text arrPlatformText = new Text(arrPlatform);

                legsGrid.add(arrTimeText, 0, rowIndex);
                legsGrid.add(arrCircle, 1, rowIndex);
                legsGrid.add(arrStopText, 2, rowIndex);
                if (!arrPlatform.isEmpty()) {
                    legsGrid.add(arrPlatformText, 3, rowIndex);
                }

                // Store the departure and arrival circles for connecting lines
                legsGrid.circlePairs.add(new Circle[]{depCircle, arrCircle});

                rowIndex += 1;
            }
        }
    }

    /**
     * Opens the journey's route in the default web browser using uMap.
     * <p>
     * The journey is visualized as a GeoJSON document passed as a query parameter
     * to the uMap service.
     * </p>
     *
     * @param journey the journey to display on the map
     */
    private static void showJourneyMap(Journey journey) {
        try {
            String geoJson = JourneyGeoJsonConverter.toGeoJson(journey).toString();

            // Remove spaces and line breaks for URL
            geoJson = geoJson.replaceAll("\\s+", "");

            // Create the URI and open in browser
            URI uri = new URI(
                "https",
                "umap.osm.ch",
                "/fr/map",
                "data=" + geoJson,
                null
            );

            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the journey as an iCalendar file.
     *
     * @param journey the journey to save
     */
    private static void saveJourneyCalendar(Journey journey) {
        try {
            String icalData = JourneyIcalConverter.toIcalendar(journey);

            // Generate default filename
            String date = journey.depTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String filename = "voyage_" + date + ".ics";

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save iCalendar Event");
            fileChooser.setInitialFileName(filename);
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("iCalendar Files", "*.ics")
            );

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                Files.writeString(file.toPath(), icalData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}