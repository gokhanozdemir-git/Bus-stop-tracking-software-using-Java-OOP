package moovit;

import javafx.application.Platform;

public class JavaConnector {
    private Controller controller;

    public JavaConnector(Controller controller) {
        this.controller = controller;
    }

    public void setStartLocation(double lat, double lon) {
        Platform.runLater(() -> controller.setStartLocation(lat, lon));
    }

    public void setEndLocation(double lat, double lon) {
        Platform.runLater(() -> controller.setEndLocation(lat, lon));
    }
   

}
