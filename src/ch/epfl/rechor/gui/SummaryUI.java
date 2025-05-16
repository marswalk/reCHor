package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import ch.epfl.rechor.journey.Vehicle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * UI component displaying a visual summary of journeys with their timings and transfers.
 * 
 * @param rootNode the root node
 * @param selectedJourneyO the selected journey
 * 
 * @return a new SummaryUI instance
 * 
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {
    private static final int ICON_SIZE = 20;
    private static final int LINE_MARGIN = 5;
    private static final int CIRCLE_RADIUS = 3;

    /**
     * Creates a new SummaryUI with the given journey list and departure time.
     *
     * @param journeysO the observable list of journeys
     * @param depTimeO  the observable departure time
     * @return a new SummaryUI instance
     */
    public static SummaryUI create(ObservableValue<List<Journey>> journeysO, ObservableValue<LocalTime> depTimeO) {
        ListView<Journey> journeyListView = new ListView<>();
        journeyListView.getStylesheets().add("/summary.css");
        journeyListView.setCellFactory(param -> new JourneyCell());

        // Bind the journey list to the list view
        journeysO.subscribe((newJourneys) -> {
            journeyListView.getItems().setAll(newJourneys);

            // Select appropriate journey based on departure time
            if (newJourneys != null && !newJourneys.isEmpty()) {
                LocalTime currentDepTime = depTimeO.getValue();
                selectJourneyByDepartureTime(journeyListView, newJourneys, currentDepTime);
            }
        });

        // When departure time changes, select the appropriate journey
        depTimeO.subscribe((newTime) -> {
            List<Journey> journeys = journeysO.getValue();
            if (journeys != null && !journeys.isEmpty()) {
                selectJourneyByDepartureTime(journeyListView, journeys, newTime);
            }
        });

        // Initial population of the list
        if (journeysO.getValue() != null) {
            journeyListView.getItems().setAll(journeysO.getValue());
            if (!journeyListView.getItems().isEmpty() && depTimeO.getValue() != null) {
                selectJourneyByDepartureTime(journeyListView, journeysO.getValue(), depTimeO.getValue());
            }
        }

        // Create observable value for selected journey
        ObjectProperty<Journey> selectedJourneyProperty = new SimpleObjectProperty<>();
        journeyListView.getSelectionModel().selectedItemProperty().subscribe(
                (newJourney) -> selectedJourneyProperty.set(newJourney));

        return new SummaryUI(journeyListView, selectedJourneyProperty);
    }

    private static void selectJourneyByDepartureTime(ListView<Journey> listView, List<Journey> journeys, LocalTime depTime) {
        Journey selectedJourney = null;

        // Find the first journey departing at or after the desired departure time
        for (Journey journey : journeys) {
            LocalTime journeyDepTime = journey.depTime().toLocalTime();
            if (!journeyDepTime.isBefore(depTime)) {
                selectedJourney = journey;
                break;
            }
        }

        // If no journey was found, select the last one
        if (selectedJourney == null && !journeys.isEmpty()) {
            selectedJourney = journeys.get(journeys.size() - 1);
        }

        if (selectedJourney != null) {
            listView.getSelectionModel().select(selectedJourney);
            listView.scrollTo(selectedJourney);
        }
    }

    /**
     * Returns the root node of this UI component.
     *
     * @return the root node
     */
    public Node rootNode() {
        return rootNode;
    }

    /**
     * Returns an observable value containing the selected journey.
     *
     * @return the selected journey
     */
    public ObservableValue<Journey> selectedJourney() {
        return selectedJourneyO;
    }

    /**
     * ListCell implementation for displaying a journey.
     */
    private static class JourneyCell extends ListCell<Journey> {
        private final BorderPane cellRoot;
        private final HBox routeBox;
        private final ImageView vehicleIcon;
        private final Text routeText;
        private final HBox departureBox;
        private final Text departureText;
        private final CustomPane journeyLinePane;
        private final Text arrivalText;
        private final HBox durationBox;
        private final Text durationText;

        public JourneyCell() {
            // Create route box (top)
            vehicleIcon = new ImageView();
            vehicleIcon.setFitHeight(ICON_SIZE);
            vehicleIcon.setFitWidth(ICON_SIZE);
            vehicleIcon.setPreserveRatio(true);

            routeText = new Text();

            routeBox = new HBox(5, vehicleIcon, routeText);
            routeBox.setAlignment(Pos.CENTER_LEFT);
            routeBox.getStyleClass().add("route");

            // Create departure box (left)
            departureText = new Text();
            departureBox = new HBox(departureText);
            departureBox.getStyleClass().add("departure");
            departureBox.setAlignment(Pos.CENTER);

            // Create journey line pane (center)
            journeyLinePane = new CustomPane();
            journeyLinePane.setPrefSize(0, 0); // Let JavaFX resize it

            // Create arrival text (right)
            arrivalText = new Text();

            // Create duration box (bottom)
            durationText = new Text();
            durationBox = new HBox(durationText);
            durationBox.setAlignment(Pos.CENTER);
            durationBox.getStyleClass().add("duration");

            // Create cell root
            cellRoot = new BorderPane();
            cellRoot.getStyleClass().add("journey");
            cellRoot.setTop(routeBox);
            cellRoot.setLeft(departureBox);
            cellRoot.setCenter(journeyLinePane);
            cellRoot.setRight(arrivalText);
            cellRoot.setBottom(durationBox);

            BorderPane.setMargin(departureBox, new Insets(0, 10, 0, 0));
            BorderPane.setMargin(arrivalText, new Insets(0, 0, 0, 10));

            setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);

            if (empty || journey == null) {
                setGraphic(null);
                return;
            }

            // Find first transport leg for route info
            Journey.Leg.Transport firstTransportLeg = null;
            for (Journey.Leg leg : journey.legs()) {
                if (leg instanceof Journey.Leg.Transport transport) {
                    firstTransportLeg = transport;
                    break;
                }
            }

            if (firstTransportLeg != null) {
                // Set vehicle icon
                vehicleIcon.setImage(VehicleIcons.iconFor(firstTransportLeg.vehicle()));

                // Set route text
                routeText.setText(FormatterFr.formatRouteDestination(firstTransportLeg));
            }

            // Set departure time
            departureText.setText(FormatterFr.formatTime(journey.depTime()));

            // Set arrival time
            arrivalText.setText(FormatterFr.formatTime(journey.arrTime()));

            // Set duration
            durationText.setText(FormatterFr.formatDuration(journey.duration()));

            // Update journey line
            updateJourneyLine(journey);

            setGraphic(cellRoot);
        }

        private void updateJourneyLine(Journey journey) {
            journeyLinePane.getChildren().clear();

            // Create the line
            Line line = new Line();
            line.setStartX(LINE_MARGIN);
            line.setEndX(100 - LINE_MARGIN); // Will be properly positioned in layoutChildren
            line.getStyleClass().add("journey-line");

            // Create departure and arrival circles
            Circle departureCircle = new Circle(CIRCLE_RADIUS);
            departureCircle.getStyleClass().add("dep-arr");

            Circle arrivalCircle = new Circle(CIRCLE_RADIUS);
            arrivalCircle.getStyleClass().add("dep-arr");

            // Create group to hold all elements
            Group circleGroup = new Group();
            circleGroup.getChildren().addAll(departureCircle, arrivalCircle);

            // Calculate positions for transfer circles
            LocalDateTime journeyStartTime = journey.depTime();
            Duration journeyDuration = journey.duration();

            for (int i = 1; i < journey.legs().size(); i += 2) {
                // Every second leg is a foot leg (based on constructor invariant)
                if (journey.legs().get(i) instanceof Journey.Leg.Foot footLeg) {
                    Circle transferCircle = new Circle(CIRCLE_RADIUS);
                    transferCircle.getStyleClass().add("transfer");

                    // Calculate relative position
                    Duration timeFromStart = Duration.between(journeyStartTime, footLeg.depTime());
                    double relativePosition = timeFromStart.toMillis() / (double) journeyDuration.toMillis();

                    // Store relative position for use in layoutChildren
                    transferCircle.setUserData(relativePosition);

                    circleGroup.getChildren().add(transferCircle);
                }
            }

            journeyLinePane.getChildren().addAll(line, circleGroup);
            journeyLinePane.setLine(line);
            journeyLinePane.setCircleGroup(circleGroup);
        }
    }

    /**
     * Custom pane that positions the line and circles properly when its size changes.
     */
    private static class CustomPane extends Pane {
        private Line line;
        private Group circleGroup;

        public void setLine(Line line) {
            this.line = line;
        }

        public void setCircleGroup(Group circleGroup) {
            this.circleGroup = circleGroup;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            double width = getWidth();
            double height = getHeight();

            if (line != null) {
                // Position the line horizontally centered
                line.setStartX(LINE_MARGIN);
                line.setEndX(width - LINE_MARGIN);
                line.setStartY(height / 2);
                line.setEndY(height / 2);
            }

            if (circleGroup != null) {
                ObservableList<Node> circles = circleGroup.getChildren();

                if (circles.size() >= 2) {
                    // First circle is departure
                    Circle departureCircle = (Circle) circles.get(0);
                    departureCircle.setCenterX(LINE_MARGIN);
                    departureCircle.setCenterY(height / 2);

                    // Last circle is arrival
                    Circle arrivalCircle = (Circle) circles.get(1);
                    arrivalCircle.setCenterX(width - LINE_MARGIN);
                    arrivalCircle.setCenterY(height / 2);

                    // Position transfer circles based on their relative positions
                    for (int i = 2; i < circles.size(); i++) {
                        Circle transferCircle = (Circle) circles.get(i);
                        Double relativePosition = (Double) transferCircle.getUserData();

                        if (relativePosition != null) {
                            double x = LINE_MARGIN + relativePosition * (width - 2 * LINE_MARGIN);
                            transferCircle.setCenterX(x);
                            transferCircle.setCenterY(height / 2);
                        }
                    }
                }
            }
        }
    }
}
