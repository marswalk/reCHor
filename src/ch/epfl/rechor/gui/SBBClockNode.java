package ch.epfl.rechor.gui;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

import java.util.Calendar;

/**
 * Represents the SBB Clock as a reusable JavaFX Node.
 * This clock features the iconic SBB design with its characteristic
 * seconds hand movement and minute jump.
 * It uses SVG paths for rendering the clock face and hands.
 * This version displays the light background clock face.
 *
 * @author Guanting Wen (392412)
 */
public class SBBClockNode {

    // --- Configuration ---
    private static final double SVG_VIEWBOX_SIZE = 112.0; // Native size of SVG elements
    private static final double PIVOT_X = 56.0; // Rotation pivot X in SVG coordinates
    private static final double PIVOT_Y = 56.0; // Rotation pivot Y in SVG coordinates

    private static final double EASING_DURATION_MS = 2000.0; // Duration for minute/hour hand animation
    private static final double SECONDS_CYCLE_DURATION_S = 58.5; // SBB seconds hand cycle

    // --- SVG Path Data for LIGHT FACE ---
    // 1. White background circle
    private static final String SVG_FACE_BACKGROUND_CIRCLE_FILL = "rgba(255,255,255,1)"; // White
    private static final double FACE_BACKGROUND_CIRCLE_CX = 56;
    private static final double FACE_BACKGROUND_CIRCLE_CY = 56;
    private static final double FACE_BACKGROUND_CIRCLE_R = 52.5;

    // 2. Grey border for the light face
    private static final String SVG_FACE_LIGHT_BORDER_D = "M56,3.5c28.995,0,52.5,23.505,52.5,52.5S84.995,108.5,56,108.5S3.5,84.995,3.5,56 S27.005,3.5,56,3.5 M56,2C26.224,2,2,26.224,2,56s24.224,54,54,54s54-24.224,54-54S85.776,2,56,2L56,2z";
    private static final String SVG_FACE_LIGHT_BORDER_FILL = "rgba(118,118,118,1)"; // Grey

