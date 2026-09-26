package moovit;

import javafx.scene.control.TextArea;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import moovit.utils.JsonReader;
import org.controlsfx.control.textfield.TextFields;
import org.json.JSONArray;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;
import java.util.List;
import java.util.Locale;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Pos;
import org.controlsfx.control.Notifications;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller {
    @FXML private AnchorPane baslangic;
    @FXML private WebView mapView;
    private WebEngine webEngine;

    @FXML private ComboBox<String> paymentComboBox;
    @FXML private TextField meslekId;
    @FXML private TextField name;
    @FXML private TextField age;
    @FXML private Button loginButton;
    @FXML private Button selectStartButton;
    @FXML private Button selectEndButton;
    @FXML private Button findRouteButton;
    @FXML private TextArea routeResultArea;
    @FXML private Button fastestRouteButton;
    private double startLat = 0, startLon = 0;
    private double endLat = 0, endLon = 0;
    private final String[] jobwords = {"Asker", "Öğretmen", "Öğrenci", "Engelli"};
    private Passenger currentPassenger;
    private List<Stop> stops;
    private Taxi taxi = JsonReader.getTaxi();

    @FXML
    private void initialize() {
        TextFields.bindAutoCompletion(meslekId, jobwords);
        paymentComboBox.getItems().addAll("Kent Kart", "Kredi Kart\u0131", "Nakit");

        loginButton.setOnAction(e -> processLogin());
        selectStartButton.setOnAction(e -> selectStartLocation());
        selectEndButton.setOnAction(e -> selectEndLocation());
        if(fastestRouteButton!=null) {
            fastestRouteButton.setOnAction(e -> findFastestRoute());
        }
        if (findRouteButton != null)
            findRouteButton.setOnAction(e -> findCheapestRouteAndPrint());

        setupWebView();
    }

    private void showError(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(5))
                .showError();
    }

    private void showSuccess(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(3))
                .showInformation();
    }

    private boolean validateInputs() {
        if (name.getText().trim().isEmpty()) {
            showError("Hata", "İsim-Soyisim alanı boş bırakılamaz!");
            name.requestFocus();
            return false;
        }

        String ageText = age.getText().trim();
        if (ageText.isEmpty()) {
            showError("Hata", "Yaş alanı boş bırakılamaz!");
            age.requestFocus();
            return false;
        }

        try {
            int ageValue = Integer.parseInt(ageText);
            if (ageValue < 0 || ageValue > 120) {
                showError("Hata", "Lütfen geçerli bir yaş giriniz (0-120)!");
                age.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Hata", "Yaş alanına sadece sayı girilmelidir!");
            age.requestFocus();
            return false;
        }

        if (meslekId.getText().trim().isEmpty()) {
            showError("Hata", "Meslek alanı boş bırakılamaz!");
            meslekId.requestFocus();
            return false;
        }

        if (!List.of(jobwords).contains(meslekId.getText().trim())) {
            showError("Hata", "Lütfen geçerli bir meslek seçiniz!\nGeçerli meslekler: Öğrenci, Öğretmen, Asker, Engelli");
            meslekId.requestFocus();
            return false;
        }

        if (paymentComboBox.getValue() == null) {
            showError("Hata", "Lütfen bir ödeme yöntemi seçiniz!");
            paymentComboBox.requestFocus();
            return false;
        }

        return true;
    }

    private void processLogin() {
        if (!validateInputs()) {
            return;
        }

        String userName = name.getText().trim();
        String userAgeStr = age.getText().trim();
        String userProfession = meslekId.getText().trim();
        String userPaymentMethod = paymentComboBox.getValue();

        int userAge = Integer.parseInt(userAgeStr);

        switch (userProfession) {
            case "Öğrenci":
                currentPassenger = new Student(userName, userAge, userPaymentMethod);
                break;
            case "Asker":
                currentPassenger = new Soldier(userName, userAge, userPaymentMethod);
                break;
            case "Öğretmen":
                currentPassenger = new Teacher(userName, userAge, userPaymentMethod);
                break;
            case "Engelli":
                currentPassenger = new Disabled(userName, userAge, userPaymentMethod);
                break;
            default:
                showError("Hata", "Bilinmeyen meslek türü!");
                return;
        }

        findRouteButton.setDisable(false);
        fastestRouteButton.setDisable(false);
        showSuccess("Başarılı", "Giriş başarıyla yapıldı!");
    }

    private void setupWebView() {
        webEngine = mapView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        JavaConnector connector = new JavaConnector(this);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", connector);
                webEngine.executeScript("addStopsToMap('" + getStopsJson() + "')");
            }
        });

        String mapPath = getClass().getResource("/moovit/map.html").toExternalForm();
        if (mapPath == null) {
            System.out.println("\u274c HATA: map.html dosyas\u0131 bulunamad\u0131!");
        } else {
            webEngine.load(mapPath);
        }
    }

    private String getStopsJson() {
        stops = JsonReader.getStops();
        JSONArray stopsArray = new JSONArray();
        for (Stop stop : stops) {
            stopsArray.put(stop.toJSON());
        }
        return stopsArray.toString();
    }

    public void setStartLocation(double lat, double lon) {
        this.startLat = lat;
        this.startLon = lon;
    }

    public void setEndLocation(double lat, double lon) {
        this.endLat = lat;
        this.endLon = lon;
    }

    private void selectStartLocation() {
        webEngine.executeScript("selectStartPoint()");
    }

    private void selectEndLocation() {
        webEngine.executeScript("selectEndPoint()");
    }

    private Stop findNearestStop(double lat, double lon) {
        Stop nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Stop stop : stops) {
            double d = getDistance(lat, lon, stop.getLat(), stop.getLon());
            if (d < minDistance) {
                minDistance = d;
                nearest = stop;
            }
        }

        return nearest;
    }

    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        var R = 6371;
        var dLat = Math.toRadians(lat2 - lat1);
        var dLon = Math.toRadians(lon2 - lon1);
        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void findFastestRoute() {
        if (currentPassenger == null) {
            routeResultArea.setText("Lütfen önce kullanıcı bilgilerini doldurun!");
            return;
        }

        // Başlangıç ve hedef durak
        Stop startStop = findNearestStop(startLat, startLon);
        Stop endStop = findNearestStop(endLat, endLon);

        if (startStop == null || endStop == null) {
            routeResultArea.setText("Başlangıç veya hedef noktası geçersiz.");
            return;
        }

        if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript("clearAllLines();");
        }

        // Tam mesafeyi hesapla
        double fullDistance = getDistance(startLat, startLon, endLat, endLon);

        // Taksi süresi ve ücreti
        double time = (fullDistance / 50.0) * 60.0;
        double cost = taxi.getopeningFee() + (fullDistance * taxi.getCostPerKm());

        double adjustedFare = currentPassenger.calculateFare(cost);

        // Rota çizimi
        if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            String js = String.format(Locale.ENGLISH,
                    "drawRouteLine(%f, %f, %f, %f, '%s');",
                    startLat, startLon, endLat, endLon, "yellow");
            webEngine.executeScript(js);
        }

        // Sonuçları yazdır
        String output = String.format(
            "\uD83D\uDE97 EN HIZLI ROTA (Taksi):\n" +
            "Mesafe: %.2f km\n" +
            "Süre: %.1f dk\n" +
            "Ücret: %.2f TL (İndirimli)\n",
            fullDistance, time, adjustedFare
        );

        routeResultArea.setText(output);
    }

    @FXML
    private void findCheapestRouteAndPrint() {
    	  if (currentPassenger == null) {
    	        routeResultArea.setText("Lütfen önce kullanıcı bilgilerini doldurup 'Giriş Yap' butonuna tıklayın!");
    	        return;
    	    }
        Stop startStop = findNearestStop(startLat, startLon);
        Stop endStop = findNearestStop(endLat, endLon);

        if (startStop == null || endStop == null) {
            System.out.println("Başlangıç veya varış durağı bulunamadı!");
            return;
        }

        // 🔄 Her seferinde eski çizimleri temizle
        if (webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
            webEngine.executeScript("clearAllLines();");
        }
        StringBuilder routeOutput = new StringBuilder();
        // 🚕 Taksi ile başlama kontrolü
        double mesafe = getDistance(startLat, startLon, startStop.getLat(), startStop.getLon());

        if (mesafe > 3.0) {
            double taxiCost = taxi.getopeningFee() + (mesafe * taxi.getCostPerKm());
            double taxiTime = (mesafe / 50.0) * 60.0;

            String taksiBilgisi = String.format(
                "Kullanıcı durağa TAKSİ ile gitti:\nMesafe: %.2f km | Süre: %.1f dk | Ücret: %.2f TL\n\n",
                mesafe, taxiTime, taxiCost
            );
            routeOutput.append(taksiBilgisi);

            // 🟡 Sarı çizgiyi haritada çiz
            if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            	String jsCode = String.format(Locale.ENGLISH,
            		    "drawRouteLine(%f, %f, %f, %f, '%s');",
            		    startLat, startLon, startStop.getLat(), startStop.getLon(), "yellow"
            		);
                webEngine.executeScript(jsCode);
            } else {
                System.out.println("WebView harita henüz hazır değil, çizim ertelendi.");
            }

        }
     // 🚏 En ucuz rotayı bul
        RouteFinder finder = new RouteFinder(stops, taxi);
        finder.setStartCoordinates(startLat, startLon);
        finder.setEndCoordinates(endLat, endLon);
        finder.setWebEngine(webEngine);
        String routeDetails = finder.findCheapestRoute(startStop, endStop, currentPassenger);
        routeOutput.append(routeDetails);

        // 🟨 VARIŞ noktasından hedefe olan mesafe kontrolü
        double endMesafe = getDistance(endLat, endLon, endStop.getLat(), endStop.getLon());
        if (endMesafe > 3.0) {
            double taxiCost = taxi.getopeningFee() + (endMesafe * taxi.getCostPerKm());
            double taxiTime = (endMesafe / 50.0) * 60.0;
            double adjustedFare = currentPassenger.calculateFare(taxiCost);

            routeOutput.append(String.format(
                "\nKullanıcı son duraktan hedef konuma TAKSİ ile gitti:\nMesafe: %.2f km | Süre: %.1f dk | Ücret: %.2f TL\n",
                endMesafe, taxiTime, adjustedFare
            ));
            // Haritada sarı çizgi çiz
            if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
                String jsCode = String.format(Locale.ENGLISH,
                        "drawRouteLine(%f, %f, %f, %f, '%s');",
                        endStop.getLat(), endStop.getLon(), endLat, endLon, "yellow");
                webEngine.executeScript(jsCode);
            }
        }
        routeResultArea.setText(routeOutput.toString());
    }
}
