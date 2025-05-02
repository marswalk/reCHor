package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.Vehicle;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A UI component that displays detailed information about a journey,
 * including departure and arrival times, legs, intermediate stops,
 * and buttons for exporting to map or calendar.
 *
 * @param journey the journey to display (can be null)
 */
public record DetailUI(Journey journey) {

    /**
     * Creates a node representing the detailed view of a journey.
     * If the journey is null, a placeholder message is displayed instead.
     *
     * @return a JavaFX node displaying the journey details
     */
    public Node createNodes() {
        if (journey == null) {
            Label noJourneyLabel = new Label("Select a journey to see details");
            noJourneyLabel.setId("no-journey");
            return noJourneyLabel;
        }

        VBox root = new VBox(10);
        root.setId("detail");

        // Header with departure and arrival information
        VBox header = createHeader();

        // Grid of journey legs
        GridPane legsGrid = createLegsGrid();

        // Action buttons
        HBox buttonsBox = createButtons();

        root.getChildren().addAll(header, legsGrid, buttonsBox);
        return root;
    }

    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(10));

        // Create departure and arrival time labels
        Label depLabel = new Label(FormatterFr.formatTime(journey.depTime()) + " " + journey.depStop().name());
        depLabel.getStyleClass().add("departure");

        Label arrLabel = new Label(FormatterFr.formatTime(journey.arrTime()) + " " + journey.arrStop().name());
        arrLabel.getStyleClass().add("departure");

        // Create duration label
        Label durationLabel = new Label("Duration: " + FormatterFr.formatDuration(journey.duration()));

        header.getChildren().addAll(depLabel, arrLabel, durationLabel);
        return header;
    }

    private GridPane createLegsGrid() {
        GridPane legsGrid = new GridPane();
        legsGrid.setId("legs");

        List<Journey.Leg> legs = journey.legs();

        int row = 0;
        for (int i = 0; i < legs.size(); i++) {
            Journey.Leg leg = legs.get(i);

            if (leg instanceof Journey.Leg.Transport transportLeg) {
                // Transport leg
                Vehicle vehicle = transportLeg.vehicle();

                // Circle with vehicle icon
                Circle circle = new Circle(15);
                circle.setFill(Color.WHITE);

                // Add the vehicle icon to the circle
                StackPane iconPane = new StackPane();
                iconPane.getChildren().addAll(
                        circle,
                        new Region() {
                            {
                                setPrefSize(30, 30);
                                setStyle("-fx-background-image: url('" +
                                        VehicleIcons.iconFor(vehicle).getUrl() + "'); " +
                                        "-fx-background-size: 20px; " +
                                        "-fx-background-repeat: no-repeat; " +
                                        "-fx-background-position: center;");
                            }
                        }
                );

                legsGrid.add(iconPane, 0, row);

                // Transport information
                VBox transportInfo = new VBox(5);

                // Route and destination
                Label routeLabel = new Label(FormatterFr.formatRouteDestination(transportLeg));
                routeLabel.setStyle("-fx-font-weight: bold;");

                // Departure and arrival
                Label legInfoLabel = new Label(FormatterFr.formatLeg(transportLeg));

                transportInfo.getChildren().addAll(routeLabel, legInfoLabel);

                legsGrid.add(transportInfo, 1, row);

                // Intermediate stops if any
                if (!transportLeg.intermediateStops().isEmpty()) {
                    GridPane intermediateStopsGrid = new GridPane();
                    intermediateStopsGrid.getStyleClass().add("intermediate-stops");

                    int stopRow = 0;
                    for (Journey.Leg.IntermediateStop stop : transportLeg.intermediateStops()) {
                        Label stopLabel = new Label(
                                FormatterFr.formatTime(stop.arrTime()) + " " + stop.stop().name()
                        );

                        intermediateStopsGrid.add(stopLabel, 0, stopRow++);
                    }

                    // Add intermediate stops to the next row
                    row++;
                    legsGrid.add(intermediateStopsGrid, 1, row);
                }
            } else if (leg instanceof Journey.Leg.Foot footLeg) {
                // Foot leg
                Label footLabel = new Label(FormatterFr.formatLeg(footLeg));

                // Center horizontally across both columns
                GridPane.setColumnSpan(footLabel, 2);
                legsGrid.add(footLabel, 0, row);
            }

            row++;
        }

        return legsGrid;
    }

    private HBox createButtons() {
        HBox buttonsBox = new HBox(5);
        buttonsBox.setId("buttons");

        Button mapButton = new Button("Map");
        Button calendarButton = new Button("Calendar");

        // Map button action - Export journey to GeoJSON and open in browser
        mapButton.setOnAction(event -> {
            try {
                // Convert journey to GeoJSON
                String geoJson = JourneyGeoJsonConverter.toGeoJson(journey).toString();

                // Create temporary HTML file to display the map
                File tempFile = createMapHtml(geoJson);

                // Open the file in the default browser
                Desktop.getDesktop().browse(tempFile.toURI());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Calendar button action - Export journey to iCalendar format
        calendarButton.setOnAction(event -> {
            try {
                // Create iCalendar content
                String icalContent = createIcalContent();

                // Create temporary .ics file
                File tempFile = File.createTempFile("journey_", ".ics");
                tempFile.deleteOnExit();

                // Write content to file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    writer.write(icalContent);
                }

                // Open the file with the default application
                Desktop.getDesktop().open(tempFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        buttonsBox.getChildren().addAll(mapButton, calendarButton);
        return buttonsBox;
    }

    private File createMapHtml(String geoJson) throws IOException {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Journey Map</title>
                <meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no">
                <link href="https://api.mapbox.com/mapbox-gl-js/v2.14.1/mapbox-gl.css" rel="stylesheet">
                <script src="https://api.mapbox.com/mapbox-gl-js/v2.14.1/mapbox-gl.js"></script>
                <style>
                    body { margin: 0; padding: 0; }
                    #map { position: absolute; top: 0; bottom: 0; width: 100%; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    mapboxgl.accessToken = 'pk.eyJ1IjoiYmVuZmFsbCIsImEiOiJjbG9veGMwbXUwMGIxMmtwODF5azRvYjJ0In0.AlCU4NQNmLD2LBX8BdVsrg';
                    const map = new mapboxgl.Map({
                        container: 'map',
                        style: 'mapbox://styles/mapbox/streets-v12',
                        center: [8.227512, 46.818188], // Center on Switzerland
                        zoom: 8
                    });
                    
                    map.on('load', () => {
                        const data = %s;
                        
                        map.addSource('route', {
                            'type': 'geojson',
                            'data': {
                                'type': 'Feature',
                                'properties': {},
                                'geometry': data
                            }
                        });
                        
                        map.addLayer({
                            'id': 'route',
                            'type': 'line',
                            'source': 'route',
                            'layout': {
                                'line-join': 'round',
                                'line-cap': 'round'
                            },
                            'paint': {
                                'line-color': '#3887be',
                                'line-width': 5,
                                'line-opacity': 0.75
                            }
                        });
                        
                        // Fit the map to the route
                        const coordinates = data.coordinates;
                        const bounds = coordinates.reduce((bounds, coord) => {
                            return bounds.extend(coord);
                        }, new mapboxgl.LngLatBounds(coordinates[0], coordinates[0]));
                        
                        map.fitBounds(bounds, {
                            padding: 50
                        });
                    });
                </script>
            </body>
            </html>
            """.formatted(geoJson);

        File tempFile = File.createTempFile("journey_map_", ".html");
        tempFile.deleteOnExit();

        Files.writeString(tempFile.toPath(), html);
        return tempFile;
    }

    private String createIcalContent() {
        // Create a unique identifier for the event
        String uid = UUID.randomUUID().toString().replace("-", "");

        // Current time as creation timestamp
        LocalDateTime now = LocalDateTime.now();

        // Create summary from journey departure and arrival
        String summary = "Journey from " + journey.depStop().name() + " to " + journey.arrStop().name();

        // Create description with all legs
        StringBuilder description = new StringBuilder();
        description.append("Journey details:\\n");
        description.append("From: ").append(journey.depStop().name())
                .append(" at ").append(FormatterFr.formatTime(journey.depTime())).append("\\n");
        description.append("To: ").append(journey.arrStop().name())
                .append(" at ").append(FormatterFr.formatTime(journey.arrTime())).append("\\n");
        description.append("Duration: ").append(FormatterFr.formatDuration(journey.duration())).append("\\n\\n");

        description.append("Legs:\\n");
        for (Journey.Leg leg : journey.legs()) {
            if (leg instanceof Journey.Leg.Transport transportLeg) {
                description.append("- ").append(transportLeg.vehicle())
                        .append(" ").append(transportLeg.route())
                        .append(" to ").append(transportLeg.destination()).append("\\n");
                description.append("  ").append(FormatterFr.formatLeg(transportLeg)).append("\\n");
            } else if (leg instanceof Journey.Leg.Foot footLeg) {
                description.append("- ").append(FormatterFr.formatLeg(footLeg)).append("\\n");
            }
        }

        // Build iCalendar content
        return new IcalBuilder()
                .begin(IcalBuilder.Component.VCALENDAR)
                .add(IcalBuilder.Name.PRODID, "-//ReCHor//NONSGML Journey//EN")
                .add(IcalBuilder.Name.VERSION, "2.0")
                .begin(IcalBuilder.Component.VEVENT)
                .add(IcalBuilder.Name.UID, uid)
                .add(IcalBuilder.Name.DTSTAMP, now)
                .add(IcalBuilder.Name.DTSTART, journey.depTime())
                .add(IcalBuilder.Name.DTEND, journey.arrTime())
                .add(IcalBuilder.Name.SUMMARY, summary)
                .add(IcalBuilder.Name.DESCRIPTION, description.toString())
                .end()
                .end()
                .build();
    }
}