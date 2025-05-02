package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehicleIconsTest {

    @Test
    public void testIconLoading() {
        // Test that icons can be loaded for each vehicle type
        for (Vehicle vehicle : Vehicle.values()) {
            Image icon = VehicleIcons.iconFor(vehicle);
            assertNotNull(icon, "Icon for " + vehicle + " should not be null");
            assertFalse(icon.isError(), "Icon for " + vehicle + " should load without errors");
        }
    }

    /**
     * Visual test that must be run manually to display icons
     */
    public static class IconVisualTest extends Application {
        @Override
        public void start(Stage primaryStage) {
            HBox iconDisplay = new HBox(10);

            for (Vehicle vehicle : Vehicle.values()) {
                Image icon = VehicleIcons.iconFor(vehicle);
                ImageView imageView = new ImageView(icon);
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                iconDisplay.getChildren().add(imageView);
            }

            Scene scene = new Scene(iconDisplay);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Vehicle Icons Test");
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }
}