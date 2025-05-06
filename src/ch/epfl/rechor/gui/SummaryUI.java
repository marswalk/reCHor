package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Callback;

import java.time.LocalTime;
import java.util.List;

public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    public static SummaryUI create(ObservableValue<List<Journey>> journeysO, ObservableValue<LocalTime> depTimeO) {
        ListView<Journey> listView = new ListView<>();
        listView.getStyleClass().add("summary-list");

        journeysO.addListener((obs, oldList, newList) -> {
            ObservableList<Journey> observableList = FXCollections.observableArrayList(newList);
            listView.setItems(observableList);
        });

        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Journey> call(ListView<Journey> param) {
                return new JourneyCell();
            }
        });

        depTimeO.addListener((obs, oldTime, newTime) -> {
            List<Journey> journeys = journeysO.getValue();
            if (journeys != null) {
                Journey selected = journeys.stream()
                        .filter(j -> !j.departureTime().isBefore(newTime))
                        .findFirst()
                        .orElse(journeys.get(journeys.size() - 1));
                listView.getSelectionModel().select(selected);
            }
        });

        ObservableValue<Journey> selectedJourneyO = listView.getSelectionModel().selectedItemProperty();

        return new SummaryUI(listView, selectedJourneyO);
    }

    private static class JourneyCell extends ListCell<Journey> {
        private final Pane root;
        private final Line line;
        private final Circle depCircle;
        private final Circle arrCircle;

        public JourneyCell() {
            root = new Pane();
            line = new Line();
            depCircle = new Circle(5);
            arrCircle = new Circle(5);

            depCircle.getStyleClass().add("dep-arr");
            arrCircle.getStyleClass().add("dep-arr");

            root.getChildren().addAll(line, depCircle, arrCircle);
        }

        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);
            if (empty || journey == null) {
                setGraphic(null);
            } else {
                line.setStartX(10);
                line.setEndX(200);
                line.setStartY(20);
                line.setEndY(20);

                depCircle.setCenterX(10);
                depCircle.setCenterY(20);

                arrCircle.setCenterX(200);
                arrCircle.setCenterY(20);

                root.getChildren().removeIf(node -> node instanceof Circle && node != depCircle && node != arrCircle);
                journey..forEach(transfer -> {
                    Circle transferCircle = new Circle(3);
                    transferCircle.getStyleClass().add("transfer");
                    transferCircle.setCenterX(calculatePosition(transfer, journey));
                    transferCircle.setCenterY(20);
                    root.getChildren().add(transferCircle);
                });

                setGraphic(root);
            }
        }

        private double calculatePosition(LocalTime transferTime, Journey journey) {
            double totalDuration = journey.arrTime().toSecondOfDay() - journey.depTime().toSecondOfDay();
            double transferOffset = transferTime.toSecondOfDay() - journey.depTime().toSecondOfDay();
            return 10 + (transferOffset / totalDuration) * 190;
        }
    }
}