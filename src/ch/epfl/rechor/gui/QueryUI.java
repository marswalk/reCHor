package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
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
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO,
        ObservableValue<Boolean> isDepartureTimeO, // NEW: Observable for departure/arrival toggle
        SBBClockNode clockNode
) {

    // Time formatting constants
    private static final DateTimeFormatter TIME_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String TIME_FORMAT_PATTERN_HHMM = "HH:mm";
    private static final String TIME_FORMAT_PATTERN_HMM = "H:mm";

    // Regex patterns for time input
    private static final String FOUR_DIGIT_PATTERN = "\\d{4}";
    private static final String THREE_DIGIT_PATTERN = "\\d{3}";

    // CSS and styling constants
    private static final String CSS_FILE = "query.css";
    private static final String TIME_PICKER_POPUP_CLASS = "time-picker-popup";
    private static final String TIME_LIST_VIEW_CLASS = "time-list-view";
    private static final String OK_BUTTON_CLASS = "ok-button";
    private static final String CANCEL_BUTTON_CLASS = "cancel-button";

    // Text constants
    private static final String DEPARTURE_PROMPT = "Nom de l'arrêt de départ";
    private static final String ARRIVAL_PROMPT = "Nom de l'arrêt d'arrivée";
    private static final String TIME_PROMPT = "HH:mm";
    private static final String DEPARTURE_LABEL = "Départ\u202f:";
    private static final String ARRIVAL_LABEL = "Arrivée\u202f:";
    private static final String DATE_LABEL = "Date\u202f:";
    private static final String TIME_LABEL = "Heure\u202f:";
    private static final String SWAP_BUTTON_TEXT = "↔";
    private static final String SWAP_TOOLTIP_TEXT = "Inverser départ et arrivée";
    private static final String OK_BUTTON_TEXT = "OK";
    private static final String CANCEL_BUTTON_TEXT = "Annuler";
    private static final String POPUP_HEADER_TEXT = "Sélectionner l'heure";
    private static final String TIME_SEPARATOR = ":";

    // ID constants
    private static final String DEP_STOP_ID = "depStop";
    private static final String ARR_STOP_ID = "arrStop";
    private static final String DATE_ID = "date";
    private static final String TIME_FIELD_ID = "timeField";

    // Dimension constants
    private static final int CLOCK_SIZE = 80;
    private static final int CLOCK_MIN_SIZE = 60;
    private static final int TIME_FIELD_WIDTH = 90;
    private static final int LIST_VIEW_HEIGHT = 180;
    private static final int LIST_VIEW_WIDTH = 90;

    // Spacing constants
    private static final int POPUP_SPACING = 15;
    private static final int POPUP_PADDING = 20;
    private static final int LIST_CELL_PADDING_V = 4;
    private static final int LIST_CELL_PADDING_H = 0;
    private static final int POPUP_OFFSET = 5;
    private static final int SCROLL_OFFSET = 2;
    private static final int SEPARATOR_PADDING = 5;

    // Time range constants
    private static final int HOURS_IN_DAY = 24;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int TIME_START = 0;

    // Font size constants
    private static final int LIST_CELL_FONT_SIZE = 14;
    private static final int HEADER_FONT_SIZE = 18;
    private static final int SEPARATOR_FONT_SIZE = 18;

    // Style strings
    private static final String LIST_CELL_STYLE = String.format("-fx-padding: %dpx %dpx; -fx-font-size: %dpx;",
            LIST_CELL_PADDING_V, LIST_CELL_PADDING_H, LIST_CELL_FONT_SIZE);
    private static final String POPUP_STYLE =
            "-fx-background-color: white;" +
                    "-fx-border-color: #B0B0B0;" +
                    "-fx-border-width: 1px;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-border-radius: 6px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.5, 0, 2);";
    private static final String HEADER_STYLE = String.format("-fx-font-size: %dpx; -fx-font-weight: bold; -fx-text-fill: #333;",
            HEADER_FONT_SIZE);
    private static final String SEPARATOR_STYLE = String.format("-fx-font-size: %dpx; -fx-font-weight: bold; -fx-padding: 0 %dpx;",
            SEPARATOR_FONT_SIZE, SEPARATOR_PADDING);

    private static class TimeListCell extends ListCell<Integer> {
        public TimeListCell() {
            setAlignment(Pos.CENTER);
            setStyle(LIST_CELL_STYLE);
        }
        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : String.format("%02d", item));
        }
    }

    // Custom StringConverter for the TextField to handle HHmm format and general parsing
    private static class SmartTimeConverter extends StringConverter<LocalTime> {
        private final DateTimeFormatter parseFormatterHHMM = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN_HHMM);
        private final DateTimeFormatter parseFormatterHMM = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN_HMM);

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
            if (toParse.matches(FOUR_DIGIT_PATTERN)) { // "1234" -> "12:34"
                toParse = toParse.substring(0, 2) + TIME_SEPARATOR + toParse.substring(2, 4);
            } else if (toParse.matches(THREE_DIGIT_PATTERN)) { // "123" -> "01:23"
                toParse = "0" + toParse.substring(0,1) + TIME_SEPARATOR + toParse.substring(1,3);
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
        VBox queryControlsVBox = new VBox();
        queryControlsVBox.getStylesheets().add(CSS_FILE);

        // --- Departure and Arrival Stops (Same as before) ---
        StopField departureField = StopField.create(stopIndex);
        departureField.textField().setPromptText(DEPARTURE_PROMPT);
        departureField.textField().setId(DEP_STOP_ID);
        Label depLabel = new Label(DEPARTURE_LABEL);

        StopField arrivalField = StopField.create(stopIndex);
        arrivalField.textField().setPromptText(ARRIVAL_PROMPT);
        arrivalField.textField().setId(ARR_STOP_ID);
        Label arrLabel = new Label(ARRIVAL_LABEL);

        Button swapButton = new Button(SWAP_BUTTON_TEXT);
        swapButton.setTooltip(new javafx.scene.control.Tooltip(SWAP_TOOLTIP_TEXT));
        swapButton.setOnAction(e -> {
            String dep = departureField.textField().getText();
            String arr = arrivalField.textField().getText();
            departureField.setTo(arr);
            arrivalField.setTo(dep);
        });

        HBox depBox = new HBox(depLabel, departureField.textField());
        depBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(departureField.textField(), Priority.ALWAYS);

        HBox arrBox = new HBox(arrLabel, arrivalField.textField());
        arrBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(arrivalField.textField(), Priority.ALWAYS);

        HBox stopsBox = new HBox(depBox, swapButton, arrBox);
        stopsBox.setAlignment(Pos.CENTER_LEFT);

        // --- Date Picker (Same as before) ---
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setId(DATE_ID);
        Label dateLabel = new Label(DATE_LABEL);
        HBox dateBox = new HBox(dateLabel, datePicker);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        // --- Time Input Field with Pop-out ListView Selector ---
        ObjectProperty<LocalTime> selectedTimeProperty = new SimpleObjectProperty<>(LocalTime.now());

        // TextField for time input
        TextField timeField = new TextField();
        timeField.setPromptText(TIME_PROMPT);
        timeField.setPrefWidth(TIME_FIELD_WIDTH); // Slightly wider for "HH:mm"
        timeField.setId(TIME_FIELD_ID);

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
        VBox popupLayout = new VBox(POPUP_SPACING);
        popupLayout.setPadding(new Insets(POPUP_PADDING));
        popupLayout.setStyle(POPUP_STYLE);
        popupLayout.getStyleClass().add(TIME_PICKER_POPUP_CLASS);

        Label popupHeader = new Label(POPUP_HEADER_TEXT);
        popupHeader.setStyle(HEADER_STYLE);

        ListView<Integer> hourListView = new ListView<>(FXCollections.observableArrayList(IntStream.range(TIME_START, HOURS_IN_DAY).boxed().collect(Collectors.toList())));
        hourListView.setCellFactory(lv -> new TimeListCell());
        hourListView.setPrefHeight(LIST_VIEW_HEIGHT);
        hourListView.setPrefWidth(LIST_VIEW_WIDTH);
        hourListView.getStyleClass().add(TIME_LIST_VIEW_CLASS);

        ListView<Integer> minuteListView = new ListView<>(FXCollections.observableArrayList(IntStream.range(TIME_START, MINUTES_IN_HOUR).boxed().collect(Collectors.toList())));
        minuteListView.setCellFactory(lv -> new TimeListCell());
        minuteListView.setPrefHeight(LIST_VIEW_HEIGHT);
        minuteListView.setPrefWidth(LIST_VIEW_WIDTH);
        minuteListView.getStyleClass().add(TIME_LIST_VIEW_CLASS);

        HBox listSelectorsBox = new HBox(hourListView, new Label(TIME_SEPARATOR), minuteListView);
        ((Label)listSelectorsBox.getChildren().get(1)).setStyle(SEPARATOR_STYLE);
        listSelectorsBox.setAlignment(Pos.CENTER);

        Button okButton = new Button(OK_BUTTON_TEXT);
        okButton.setDefaultButton(true);
        okButton.getStyleClass().add(OK_BUTTON_CLASS);
        Button cancelButton = new Button(CANCEL_BUTTON_TEXT);
        cancelButton.getStyleClass().add(CANCEL_BUTTON_CLASS);

        HBox buttonsPopupBox = new HBox(okButton, cancelButton);
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
                hourListView.scrollTo(Math.max(TIME_START, currentTimeInField.getHour() - SCROLL_OFFSET));
                minuteListView.getSelectionModel().select(currentTimeInField.getMinute());
                minuteListView.scrollTo(Math.max(TIME_START, currentTimeInField.getMinute() - SCROLL_OFFSET));

                Bounds fieldBounds = timeField.localToScreen(timeField.getBoundsInLocal());
                timePopup.show(timeField.getScene().getWindow(), fieldBounds.getMinX(), fieldBounds.getMaxY() + POPUP_OFFSET);
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

        // --- Add Time Type Selection (Departure/Arrival Toggle) ---
        BooleanProperty isDepartureTimeProperty = new SimpleBooleanProperty(true); // Default to departure time

        ToggleGroup timeTypeToggleGroup = new ToggleGroup();

        ToggleButton departureTimeToggle = new ToggleButton("Départ");
        departureTimeToggle.setToggleGroup(timeTypeToggleGroup);
        departureTimeToggle.setSelected(true); // Default selection

        ToggleButton arrivalTimeToggle = new ToggleButton("Arrivée");
        arrivalTimeToggle.setToggleGroup(timeTypeToggleGroup);

        timeTypeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == departureTimeToggle) {
                isDepartureTimeProperty.set(true);
            } else if (newVal == arrivalTimeToggle) {
                isDepartureTimeProperty.set(false);
            }
        });

        HBox timeTypeBox = new HBox(5, departureTimeToggle, arrivalTimeToggle);
        timeTypeBox.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label(TIME_LABEL);
        HBox timeBoxContent = new HBox(5, timeLabel, timeField, timeTypeBox);
        timeBoxContent.setAlignment(Pos.CENTER_LEFT);

        // --- Date and Time Row ---
        HBox dateTimeBox = new HBox(10, dateBox, timeBoxContent);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);

        queryControlsVBox.getChildren().addAll(stopsBox, dateTimeBox);

        // --- SBB Clock (Same as before) ---
        SBBClockNode sbbClock = new SBBClockNode();
        Node clockView = sbbClock.getView();
        ((StackPane) clockView).setPrefSize(CLOCK_SIZE, CLOCK_SIZE);
        ((StackPane) clockView).setMinSize(CLOCK_MIN_SIZE, CLOCK_MIN_SIZE);

        // --- Main Layout (Same as before) ---
        HBox mainLayout = new HBox();
        mainLayout.setAlignment(Pos.CENTER_LEFT);
        mainLayout.getChildren().addAll(queryControlsVBox, clockView);
        HBox.setHgrow(queryControlsVBox, Priority.ALWAYS);

        return new QueryUI(
                mainLayout,
                departureField.stopO(),
                arrivalField.stopO(),
                datePicker.valueProperty(),
                selectedTimeProperty, // This property is central
                isDepartureTimeProperty, // NEW: Pass the departure/arrival toggle observable
                sbbClock
        );
    }
}
