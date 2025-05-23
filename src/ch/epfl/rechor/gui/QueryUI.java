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
import javafx.scene.input.MouseEvent;
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
 * This component allows users to specify the parameters for their journey search, including:
 * <ul>
 *   <li>Departure stop</li>
 *   <li>Arrival stop</li>
 *   <li>Date of travel</li>
 *   <li>Time of travel</li>
 * </ul>
 * The interface dynamically updates the search results based on user input.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 *   <li>Text fields for entering departure and arrival stops, with auto-completion support.</li>
 *   <li>A date picker for selecting the travel date.</li>
 *   <li>A time field with formatted input for specifying the travel time.</li>
 *   <li>A button to swap the departure and arrival stops.</li>
 * </ul>
 * 
 * @param rootNode  the root JavaFX node for this component
 * @param depStopO  observable value containing the selected departure stop name
 * @param arrStopO  observable value containing the selected arrival stop name
 * @param dateO     observable value containing the selected date
 * @param timeO     observable value containing the selected time
 * 
 * @see StopField
 * @see javafx.scene.control.DatePicker
 * @see javafx.scene.control.TextFormatter
 * 
 * @author Guanting Wen (392412)
@author Ben Fall (373176)
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO) {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final StringConverter<LocalTime> TIME_CONVERTER = new LocalTimeStringConverter(DISPLAY_FORMATTER, INPUT_FORMATTER);
    private static final TextFormatter<LocalTime> TIME_FORMATTER = new TextFormatter<>(TIME_CONVERTER, LocalTime.now());

    /**
     * Creates a new query interface using the provided stop index.
     * <p>
     * This method initializes all UI components, including text fields for stops,
     * a date picker, and a time field. It also sets up event handlers for user interactions,
     * such as swapping stops and updating observable values.
     * </p>
     *
     * @param stopIndex the index to use for searching stops
     * @return a new instance of QueryUI
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
        HBox stopsBox = new HBox();
        stopsBox.setAlignment(Pos.CENTER_LEFT);

        // Create departure container
        HBox depBox = new HBox();
        depBox.setAlignment(Pos.CENTER_LEFT);
        depBox.getChildren().addAll(depLabel, departureField.textField());

        // Create arrival container
        HBox arrBox = new HBox();
        arrBox.setAlignment(Pos.CENTER_LEFT);
        arrBox.getChildren().addAll(arrLabel, arrivalField.textField());

        stopsBox.getChildren().addAll(depBox, swapButton, arrBox);

        // Create date picker
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId("date");
        Label dateLabel = new Label("Date\u202f:");

        // Create time field with formatter
        ObjectProperty<LocalTime> timeProperty = new SimpleObjectProperty<>(LocalTime.now());
        TextField timeField = new TextField();
        timeField.setId("time");

        timeField.setTextFormatter(TIME_FORMATTER);
        timeProperty.bind(TIME_FORMATTER.valueProperty());

        Label timeLabel = new Label("Heure\u202f:");

        // Second row with date and time
        HBox dateTimeBox = new HBox();
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);

        HBox dateBox = new HBox();
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.getChildren().addAll(dateLabel, datePicker);

        HBox timeBox = new HBox();
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
