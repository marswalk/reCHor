package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents the query interface for searching journeys.
 * <p>
 * This component allows users to select departure and arrival stops,
 * and choose the date and time for their journey search.
 *
 * @param rootNode  the root JavaFX node for this component
 * @param depStopO  observable value containing the selected departure stop name
 * @param arrStopO  observable value containing the selected arrival stop name
 * @param dateO     observable value containing the selected date
 * @param timeO     observable value containing the selected time
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO) {

    /**
     * Creates a new query interface that uses the provided stop index.
     *
     * @param stopIndex the index to use for searching stops
     * @return a new query UI component
     */
    public static QueryUI create(StopIndex stopIndex) {
        // Root container
        VBox root = new VBox();
        root.getStylesheets().add("query.css");

        // Create departure stop field
        StopField departureField = StopField.create(stopIndex);
        departureField.textField().setPromptText("Nom de l'arrêt de départ");
        departureField.textField().setId("depStop");
        Label depLabel = new Label("Départ\u202f:");

        // Create arrival stop field
        StopField arrivalField = StopField.create(stopIndex);
        arrivalField.textField().setPromptText("Nom de l'arrêt d'arrivée");
        arrivalField.textField().setId("arrStop");
        Label arrLabel = new Label("Arrivée\u202f:");

        // Create swap button
        Button swapButton = new Button("↔");
        swapButton.setOnAction(e -> {
            String dep = departureField.textField().getText();
            String arr = arrivalField.textField().getText();
            departureField.setTo(arr);
            arrivalField.setTo(dep);
        });

        // First row with departure and arrival stops
        HBox stopsBox = new HBox(5);
        stopsBox.setAlignment(Pos.CENTER_LEFT);

        // Create departure container
        HBox depBox = new HBox(5);
        depBox.setAlignment(Pos.CENTER_LEFT);
        depBox.getChildren().addAll(depLabel, departureField.textField());

        // Create arrival container
        HBox arrBox = new HBox(5);
        arrBox.setAlignment(Pos.CENTER_LEFT);
        arrBox.getChildren().addAll(arrLabel, arrivalField.textField());

        stopsBox.getChildren().addAll(depBox, swapButton, arrBox);

        // Create date picker
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId("date");
        Label dateLabel = new Label("Date\u202f:");

        // Create time field with formatter
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("H:mm");
        StringConverter<LocalTime> timeConverter = new LocalTimeStringConverter(displayFormatter, inputFormatter);

        ObjectProperty<LocalTime> timeProperty = new SimpleObjectProperty<>(LocalTime.now());
        TextField timeField = new TextField();
        timeField.setId("time");

        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(timeConverter, timeProperty.get());
        timeField.setTextFormatter(timeFormatter);
        timeProperty.bind(timeFormatter.valueProperty());

        Label timeLabel = new Label("Heure\u202f:");

        // Second row with date and time
        HBox dateTimeBox = new HBox(5);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);

        HBox dateBox = new HBox(5);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.getChildren().addAll(dateLabel, datePicker);

        HBox timeBox = new HBox(5);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        timeBox.getChildren().addAll(timeLabel, timeField);

        dateTimeBox.getChildren().addAll(dateBox, timeBox);

        // Add all components to the root container
        root.getChildren().addAll(stopsBox, dateTimeBox);

        return new QueryUI(
                root,
                departureField.stopO(),
                arrivalField.stopO(),
                datePicker.valueProperty(),
                timeProperty);
    }
}