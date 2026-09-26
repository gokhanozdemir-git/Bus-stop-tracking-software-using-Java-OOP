package moovit;

import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

public class Stop {
    private String id;
    private String name;
    private String type;
    private double lat;
    private double lon;
    private boolean sonDurak;
    private List<NextStop> nextStops;
    private Transfer transfer;

    // Getter ve Setter metodları
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public boolean isSonDurak() { return sonDurak; }
    public void setSonDurak(boolean sonDurak) { this.sonDurak = sonDurak; }

    public List<NextStop> getNextStops() { return nextStops; }
    public void setNextStops(List<NextStop> nextStops) { this.nextStops = nextStops; }

    public Transfer getTransfer() { return transfer; }
    public void setTransfer(Transfer transfer) { this.transfer = transfer; }
    
    public JSONObject toJSON() {
        JSONObject jsonStop = new JSONObject();
        jsonStop.put("name", this.name);
        jsonStop.put("type", this.type);
        jsonStop.put("lat", this.lat);
        jsonStop.put("lon", this.lon);
        
        JSONArray nextStopsArray = new JSONArray();
        if (this.nextStops != null) {
            for (NextStop nextStop : this.nextStops) {
                nextStopsArray.put(nextStop.toJSON());
            }
        }
        jsonStop.put("nextStops", nextStopsArray);

        // **Transfer Bilgisi JSON'a ekleniyor**
        if (this.transfer != null) {
            jsonStop.put("transfer", this.transfer.toJSON());
        } else {
            jsonStop.put("transfer", JSONObject.NULL);
        }

        return jsonStop;
    }

    @Override
    public String toString() {
        return name + " (" + type + ") - Koordinatlar: (" + lat + ", " + lon + ")";
    }
}