    // 3. Black markers (same as faceDark)
    private static final String SVG_MARKERS_D = "M51.627,7.693l0.366,3.481l-1.392,0.146l-0.366-3.481L51.627,7.693z M54.25,19.5h3.5v-12h-3.5V19.5z M54.25,104.499h3.5v-12h-3.5V104.499z M72.734,23.515l3.031,1.75l6-10.392l-3.031-1.75L72.734,23.515z M30.234,97.126l3.031,1.75l6-10.392l-3.031-1.75L30.234,97.126z M88.484,39.266l10.392-6l-1.75-3.031l-10.392,6L88.484,39.266z M23.515,72.734l-10.392,6l1.75,3.031l10.392-6L23.515,72.734z M92.499,54.25v3.5h12v-3.5H92.499z M19.5,54.25h-12v3.5h12V54.25z M86.734,75.766l10.392,6l1.75-3.031l-10.392-6L86.734,75.766z M25.265,36.234l-10.392-6l-1.75,3.031l10.392,6L25.265,36.234z M72.734,88.484l6,10.392l3.031-1.75l-6-10.392L72.734,88.484z M30.234,14.873l6,10.392l3.031-1.75l-6-10.392L30.234,14.873z M60.007,11.174l1.392,0.146l0.366-3.481l-1.392-0.146L60.007,11.174z M50.233,104.161l1.392,0.146l0.366-3.481l-1.392-0.146L50.233,104.161z M64.672,11.838l1.369,0.291l0.728-3.424l-1.369-0.291L64.672,11.838z M45.231,103.294l1.369,0.291l0.728-3.424l-1.369-0.291L45.231,103.294zM69.24,12.986l1.331,0.433l1.082-3.329l-1.331-0.433L69.24,12.986z M40.346,101.91l1.331,0.433l1.082-3.329l-1.331-0.433L40.346,101.91z M76.366,11.978l-1.279-0.569l-1.424,3.197l1.279,0.569L76.366,11.978z M35.633,100.021l1.279,0.569l1.424-3.197l-1.279-0.569L35.633,100.021z M81.883,19.183l1.133,0.823l2.057-2.832l-1.133-0.823L81.883,19.183z M26.925,94.826l1.133,0.823l2.057-2.832l-1.133-0.823L26.925,94.826z M88.973,20.425l-1.04-0.937l-2.342,2.601l1.04,0.937L88.973,20.425z M23.026,91.574l1.04,0.937l2.342-2.601l-1.04-0.937L23.026,91.574z M92.51,24.067l-0.937-1.04l-2.601,2.342l0.937,1.04L92.51,24.067zM19.489,87.932l0.937,1.04l2.601-2.342l-0.937-1.04L19.489,87.932z M92.816,30.116l2.832-2.057l-0.823-1.133l-2.832,2.057L92.816,30.116z M19.183,81.883l-2.832,2.057l0.823,1.133l2.832-2.057L19.183,81.883z M97.395,38.337l3.197-1.424l-0.569-1.279l-3.197,1.424L97.395,38.337z M14.605,73.663l-3.197,1.424l0.569,1.279l3.197-1.424L14.605,73.663z M99.014,42.759l3.329-1.082l-0.433-1.331l-3.329,1.082L99.014,42.759z M12.986,69.24l-3.329,1.082l0.433,1.331l3.329-1.082L12.986,69.24z M100.161,47.328l3.424-0.728l-0.291-1.369l-3.424,0.728L100.161,47.328z M11.838,64.672l-3.424,0.728l0.291,1.369l3.424-0.728L11.838,64.672zM100.827,51.993l3.481-0.366l-0.146-1.392l-3.481,0.366L100.827,51.993z M11.174,60.007l-3.481,0.366l0.146,1.392l3.481-0.366L11.174,60.007z M100.68,61.399l3.481,0.366l0.146-1.392l-3.481-0.366L100.68,61.399z M11.319,50.6l-3.481-0.366l-0.146,1.392l3.481,0.366L11.319,50.6z M99.87,66.04l3.424,0.728l0.291-1.369l-3.424-0.728L99.87,66.04z M12.129,45.96l-3.424-0.728l-0.291,1.369l3.424,0.728L12.129,45.96z M98.581,70.572l3.329,1.082l0.433-1.331l-3.329-1.082L98.581,70.572z M13.418,41.428l-3.329-1.082l-0.432,1.332l3.329,1.082L13.418,41.428z M96.824,74.942l3.197,1.424l0.569-1.279l-3.197-1.424L96.824,74.942zM15.174,37.058l-3.197-1.424l-0.569,1.279l3.197,1.424L15.174,37.058z M91.995,83.016l2.832,2.057l0.823-1.133l-2.832-2.057L91.995,83.016z M20.006,28.983l-2.832-2.057L16.35,28.06l2.832,2.057L20.006,28.983z M88.973,86.63l2.601,2.342l0.937-1.04L89.91,85.59L88.973,86.63z M23.026,25.368l-2.601-2.342l-0.937,1.04l2.601,2.342L23.026,25.368z M85.59,89.91l2.342,2.601l1.04-0.937l-2.342-2.601L85.59,89.91z M23.026,20.425l2.342,2.601l1.04-0.937l-2.342-2.601L23.026,20.425z M81.883,92.816l2.057,2.832l1.133-0.823l-2.057-2.832L81.883,92.816z M26.925,17.174l2.057,2.832l1.133-0.823l-2.057-2.832L26.925,17.174zM73.663,97.393l1.424,3.197l1.279-0.569l-1.424-3.197L73.663,97.393z M38.337,14.605l-1.424-3.197l-1.279,0.569l1.424,3.197L38.337,14.605z M69.24,99.014l1.082,3.329l1.331-0.433l-1.082-3.329L69.24,99.014z M40.346,10.09l1.082,3.329l1.331-0.433l-1.081-3.328L40.346,10.09z M64.672,100.161l0.728,3.424l1.369-0.291l-0.728-3.424L64.672,100.161z M45.231,8.706l0.728,3.424l1.369-0.291l-0.728-3.424L45.231,8.706z M60.007,100.827l0.366,3.481l1.392-0.146l-0.366-3.481L60.007,100.827z";
    private static final String SVG_MARKERS_FILL = "rgba(30,30,30,1)"; // Black

    // --- Hands (same for light/dark face) ---
    private static final String SVG_HOURS_HAND_D = "M 59.2 68 L 52.8 68 L 53.4 24 L 58.6 24 Z";
    private static final String SVG_HOURS_HAND_FILL = "rgba(0,0,0,1)"; // Black

    private static final String SVG_MINUTES_HAND_D = "M 58.6 68 L 53.4 68 L 54.2 10 L 57.8 10 Z";
    private static final String SVG_MINUTES_HAND_FILL = "rgba(0,0,0,1)"; // Black

