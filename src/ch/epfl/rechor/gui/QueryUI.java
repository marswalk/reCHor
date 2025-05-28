package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the query interface for searching journeys.
 * <p>
 * This component allows users to specify the parameters for their journey search, including:
 * <ul>
 * <li>Departure stop</li>
 * <li>Arrival stop</li>
 * <li>Date of travel</li>
 * <li>Time of travel (via a text field with a pop-out list selector on focus)</li>
 * </ul>
 * The text field for time supports "HHmm" input, automatically interpreting it as "HH:mm".
 * The interface dynamically updates the search results based on user input.
 * It also displays an SBB clock on the right side.
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 * <li>Text fields for entering departure and arrival stops, with auto-completion support.</li>
 * <li>A date picker for selecting the travel date.</li>
 * <li>A text field for time input. When focused, a pop-out selector with lists for hours/minutes appears.
 * Four-digit input (e.g., "1430") is interpreted as "14:30".</li>
 * <li>A button to swap the departure and arrival stops.</li>
 * <li>An SBB clock display.</li>
 * </ul>
 * @param rootNode  the root JavaFX node for this component
 * @param depStopO  observable value containing the selected departure stop name
 * @param arrStopO  observable value containing the selected arrival stop name
 * @param dateO     observable value containing the selected date
 * @param timeO     observable value containing the selected time
 * @see StopField
 * @see SBBClockNode
 * @see javafx.scene.control.DatePicker
 * @see javafx.scene.control.ListView
 * @see javafx.scene.control.TextField
 * @see javafx.scene.control.TextFormatter
 * @see javafx.stage.Popup
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 * @author AI Assistant (SBB Clock Integration & Enhanced Pop-out Time Selector with TextField)
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO,
        SBBClockNode clockNode
) {

    private static final DateTimeFormatter TIME_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static class TimeListCell extends ListCell<Integer> {
        public TimeListCell() {
            setAlignment(Pos.CENTER);
            setStyle("-fx-padding: 4px 0px; -fx-font-size: 14px;");
        }
        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : String.format("%02d", item));
        }
    }

    // Custom StringConverter for the TextField to handle HHmm format and general parsing
    private static class SmartTimeConverter extends StringConverter<LocalTime> {
        private final DateTimeFormatter parseFormatterHHMM = DateTimeFormatter.ofPattern("HH:mm");
        private final DateTimeFormatter parseFormatterHMM = DateTimeFormatter.ofPattern("H:mm");

        @Override
        public String toString(LocalTime time) {
            return time != null ? time.format(TIME_DISPLAY_FORMATTER) : "";
        }

        @Override
        public LocalTime fromString(String string) throws DateTimeParseException {
            if (string == null || string.trim().isEmpty()) {
                return null; // Allow empty field
            }
            String toParse = string.trim();
            if (toParse.matches("\\d{4}")) { // "1234" -> "12:34"
                toParse = toParse.substring(0, 2) + ":" + toParse.substring(2, 4);
            } else if (toParse.matches("\\d{3}")) { // "123" -> "01:23"
                toParse = "0" + toParse.substring(0,1) + ":" + toParse.substring(1,3);
            }


            try {
                return LocalTime.parse(toParse, parseFormatterHHMM);
            } catch (DateTimeParseException e1) {
                // Try parsing H:mm (e.g. "9:30")
                try {
                    return LocalTime.parse(toParse, parseFormatterHMM);
                } catch (DateTimeParseException e2) {
                    throw new DateTimeParseException("Invalid time format: " + string, string, 0, e2);
                }
            }
        }
    }


    public static QueryUI create(StopIndex stopIndex) {
        VBox queryControlsVBox = new VBox(5);
        queryControlsVBox.getStylesheets().add("query.css");
        queryControlsVBox.setPadding(new Insets(10));

        // --- Departure and Arrival Stops (Same as before) ---
        StopField departureField = StopField.create(stopIndex);
        departureField.textField().setPromptText("Nom de l'arrêt de départ");
        departureField.textField().setId("depStop");
        Label depLabel = new Label("Départ\u202f:");

        StopField arrivalField = StopField.create(stopIndex);
        arrivalField.textField().setPromptText("Nom de l'arrêt d'arrivée");
        arrivalField.textField().setId("arrStop");
        Label arrLabel = new Label("Arrivée\u202f:");

        Button swapButton = new Button("↔");
        swapButton.setTooltip(new javafx.scene.control.Tooltip("Inverser départ et arrivée"));
        swapButton.setOnAction(e -> {
            String dep = departureField.textField().getText();
            String arr = arrivalField.textField().getText();
            departureField.setTo(arr);
            arrivalField.setTo(dep);
        });

        HBox depBox = new HBox(5, depLabel, departureField.textField());
        depBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(departureField.textField(), Priority.ALWAYS);

        HBox arrBox = new HBox(5, arrLabel, arrivalField.textField());
        arrBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(arrivalField.textField(), Priority.ALWAYS);

        HBox stopsBox = new HBox(10, depBox, swapButton, arrBox);
        stopsBox.setAlignment(Pos.CENTER_LEFT);

        // --- Date Picker (Same as before) ---
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId("date");
        Label dateLabel = new Label("Date\u202f:");
        HBox dateBox = new HBox(5, dateLabel, datePicker);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        // --- Time Input Field with Pop-out ListView Selector ---
        ObjectProperty<LocalTime> selectedTimeProperty = new SimpleObjectProperty<>(LocalTime.now());

        // TextField for time input
        TextField timeField = new TextField();
        timeField.setPromptText("HH:mm");
        timeField.setPrefWidth(90); // Slightly wider for "HH:mm"
        timeField.setId("timeField");

        TextFormatter<LocalTime> timeTextFormatter = new TextFormatter<>(
                new SmartTimeConverter(),
                selectedTimeProperty.get() // Initial value for formatter
        );
        timeField.setTextFormatter(timeTextFormatter);

        // Bind TextFormatter's value to selectedTimeProperty
        // 1. Changes from TextFormatter (valid user input) update selectedTimeProperty
        timeTextFormatter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && (selectedTimeProperty.get() == null || !selectedTimeProperty.get().equals(newVal))) {
                selectedTimeProperty.set(newVal);
            } else if (newVal == null && selectedTimeProperty.get() != null){
                selectedTimeProperty.set(null); // Allow clearing the time
            }
        });

        // 2. Changes to selectedTimeProperty (e.g., from popup) update TextFormatter's value (and thus TextField's text)
        selectedTimeProperty.addListener((obs, oldVal, newVal) -> {
            if (timeTextFormatter.getValue() == null || !timeTextFormatter.getValue().equals(newVal)) {
                timeTextFormatter.setValue(newVal); // This updates the TextField
            }
        });


        // Popup content (ListViews for hours and minutes)
        VBox popupLayout = new VBox(15);
        popupLayout.setPadding(new Insets(20));
        popupLayout.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #B0B0B0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.5, 0, 2);"
        );
        popupLayout.getStyleClass().add("time-picker-popup");

        Label popupHeader = new Label("Sélectionner l'heure");
        popupHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        ListView<Integer> hourListView = new ListView<>(FXCollections.observableArrayList(IntStream.range(0, 24).boxed().collect(Collectors.toList())));
        hourListView.setCellFactory(lv -> new TimeListCell());
        hourListView.setPrefHeight(180);
        hourListView.setPrefWidth(90);
        hourListView.getStyleClass().add("time-list-view");

        ListView<Integer> minuteListView = new ListView<>(FXCollections.observableArrayList(IntStream.range(0, 60).boxed().collect(Collectors.toList())));
        minuteListView.setCellFactory(lv -> new TimeListCell());
        minuteListView.setPrefHeight(180);
        minuteListView.setPrefWidth(90);
        minuteListView.getStyleClass().add("time-list-view");

        HBox listSelectorsBox = new HBox(5, hourListView, new Label(":"), minuteListView);
        ((Label)listSelectorsBox.getChildren().get(1)).setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 5px;");
        listSelectorsBox.setAlignment(Pos.CENTER);

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.getStyleClass().add("ok-button");
        Button cancelButton = new Button("Annuler");
        cancelButton.getStyleClass().add("cancel-button");

        HBox buttonsPopupBox = new HBox(10, okButton, cancelButton);
        buttonsPopupBox.setAlignment(Pos.CENTER_RIGHT);

        popupLayout.getChildren().addAll(popupHeader, listSelectorsBox, buttonsPopupBox);

        Popup timePopup = new Popup();
        timePopup.setAutoHide(true);
        timePopup.getContent().add(popupLayout);

        // Show popup on TextField focus
        timeField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused && !timePopup.isShowing()) {
                LocalTime currentTimeInField = timeTextFormatter.getValue();
                if (currentTimeInField == null) currentTimeInField = LocalTime.now(); // Fallback

                hourListView.getSelectionModel().select(currentTimeInField.getHour());
                hourListView.scrollTo(Math.max(0, currentTimeInField.getHour() - 2));
                minuteListView.getSelectionModel().select(currentTimeInField.getMinute());
                minuteListView.scrollTo(Math.max(0, currentTimeInField.getMinute() - 2));

                Bounds fieldBounds = timeField.localToScreen(timeField.getBoundsInLocal());
                timePopup.show(timeField.getScene().getWindow(), fieldBounds.getMinX(), fieldBounds.getMaxY() + 5);
            }
        });

        // Popup button actions
        okButton.setOnAction(event -> {
            Integer selectedHour = hourListView.getSelectionModel().getSelectedItem();
            Integer selectedMinute = minuteListView.getSelectionModel().getSelectedItem();
            if (selectedHour != null && selectedMinute != null) {
                selectedTimeProperty.set(LocalTime.of(selectedHour, selectedMinute));
                // The listener on selectedTimeProperty will update the TextFormatter, then the TextField
            }
            timePopup.hide();
            // Platform.runLater(() -> queryControlsVBox.requestFocus()); // Optionally move focus away
        });

        cancelButton.setOnAction(event -> {
            timePopup.hide();
        });


        Label timeLabel = new Label("Heure\u202f:");
        HBox timeBoxContent = new HBox(5, timeLabel, timeField); // Using timeField directly
        timeBoxContent.setAlignment(Pos.CENTER_LEFT);

        // --- Date and Time Row ---
        HBox dateTimeBox = new HBox(20);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);
        dateTimeBox.getChildren().addAll(dateBox, timeBoxContent);

        queryControlsVBox.getChildren().addAll(stopsBox, dateTimeBox);

        // --- SBB Clock (Same as before) ---
        SBBClockNode sbbClock = new SBBClockNode();
        Node clockView = sbbClock.getView();
        ((StackPane) clockView).setPrefSize(80, 80);
        ((StackPane) clockView).setMinSize(60,60);

        // --- Main Layout (Same as before) ---
        HBox mainLayout = new HBox(10);
        mainLayout.setAlignment(Pos.CENTER_LEFT);
        mainLayout.setPadding(new Insets(5));
        mainLayout.getChildren().addAll(queryControlsVBox, clockView);
        HBox.setHgrow(queryControlsVBox, Priority.ALWAYS);

        return new QueryUI(
                mainLayout,
                departureField.stopO(),
                arrivalField.stopO(),
                datePicker.valueProperty(),
                selectedTimeProperty, // This property is central
                sbbClock
        );
    }
}