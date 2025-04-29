package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public record DetailUI(Node rootNode) {

    /**
     * Creates the detailed UI for a journey.
     *
     * @param journeyObservable the observable journey
     * @return the DetailUI instance
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservable) {
        BorderPane root = new BorderPane();
        root.getStylesheets().add("details.css");

        // Center: Journey details
        VBox detailsBox = new VBox();
        detailsBox.setId("details");
        root.setCenter(detailsBox);

        // Bottom: Buttons
        HBox buttonsBox = new HBox(10);
        Button mapButton = new Button("Carte");
        Button calendarButton = new Button("Calendrier");
        buttonsBox.getChildren().addAll(mapButton, calendarButton);
        root.setBottom(buttonsBox);

        // Handle journey updates
        journeyObservable.addListener((obs, oldJourney, newJourney) -> {
            detailsBox.getChildren().clear();
            if (newJourney == null) {
                Label noJourneyLabel = new Label("Aucun voyage");
                noJourneyLabel.setId("no-journey");
                detailsBox.getChildren().add(noJourneyLabel);
            } else {
                detailsBox.getChildren().add(createJourneyDetails(newJourney));
            }
        });

        return new DetailUI(root);
    }

    /**
     * Creates the graphical representation of a journey's details.
     *
     * @param journey the journey
     * @return the node representing the journey details
     */
    private static Node createJourneyDetails(Journey journey) {
        GridPane grid = new GridPane();
        grid.setId("journey-details");

        // Example: Add a placeholder for journey steps
        Text placeholder = new Text("Détails du voyage ici...");
        grid.add(placeholder, 0, 0);

        // Add annotations (lines between circles)
        Pane annotations = new Pane();
        Line line = new Line(10, 10, 100, 100);
        line.setStrokeWidth(2);
        line.setStroke(Color.RED);
        annotations.getChildren().add(line);

        StackPane stack = new StackPane(grid, annotations);
        return stack;
    }
}