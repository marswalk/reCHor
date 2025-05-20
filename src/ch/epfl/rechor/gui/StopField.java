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
 * Represents a text field that allows users to search and select public transport stops.
 * <p>
 * The component displays a popup with matching stops when the field is focused.
 * Users can navigate the suggestions using keyboard arrow keys and select a stop.
 *
 * @param textField the text field used for input and display
 * @param stopO     an observable value containing the selected stop name
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record StopField(TextField textField, ObservableValue<String> stopO) {

    /**
     * Creates a new stop field that uses the provided index for searching stops.
     *
     * @param stopIndex the index to use for searching stops
     * @return a new stop field component
     */
    public static StopField create(StopIndex stopIndex) {
        // Create the text field and result popup
        TextField textField = new TextField();
        StringProperty selectedStopProperty = new SimpleStringProperty("");
        ListView<String> resultList = new ListView<>();
        Popup popup = new Popup();

        // Configure the results list view
        resultList.setFocusTraversable(false);
        resultList.setMaxHeight(240);
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
                        textField.setText(resultList.getItems().get(0));
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
     */
    private static void updateSearchResults(String query, StopIndex stopIndex, ListView<String> resultList) {
        // Search for stops and update the list
        List<String> matchingStops = stopIndex.stopsMatching(query, 30);
        resultList.getItems().setAll(matchingStops);

        // Select the first result if there are any
        if (!resultList.getItems().isEmpty()) {
            resultList.getSelectionModel().select(0);
        }
    }
}