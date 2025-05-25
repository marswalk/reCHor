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
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
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
 * It also displays an SBB clock on the right side.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li>Text fields for entering departure and arrival stops, with auto-completion support.</li>
 * <li>A date picker for selecting the travel date.</li>
 * <li>A time field with formatted input for specifying the travel time.</li>
 * <li>A button to swap the departure and arrival stops.</li>
 * <li>An SBB clock display.</li>
 * </ul>
 * * @param rootNode  the root JavaFX node for this component
 * @param depStopO  observable value containing the selected departure stop name
 * @param arrStopO  observable value containing the selected arrival stop name
 * @param dateO     observable value containing the selected date
 * @param timeO     observable value containing the selected time
 * * @see StopField
 * @see SBBClockNode
 * @see javafx.scene.control.DatePicker
 * @see javafx.scene.control.TextFormatter
 * * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 * @author AI Assistant (SBB Clock Integration)
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO,
        SBBClockNode clockNode // Expose clock node if its animation needs external control (e.g. stop on close)
) {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final StringConverter<LocalTime> TIME_CONVERTER = new LocalTimeStringConverter(DISPLAY_FORMATTER, INPUT_FORMATTER);


    /**
     * Creates a new query interface using the provided stop index.
     * <p>
     * This method initializes all UI components, including text fields for stops,
     * a date picker, a time field, and an SBB clock. It also sets up event handlers
     * for user interactions, such as swapping stops and updating observable values.
     * </p>
     *
     * @param stopIndex the index to use for searching stops
     * @return a new instance of QueryUI
     */
    public static QueryUI create(StopIndex stopIndex) {
        // Container for query controls (original VBox content)
        VBox queryControlsVBox = new VBox();
        queryControlsVBox.getStylesheets().add("query.css"); // Apply stylesheet to controls


        // --- Create departure stop field ---
        StopField departureField = StopField.create(stopIndex);
        departureField.textField().setPromptText("Nom de l'arrêt de départ");
        departureField.textField().setId("depStop");
        Label depLabel = new Label("Départ\u202f:");

        // --- Create arrival stop field ---
        StopField arrivalField = StopField.create(stopIndex);
        arrivalField.textField().setPromptText("Nom de l'arrêt d'arrivée");
        arrivalField.textField().setId("arrStop");
        Label arrLabel = new Label("Arrivée\u202f:");

        // --- Create swap button ---
        Button swapButton = new Button("↔");
        swapButton.setOnAction(e -> {
            String dep = departureField.textField().getText();
            String arr = arrivalField.textField().getText();
            departureField.setTo(arr);
            arrivalField.setTo(dep);
        });

        // --- First row with departure and arrival stops ---
        HBox stopsBox = new HBox();
        stopsBox.setAlignment(Pos.CENTER_LEFT);

        HBox depBox = new HBox();
        depBox.setAlignment(Pos.CENTER_LEFT);
        depBox.getChildren().addAll(depLabel, departureField.textField());
        HBox.setHgrow(departureField.textField(), Priority.ALWAYS);


        HBox arrBox = new HBox();
        arrBox.setAlignment(Pos.CENTER_LEFT);
        arrBox.getChildren().addAll(arrLabel, arrivalField.textField());
        HBox.setHgrow(arrivalField.textField(), Priority.ALWAYS);

        stopsBox.getChildren().addAll(depBox, swapButton, arrBox);

        // --- Create date picker ---
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId("date");
        Label dateLabel = new Label("Date\u202f:");

        // --- Create time field with formatter ---
        // Must re-initialize TextFormatter for each instance if it holds state related to a specific TextField
        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(TIME_CONVERTER, LocalTime.now());
        ObjectProperty<LocalTime> timeProperty = new SimpleObjectProperty<>(); // Default to null or LocalTime.now()
        timeProperty.bind(timeFormatter.valueProperty()); // Bind to the formatter's value

        TextField timeField = new TextField();
        timeField.setId("time");
        timeField.setTextFormatter(timeFormatter);
        // Set initial text from current time if property is initialized with it
        if (timeFormatter.getValue() != null) {
            timeField.setText(TIME_CONVERTER.toString(timeFormatter.getValue()));
        }


        Label timeLabel = new Label("Heure\u202f:");

        // --- Second row with date and time ---
        HBox dateTimeBox = new HBox();
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);

        HBox dateBox = new HBox();
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.getChildren().addAll(dateLabel, datePicker);

        HBox timeBoxContent = new HBox();
        timeBoxContent.setAlignment(Pos.CENTER_LEFT);
        timeBoxContent.getChildren().addAll(timeLabel, timeField);

        dateTimeBox.getChildren().addAll(dateBox, timeBoxContent);

        // Add all control components to the queryControlsVBox
        queryControlsVBox.getChildren().addAll(stopsBox, dateTimeBox);

        // --- Create SBB Clock ---
        SBBClockNode sbbClock = new SBBClockNode();
        Node clockView = sbbClock.getView();
        // Set a preferred size for the clock in the UI. Adjust as needed.
        // The SBBClockNode's internal scaling will adapt to this size.
        ((StackPane) clockView).setPrefSize(80, 80);
        ((StackPane) clockView).setMinSize(60,60); // Ensure it doesn't get too small


        // --- Main layout: HBox with query controls on left, clock on right ---
        HBox mainLayout = new HBox();
        mainLayout.setAlignment(Pos.CENTER_LEFT); // Vertically center children

        mainLayout.getChildren().addAll(queryControlsVBox, clockView);
        HBox.setHgrow(queryControlsVBox, Priority.ALWAYS); // Query controls take available width

        return new QueryUI(
                mainLayout, // This HBox is the new rootNode
                departureField.stopO(),
                arrivalField.stopO(),
                datePicker.valueProperty(),
                timeProperty,
                sbbClock); // Pass the clock instance
    }
}