    private static final String SVG_SECONDS_HAND_D = "M61.25,24.8c0-2.899-2.351-5.25-5.25-5.25s-5.25,2.351-5.25,5.25c0,2.66,1.985,4.834,4.55,5.179V72.5h1.4V29.979C59.265,29.634,61.25,27.46,61.25,24.8z";
    private static final String SVG_SECONDS_HAND_FILL = "rgba(235,0,0,1)"; // Red

    // --- JavaFX Nodes ---
    private Rotate hourRotate, minuteRotate, secondRotate;
    private final Group clockPartsGroup; // Holds the SVG elements, native size 112x112
    private final StackPane scalableWrapper; // This is the node returned by getView()
    private final Scale scaleTransform;
    private final AnimationTimer timer;

    // --- Animation State ---
    private double currentHourAngle = 0;
    private double currentMinuteAngle = 0;
    private int lastKnownMinute = -1;
    private long minuteAnimationStartMillis = 0;
    private double startHourAngleForAnimation = 0;
    private double startMinuteAngleForAnimation = 0;

    /**
     * Constructs a new SBBClockNode.
     * The clock will have a default preferred size and will scale its contents
     * to fit within the bounds allocated by its parent.
     */
    public SBBClockNode() {
        clockPartsGroup = new Group();

        // 1. Face Background (White Circle)
        Circle faceBackground = new Circle(FACE_BACKGROUND_CIRCLE_CX, FACE_BACKGROUND_CIRCLE_CY, FACE_BACKGROUND_CIRCLE_R);
        faceBackground.setFill(Color.web(SVG_FACE_BACKGROUND_CIRCLE_FILL));
        clockPartsGroup.getChildren().add(faceBackground);

        // 2. Face Border (Grey Path for Light version)
        SVGPath faceBorder = new SVGPath();
        faceBorder.setContent(SVG_FACE_LIGHT_BORDER_D);
        faceBorder.setFill(Color.web(SVG_FACE_LIGHT_BORDER_FILL));
        clockPartsGroup.getChildren().add(faceBorder);

        // 3. Face Markers (Black)
        SVGPath markers = new SVGPath();
        markers.setContent(SVG_MARKERS_D);
        markers.setFill(Color.web(SVG_MARKERS_FILL));
        clockPartsGroup.getChildren().add(markers);

        // 4. Hour Hand
        hourRotate = new Rotate(0, PIVOT_X, PIVOT_Y);
        SVGPath hourHand = createHand(SVG_HOURS_HAND_D, Color.web(SVG_HOURS_HAND_FILL), hourRotate);
        clockPartsGroup.getChildren().add(hourHand);

        // 5. Minute Hand
        minuteRotate = new Rotate(0, PIVOT_X, PIVOT_Y);
        SVGPath minuteHand = createHand(SVG_MINUTES_HAND_D, Color.web(SVG_MINUTES_HAND_FILL), minuteRotate);
        clockPartsGroup.getChildren().add(minuteHand);

        // 6. Second Hand
        secondRotate = new Rotate(0, PIVOT_X, PIVOT_Y);
        SVGPath secondHand = createHand(SVG_SECONDS_HAND_D, Color.web(SVG_SECONDS_HAND_FILL), secondRotate);
        clockPartsGroup.getChildren().add(secondHand);

        // --- Initial time setup ---
        Calendar initialCal = Calendar.getInstance();
        int initialH = initialCal.get(Calendar.HOUR_OF_DAY);
        int initialM = initialCal.get(Calendar.MINUTE);
        int initialS = initialCal.get(Calendar.SECOND);
        int initialMs = initialCal.get(Calendar.MILLISECOND);

        currentMinuteAngle = (initialM * 6.0) % 360;
        currentHourAngle = ((initialH % 12 + initialM / 60.0) * 30.0) % 360;
        lastKnownMinute = initialM;

        minuteRotate.setAngle(currentMinuteAngle);
        hourRotate.setAngle(currentHourAngle);

        double initialTotalSecondsWithFraction = initialS + initialMs / 1000.0;
        if (initialTotalSecondsWithFraction < SECONDS_CYCLE_DURATION_S) {
            secondRotate.setAngle((initialTotalSecondsWithFraction / SECONDS_CYCLE_DURATION_S) * 360.0);
        } else {
            secondRotate.setAngle(0.0); // Pause position
        }

        // --- Scaling Setup ---
        scaleTransform = new Scale(1, 1);
        scaleTransform.setPivotX(PIVOT_X);
        scaleTransform.setPivotY(PIVOT_Y);
        clockPartsGroup.getTransforms().add(scaleTransform);

        scalableWrapper = new StackPane(clockPartsGroup);
        scalableWrapper.setAlignment(Pos.CENTER);
        // Bind the scaleTransform to the size of scalableWrapper.
        // This makes the clockPartsGroup scale to fit inside scalableWrapper.
        scaleTransform.xProperty().bind(
                Bindings.min(scalableWrapper.widthProperty(), scalableWrapper.heightProperty()).divide(SVG_VIEWBOX_SIZE)
        );
        scaleTransform.yProperty().bind(
                Bindings.min(scalableWrapper.widthProperty(), scalableWrapper.heightProperty()).divide(SVG_VIEWBOX_SIZE)
        );
        // Default preferred size, can be overridden by parent layout.
        scalableWrapper.setPrefSize(100, 100);
        // Ensure the StackPane itself doesn't add extra background if not desired
        scalableWrapper.setStyle("-fx-background-color: transparent;");


        // --- Animation Timer ---
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Calendar cal = Calendar.getInstance();
                int h = cal.get(Calendar.HOUR_OF_DAY);
                int m = cal.get(Calendar.MINUTE);
                int s = cal.get(Calendar.SECOND);
                int ms = cal.get(Calendar.MILLISECOND);

                double totalSecondsWithFraction = s + ms / 1000.0;
                double secondsAngle;

                if (totalSecondsWithFraction < SECONDS_CYCLE_DURATION_S) {
                    secondsAngle = (totalSecondsWithFraction / SECONDS_CYCLE_DURATION_S) * 360.0;
                } else {
                    secondsAngle = 0.0; // Pause period
                }
                secondRotate.setAngle(secondsAngle % 360);

                if (m != lastKnownMinute) {
                    startMinuteAngleForAnimation = currentMinuteAngle;
                    startHourAngleForAnimation = currentHourAngle;
                    minuteAnimationStartMillis = System.currentTimeMillis();
                    lastKnownMinute = m;
                }

                if (minuteAnimationStartMillis > 0) {
                    long animationProgressMs = System.currentTimeMillis() - minuteAnimationStartMillis;
                    double changeForMinute = 6.0;
                    double changeForHour = 0.5;

                    if (animationProgressMs < EASING_DURATION_MS) {
                        currentMinuteAngle = easeOutElastic(animationProgressMs, startMinuteAngleForAnimation, changeForMinute, EASING_DURATION_MS);
                        currentHourAngle = easeOutElastic(animationProgressMs, startHourAngleForAnimation, changeForHour, EASING_DURATION_MS);
                    } else {
                        currentMinuteAngle = (m * 6.0) % 360;
                        currentHourAngle = ((h % 12 + m / 60.0) * 30.0) % 360;
                        minuteAnimationStartMillis = 0;
                    }
                }
                minuteRotate.setAngle(currentMinuteAngle % 360);
                hourRotate.setAngle(currentHourAngle % 360);
            }
        };
        startAnimation(); // Start the animation by default
    }

    /**
     * Gets the JavaFX Node representing the clock.
     * This node can be added to a JavaFX scene graph.
     * @return The clock Node.
     */
    public Node getView() {
        return scalableWrapper;
    }

    /**
     * Starts the clock animation.
     */
    public void startAnimation() {
        if (timer != null) {
            timer.start();
        }
    }

    /**
     * Stops the clock animation.
     * It's good practice to call this when the clock is no longer visible
     * or when the application is closing, to free up resources.
     */
    public void stopAnimation() {
        if (timer != null) {
            timer.stop();
        }
    }

    private SVGPath createHand(String svgData, Color color, Rotate rotateTransform) {
        SVGPath hand = new SVGPath();
        hand.setContent(svgData);
        hand.setFill(color);
        hand.getTransforms().add(rotateTransform);
        return hand;
    }

    private double easeOutElastic(double timeMs, double startValue, double changeInValue, double durationMs) {
        if (timeMs <= 0) return startValue;
        if (timeMs >= durationMs) return startValue + changeInValue;
        double normalizedTime = timeMs / durationMs;
        return changeInValue * Math.pow(2, -10 * normalizedTime) *
                Math.sin((timeMs - 2.0) * (2 * Math.PI) / 300.0) * 1.5 +
                changeInValue + startValue;
    }
}
