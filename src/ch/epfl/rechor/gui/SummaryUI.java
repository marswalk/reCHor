package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Represents the graphical user interface component that provides an overview of all journeys
 * for a given day. This component displays a list of journeys with details such as departure time,
 * arrival time, duration, and transfer points.
 *
 * <p>The journeys are displayed in a {@link ListView}, where each journey is represented by a custom
 * cell. The list is sorted by departure time, and the first journey departing at or after the desired
 * departure time is automatically selected.</p>
 *
 * <p>The graphical representation of each journey includes:</p>
 * <ul>
 *   <li>An icon representing the type of vehicle for the first transport leg.</li>
 *   <li>The departure and arrival times.</li>
 *   <li>A graphical line with circles indicating stops and transfers.</li>
 *   <li>The total duration of the journey.</li>
 * </ul>
 *
 * <p>This class is part of the ReCHor project and is used to display the second main section of the
 * graphical interface, as described in the project specifications.</p>
 *
 * @param rootNode the root node of the scene graph
 * @param selectedJourneyO an observable value containing the currently selected journey
 * @see javafx.scene.control.ListView
 * @see ch.epfl.rechor.journey.Journey
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {
    private static final int ICON_SIZE = 20;
    private static final int LINE_MARGIN = 5;
    private static final int CIRCLE_RADIUS = 3;
    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");

    /**
     * Sealed interface for items that can be displayed in the journey list.
     */
    private sealed interface ListItem {
        /**
         * Represents a journey item in the list.
         */
        record JourneyItem(Journey journey) implements ListItem {}
        
        /**
         * Represents a date header in the list.
         */
        record DateHeader(LocalDate date) implements ListItem {}
        
        /**
         * Represents a button to load more journeys.
         */
        record LoadMoreButton(boolean isPrevious, LocalDate referenceDate) implements ListItem {}
    }
    
    /**
     * Creates a new {@code SummaryUI} instance that displays a list of journeys and allows
     * the user to select a journey.
     *
     * @param journeysO an observable value containing the list of journeys to display
     * @param timeO an observable value containing the desired time
     * @param isDepartureTimeO an observable value containing the toggle state for departure/arrival time
     * @param depStopO an observable value containing the departure stop
     * @param arrStopO an observable value containing the arrival stop
     * @return a new {@code SummaryUI} instance
     */
    public static SummaryUI create(ObservableValue<List<Journey>> journeysO, ObservableValue<LocalTime> timeO, ObservableValue<Boolean> isDepartureTimeO, ObservableValue<String> depStopO, ObservableValue<String> arrStopO) {
        BorderPane container = new BorderPane();
        ListView<ListItem> journeyListView = new ListView<>();
        journeyListView.getStylesheets().add("/summary.css");
        
        // Create observable property for selected journey
        ObjectProperty<Journey> selectedJourneyProperty = new SimpleObjectProperty<>();
        
        // Keep track of loaded dates
        List<LocalDate> loadedDates = new ArrayList<>();
        
        // Function to load journeys for a specific date
        Function<LocalDate, List<Journey>> loadJourneysForDate = (date) -> {
            // In a real implementation, this would query a service or repository
            // For now, we'll simulate by using the same journeys but with adjusted dates
            List<Journey> baseJourneys = journeysO.getValue();
            if (baseJourneys == null || baseJourneys.isEmpty()) {
                return List.of();
            }
            
            // Adjust dates of journeys to match the requested date
            List<Journey> adjustedJourneys = new ArrayList<>();
            LocalDate currentDate = baseJourneys.get(0).depTime().toLocalDate();
            for (Journey journey : baseJourneys) {
                // Calculate the time difference in days
                long daysDifference = date.toEpochDay() - currentDate.toEpochDay();
                
                // Create new legs with adjusted dates
                List<Journey.Leg> adjustedLegs = new ArrayList<>();
                for (Journey.Leg leg : journey.legs()) {
                    if (leg instanceof Journey.Leg.Transport transport) {
                        // Adjust transport leg dates
                        LocalDateTime adjustedDepTime = transport.depTime().plusDays(daysDifference);
                        LocalDateTime adjustedArrTime = transport.arrTime().plusDays(daysDifference);
                        
                        // Adjust intermediate stops
                        List<Journey.Leg.IntermediateStop> adjustedStops = new ArrayList<>();
                        for (Journey.Leg.IntermediateStop stop : transport.intermediateStops()) {
                            adjustedStops.add(new Journey.Leg.IntermediateStop(
                                    stop.stop(),
                                    stop.arrTime().plusDays(daysDifference),
                                    stop.depTime().plusDays(daysDifference)
                            ));
                        }
                        
                        adjustedLegs.add(new Journey.Leg.Transport(
                                transport.depStop(),
                                adjustedDepTime,
                                transport.arrStop(),
                                adjustedArrTime,
                                adjustedStops,
                                transport.vehicle(),
                                transport.route(),
                                transport.destination()
                        ));
                    } else if (leg instanceof Journey.Leg.Foot foot) {
                        // Adjust foot leg dates
                        LocalDateTime adjustedDepTime = foot.depTime().plusDays(daysDifference);
                        LocalDateTime adjustedArrTime = foot.arrTime().plusDays(daysDifference);
                        
                        adjustedLegs.add(new Journey.Leg.Foot(
                                foot.depStop(),
                                adjustedDepTime,
                                foot.arrStop(),
                                adjustedArrTime
                        ));
                    }
                }
                
                adjustedJourneys.add(new Journey(adjustedLegs));
            }
            
            return adjustedJourneys;
        };
        
        // Set up the cell factory to display different types of items
        journeyListView.setCellFactory(listView -> new ListCell<>() {
            private final JourneyCell journeyCell = new JourneyCell();
            private final Button loadMoreButton = new Button();
            private final Text dateHeaderText = new Text();
            private final BorderPane dateHeaderPane = new BorderPane(dateHeaderText);
            
            {
                loadMoreButton.getStyleClass().add("load-more-button");
                dateHeaderPane.getStyleClass().add("date-header");
                this.getStyleClass().add("date-header-cell");
            }
            
            @Override
            protected void updateItem(ListItem item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                
                if (item instanceof ListItem.JourneyItem journeyItem) {
                    journeyCell.updateItem(journeyItem.journey(), false);
                    setGraphic(journeyCell.getGraphic());
                } else if (item instanceof ListItem.DateHeader dateHeader) {
                    dateHeaderText.setText(DATE_FORMATTER.format(dateHeader.date()));
                    setGraphic(dateHeaderPane);
                } else if (item instanceof ListItem.LoadMoreButton loadMoreBtn) {
                    if (loadMoreBtn.isPrevious()) {
                        loadMoreButton.setText("Load Previous Day");
                    } else {
                        loadMoreButton.setText("Load Next Day");
                    }
                    
                    // Set up button action
                    loadMoreButton.setOnAction(event -> {
                        LocalDate targetDate = loadMoreBtn.isPrevious() 
                                ? loadMoreBtn.referenceDate().minusDays(1)
                                : loadMoreBtn.referenceDate().plusDays(1);
                        
                        // Load journeys for the target date
                        List<Journey> newJourneys = loadJourneysForDate.apply(targetDate);
                        if (!newJourneys.isEmpty()) {
                            // Add the date to loaded dates
                            loadedDates.add(targetDate);
                            
                            // Create list items with date header
                            ObservableList<ListItem> items = journeyListView.getItems();
                            int insertIndex = loadMoreBtn.isPrevious() ? 0 : items.size();
                            
                            // Remove the load more button
                            items.remove(getIndex());
                            
                            if (loadMoreBtn.isPrevious()) {
                                // Add load previous button if needed
                                items.add(0, new ListItem.LoadMoreButton(true, targetDate));
                                
                                // Add date header
                                items.add(1, new ListItem.DateHeader(targetDate));
                                
                                // Add journeys
                                int index = 2;
                                for (Journey journey : newJourneys) {
                                    items.add(index++, new ListItem.JourneyItem(journey));
                                }
                            } else {
                                // Add date header
                                items.add(new ListItem.DateHeader(targetDate));
                                
                                // Add journeys
                                for (Journey journey : newJourneys) {
                                    items.add(new ListItem.JourneyItem(journey));
                                }
                                
                                // Add load next button if needed
                                items.add(new ListItem.LoadMoreButton(false, targetDate));
                            }
                        }
                    });
                    
                    setGraphic(loadMoreButton);
                }
            }
        });
        
        // Initial population and scroll listener setup
        journeysO.subscribe((newJourneys) -> {
            if (newJourneys == null || newJourneys.isEmpty()) {
                journeyListView.getItems().clear();
                return;
            }
            
            // Get the current date from journeys
            LocalDate currentDate = newJourneys.get(0).depTime().toLocalDate();
            loadedDates.clear();
            loadedDates.add(currentDate);
            
            // Create list items
            ObservableList<ListItem> items = FXCollections.observableArrayList();
            
            // Add load previous button
            items.add(new ListItem.LoadMoreButton(true, currentDate));
            
            // Add date header
            items.add(new ListItem.DateHeader(currentDate));
            
            // Add journeys
            for (Journey journey : newJourneys) {
                items.add(new ListItem.JourneyItem(journey));
            }
            
            // Add load next button
            items.add(new ListItem.LoadMoreButton(false, currentDate));
            
            journeyListView.setItems(items);
            
            // Select appropriate journey based on time and time type
            updateSelectedJourney(journeyListView, newJourneys, timeO.getValue(), isDepartureTimeO.getValue());
        });
        
        // When time changes, update the selected journey
        timeO.subscribe((newTime) -> {
            updateSelectedJourney(journeyListView, journeysO.getValue(), newTime, isDepartureTimeO.getValue());
        });

        // When time type changes, update the selected journey
        isDepartureTimeO.subscribe((isDeparture) -> {
            updateSelectedJourney(journeyListView, journeysO.getValue(), timeO.getValue(), isDeparture);
        });
        
        // Track selection changes
        journeyListView.getSelectionModel().selectedItemProperty().subscribe(item -> {
            if (item instanceof ListItem.JourneyItem journeyItem) {
                selectedJourneyProperty.set(journeyItem.journey());
            }
        });
        
        // Observe changes in depStopO or arrStopO and update container accordingly
        Runnable updateView = () -> {
            String dep = depStopO.getValue();
            String arr = arrStopO.getValue();
            if (dep != null && arr != null && dep.equals(arr) && !dep.isEmpty()) {
                container.setCenter(createIdenticalStopsView());
                selectedJourneyProperty.set(null); // Clear detail UI
            } else {
                container.setCenter(journeyListView);
            }
        };
        depStopO.subscribe(s -> updateView.run());
        arrStopO.subscribe(s -> updateView.run());
        updateView.run();
        
        return new SummaryUI(container, selectedJourneyProperty);
    }

    /**
     * Updates the selected journey in the list view based on the specified time and whether
     * it's a departure or arrival time.
     *
     * @param listView the {@link ListView} displaying the journeys
     * @param journeys the list of available journeys
     * @param time the reference time for selection
     * @param isDepartureTime true if {@code time} is a departure time, false if it's an arrival time
     */
    private static void updateSelectedJourney(
            ListView<ListItem> listView,
            List<Journey> journeys,
            LocalTime time,
            Boolean isDepartureTime) {

        if (journeys == null || journeys.isEmpty() || time == null || isDepartureTime == null) {
            return;
        }

        Journey selectedJourney = null;

        if (isDepartureTime) {
            // Select journey based on departure time
            for (Journey journey : journeys) {
                if (!journey.depTime().toLocalTime().isBefore(time)) {
                    selectedJourney = journey;
                    break;
                }
            }
        } else {
            // Select journey based on arrival time
            for (Journey journey : journeys) {
                if (!journey.arrTime().toLocalTime().isAfter(time)) {
                    selectedJourney = journey;
                } else {
                    break; // Stop once we find a journey arriving after the specified time
                }
            }
        }

        if (selectedJourney != null) {
            ObservableList<ListItem> items = listView.getItems();
            for (int i = 0; i < items.size(); i++) {
                ListItem item = items.get(i);
                if (item instanceof ListItem.JourneyItem journeyItem &&
                    journeyItem.journey().equals(selectedJourney)) {
                    listView.getSelectionModel().select(i);
                    listView.scrollTo(i);
                    break;
                }
            }
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
     * Returns an observable value containing the currently selected journey.
     *
     * @return the selected journey as an observable value
     */
    public ObservableValue<Journey> selectedJourney() {
        return selectedJourneyO;
    }

    /**
     * A custom {@link ListCell} implementation for displaying a journey in the list view.
     * Each cell displays details about the journey, including the vehicle type, departure and
     * arrival times, duration, and a graphical representation of the journey's stops and transfers.
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

        /**
         * Constructs a new {@code JourneyCell} and initializes its graphical components.
         */
        public JourneyCell() {
            // Create route box (top)
            vehicleIcon = new ImageView();
            vehicleIcon.setFitHeight(ICON_SIZE);
            vehicleIcon.setFitWidth(ICON_SIZE);
            vehicleIcon.setPreserveRatio(true);

            routeText = new Text();

            routeBox = new HBox(vehicleIcon, routeText);
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

            setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        }

        /**
         * Updates the content of this cell to display the specified journey.
         *
         * @param journey the journey to display, or {@code null} if the cell is empty
         * @param empty whether this cell is empty
         */
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

        /**
         * Updates the graphical representation of the journey's stops and transfers.
         *
         * @param journey the journey to display
         */
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
     * A custom {@link Pane} implementation for positioning the graphical elements of a journey,
     * including the line and circles representing stops and transfers.
     */
    private static class CustomPane extends Pane {
        private Line line;
        private Group circleGroup;

        /**
         * Sets the line representing the journey.
         *
         * @param line the line to set
         */
        public void setLine(Line line) {
            this.line = line;
        }

        /**
         * Sets the group of circles representing stops and transfers.
         *
         * @param circleGroup the group of circles to set
         */
        public void setCircleGroup(Group circleGroup) {
            this.circleGroup = circleGroup;
        }

        /**
         * Lays out the graphical elements of the journey, positioning the line and circles
         * based on the pane's size and the relative positions of the stops and transfers.
         */
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

    private static Node createIdenticalStopsView() {
        VBox view = new VBox(10);
        view.setAlignment(Pos.CENTER);
        // Randomly pick between frau.jpg and mann.jpg
        String imageName = RANDOM.nextBoolean() ? "frau.jpg" : "mann.jpg";
        Image image = new Image("/" + imageName);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        Text message = new Text("Departure and destination are identical.");

        view.getChildren().addAll(imageView, message);
        return view;
    }
}
