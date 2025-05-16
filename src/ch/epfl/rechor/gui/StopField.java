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
        popup.setAutoHide(true);

        // When user selects an item from the list
        resultList.setOnMouseClicked(e -> {
            String selectedItem = resultList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                textField.setText(selectedItem);
                textField.getParent().requestFocus(); // Make field inactive
            }
        });

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
                } else if (event.getCode() == KeyCode.ENTER) {
                    String selectedItem = resultList.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        textField.setText(selectedItem);
                        textField.getParent().requestFocus(); // Make field inactive
                        event.consume();
                    }
                }
            }
        });
        
        // Add a mouse click handler to show popup even if already focused
        textField.setOnMouseClicked(e -> {
            if (textField.isFocused()) {
                showPopup(textField, stopIndex, resultList, popup);
            }
        });

        // Track focus state and handle search functionality
        textField.focusedProperty().addListener((obs, oldVal, isFocused) -> {
            if (isFocused) {
                // Text field gained focus - show popup and search
                showPopup(textField, stopIndex, resultList, popup);
            } else {
                // Text field lost focus - process selection and hide popup
                popup.hide();

                // Select the first item in the list if something was typed but nothing selected
                if (!resultList.getItems().isEmpty() &&
                    !textField.getText().isEmpty() &&
                    resultList.getSelectionModel().getSelectedItem() != null) {
                    textField.setText(resultList.getSelectionModel().getSelectedItem());
                } else if (!resultList.getItems().isEmpty()) {
                    textField.setText(resultList.getItems().get(0));
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
     * Helper method to show popup and update search results
     */
    private static void showPopup(TextField textField, StopIndex stopIndex, ListView<String> resultList, Popup popup) {
        updateSearchResults(textField.getText(), stopIndex, resultList);

        // Position popup under the text field
        Node textFieldNode = textField;
        popup.setAnchorX(textFieldNode.localToScreen(textFieldNode.getBoundsInLocal()).getMinX());
        popup.setAnchorY(textFieldNode.localToScreen(textFieldNode.getBoundsInLocal()).getMaxY());
        popup.show(textField.getScene().getWindow());

        // Listen for changes in the search text
        textField.textProperty().addListener((observable, oldText, newText) -> {
            updateSearchResults(newText, stopIndex, resultList);
        });

        // Listen for changes in the position of the field (for positioning the popup)
        textField.boundsInLocalProperty().addListener((observable, oldBounds, newBounds) -> {
            if (popup.isShowing()) {
                Node node = textField;
                popup.setAnchorX(node.localToScreen(node.getBoundsInLocal()).getMinX());
                popup.setAnchorY(node.localToScreen(node.getBoundsInLocal()).getMaxY());
            }
        });
    }

    /**
     * Sets the text field to display the given stop name.
     *
     * @param stopName the stop name to display
     */
    public void setTo(String stopName) {
        if (stopName == null) {
            textField.setText("");
        } else {
            textField.setText(stopName);
        }
    }

    /**
     * Updates the search results list based on the current query.
     */
    private static void updateSearchResults(String query, StopIndex stopIndex, ListView<String> resultList) {
        // Always search for stops and update the list, even when query is blank
        // This will return alphabetically sorted stops when query is blank
        List<String> matchingStops = stopIndex.stopsMatching(query, 30);
        resultList.getItems().setAll(matchingStops);

        // Select the first result if there are any
        if (!resultList.getItems().isEmpty()) {
            resultList.getSelectionModel().select(0);
        }
    }
}
