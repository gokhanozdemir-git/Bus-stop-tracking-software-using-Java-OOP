package moovit;

import org.json.JSONObject;

public class NextStop {
    private String stopId;
    private double mesafe;
    private int sure;
    private double ucret;

    public String getStopId() { return stopId; }
    public void setStopId(String stopId) { this.stopId = stopId; }

    public double getMesafe() { return mesafe; }
    public void setMesafe(double mesafe) { this.mesafe = mesafe; }

    public int getSure() { return sure; }
    public void setSure(int sure) { this.sure = sure; }

    public double getUcret() { return ucret; }
    public void setUcret(double ucret) { this.ucret = ucret; }
    
    public JSONObject toJSON() {
        JSONObject jsonNextStop = new JSONObject();
        jsonNextStop.put("stopId", this.stopId);
        jsonNextStop.put("mesafe", this.mesafe);
        jsonNextStop.put("sure", this.sure);
        jsonNextStop.put("ucret", this.ucret);
        return jsonNextStop;
    }

    @Override
    public String toString() {
        return "NextStop{ stopId='" + stopId + "', mesafe=" + mesafe + " km, sure=" + sure + " dk, ucret=" + ucret + " TL }";
    }
}
