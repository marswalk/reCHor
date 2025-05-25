package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

import java.util.List;

/**
 * Represents a text field for searching and selecting public transport stops.
 * <p>
 * This component provides an auto-completion feature by displaying a popup with matching stops
 * as the user types. Users can navigate the suggestions using the keyboard and select a stop.
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Displays a popup with up to 30 matching stops based on user input.</li>
 *   <li>Allows navigation of suggestions using arrow keys.</li>
 *   <li>Automatically updates the selected stop when the field loses focus.</li>
 *   <li>Supports swapping of stop names programmatically.</li>
 * </ul>
 *
 * @param textField the text field used for input and display
 * @param stopO     an observable value containing the selected stop name
 *
 * @see javafx.scene.control.TextField
 * @see javafx.stage.Popup
 * @see javafx.scene.control.ListView
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record StopField(TextField textField, ObservableValue<String> stopO) {

    /** The maximum number of matching stops to display in the popup. */
    private static final int MAX_RESULTS = 30;
    private static final double MAX_POPUP_HEIGHT = 240.0;

    /**
     * Creates a new stop field using the provided stop index for searching stops.
     * <p>
     * This method initializes the text field, popup, and result list, and sets up
     * event handlers for user interactions, such as keyboard navigation and focus changes.
     * </p>
     *
     * @param stopIndex the index to use for searching stops
     * @return a new instance of StopField
     */
    public static StopField create(StopIndex stopIndex) {
        // Create the text field and result popup
        TextField textField = new TextField();
        StringProperty selectedStopProperty = new SimpleStringProperty("");
        ListView<String> resultList = new ListView<>();
        Popup popup = new Popup();

        // Configure the results list view
        resultList.setFocusTraversable(false);
        resultList.setMaxHeight(MAX_POPUP_HEIGHT);
        popup.getContent().add(resultList);
        popup.setHideOnEscape(false);
        // Don't auto-hide, we'll manage visibility based on focus
        popup.setAutoHide(false);

        // Handle keyboard navigation in the results list
        textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (popup.isShowing()) {
                if (event.getCode() == KeyCode.DOWN) {
                    int currentIndex = resultList.getSelectionModel().getSelectedIndex();
                    if (currentIndex < resultList.getItems().size() - 1) {
                        resultList.getSelectionModel().select(currentIndex + 1);
                        resultList.scrollTo(currentIndex + 1);
                        event.consume();
                    }
                } else if (event.getCode() == KeyCode.UP) {
                    int currentIndex = resultList.getSelectionModel().getSelectedIndex();
                    if (currentIndex > 0) {
                        resultList.getSelectionModel().select(currentIndex - 1);
                        resultList.scrollTo(currentIndex - 1);
                        event.consume();
                    }
                } else if (event.getCode() == KeyCode.TAB) {
                    // Tab key selects the currently highlighted item and hides the popup
                    String selectedItem = resultList.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        textField.setText(selectedItem);
                        selectedStopProperty.set(selectedItem);
                        popup.hide();
                        // Don't consume the event so the focus moves to the next field
                    }
                }
            }
        });

        // Create subscriptions for focus, text changes, and position changes
        textField.focusedProperty().subscribe((isFocused) -> {
            if (isFocused) {
                // Text field gained focus - show popup and search
                updateSearchResults(textField.getText(), stopIndex, resultList);

                // Position popup under the text field
                Node node = textField;
                popup.setAnchorX(node.localToScreen(node.getBoundsInLocal()).getMinX());
                popup.setAnchorY(node.localToScreen(node.getBoundsInLocal()).getMaxY());
                popup.show(textField.getScene().getWindow());

                // Subscribe to text changes when focused
                textField.textProperty().subscribe((newText) ->
                        updateSearchResults(newText, stopIndex, resultList));

                // Subscribe to bounds changes when focused
                textField.boundsInLocalProperty().subscribe((newBounds) -> {
                    if (popup.isShowing()) {
                        Node n = textField;
                        popup.setAnchorX(n.localToScreen(n.getBoundsInLocal()).getMinX());
                        popup.setAnchorY(n.localToScreen(n.getBoundsInLocal()).getMaxY());
                    }
                });
            } else {
                // Text field lost focus - hide popup and update the selected stop
                popup.hide();

                // Select the first item in the list if something was typed but nothing selected
                if (!resultList.getItems().isEmpty()) {
                    if (resultList.getSelectionModel().getSelectedItem() != null) {
                        textField.setText(resultList.getSelectionModel().getSelectedItem());
                    } else {
                        textField.setText(resultList.getItems().getFirst());
                    }
                } else {
                    textField.setText("");
                }

                // Update the selected stop property
                selectedStopProperty.set(textField.getText());
            }
        });

        return new StopField(textField, selectedStopProperty);
    }

    /**
     * Sets the text field to display the given stop name and updates the observable stop property.
     * <p>
     * This method is used to programmatically set the stop name in the text field and ensure
     * the observable value is updated accordingly.
     * </p>
     *
     * @param stopName the stop name to display
     */
    public void setTo(String stopName) {
        String newText = (stopName == null) ? "" : stopName;
        textField.setText(newText);

        // Directly update the observable property 'stopO'
        if (this.stopO instanceof StringProperty property) {
            property.set(newText);
        }
    }

    /**
     * Updates the search results list based on the current query.
     * <p>
     * This method searches for stops matching the query and updates the popup list
     * with the top MAX_RESULTS results. The first result is selected by default.
     * </p>
     *
     * @param query      the search query entered by the user
     * @param stopIndex  the index to use for searching stops
     * @param resultList the list view to display the search results
     */
    private static void updateSearchResults(String query, StopIndex stopIndex, ListView<String> resultList) {
        // Search for stops and update the list
        List<String> matchingStops = stopIndex.stopsMatching(query, MAX_RESULTS);
        resultList.getItems().setAll(matchingStops);

        // Select the first result if there are any
        if (!resultList.getItems().isEmpty()) {
            resultList.getSelectionModel().select(0);
        }
    }
}
