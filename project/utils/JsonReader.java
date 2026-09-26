package moovit.utils;

import moovit.Stop;
import moovit.NextStop;
import moovit.Transfer;
import moovit.Taxi;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {
    private static Taxi taxi; // Taksi nesnesi saklanır

    public static List<Stop> getStops() {
        List<Stop> stopList = new ArrayList<>();
        try {
            FileReader reader = new FileReader("src/project/utils/data.json");
            JSONObject jsonData = new JSONObject(new JSONTokener(reader));

            JSONObject jsontaxi = jsonData.getJSONObject("taxi");
            taxi = new Taxi(); // global değişken olarak ata
            taxi.setopeningFee(jsontaxi.getDouble("openingFee"));
            taxi.setCostPerKm(jsontaxi.getDouble("costPerKm"));

            JSONArray duraklar = jsonData.getJSONArray("duraklar");
            for (int i = 0; i < duraklar.length(); i++) {
                JSONObject jsonStop = duraklar.getJSONObject(i);
                Stop stop = new Stop();

                stop.setId(jsonStop.getString("id"));
                stop.setName(jsonStop.getString("name"));
                stop.setType(jsonStop.getString("type"));
                stop.setLat(jsonStop.getDouble("lat"));
                stop.setLon(jsonStop.getDouble("lon"));
                stop.setSonDurak(jsonStop.getBoolean("sonDurak"));

                List<NextStop> nextStops = new ArrayList<>();
                JSONArray nextStopsArray = jsonStop.getJSONArray("nextStops");
                for (int j = 0; j < nextStopsArray.length(); j++) {
                    JSONObject jsonNextStop = nextStopsArray.getJSONObject(j);
                    NextStop nextStop = new NextStop();
                    nextStop.setStopId(jsonNextStop.getString("stopId"));
                    nextStop.setMesafe(jsonNextStop.getDouble("mesafe"));
                    nextStop.setSure(jsonNextStop.getInt("sure"));
                    nextStop.setUcret(jsonNextStop.getDouble("ucret"));
                    nextStops.add(nextStop);
                }
                stop.setNextStops(nextStops);

                if (!jsonStop.isNull("transfer")) {
                    JSONObject jsonTransfer = jsonStop.getJSONObject("transfer");
                    Transfer transfer = new Transfer();
                    transfer.setTransferStopId(jsonTransfer.getString("transferStopId"));
                    transfer.setTransferSure(jsonTransfer.getInt("transferSure"));
                    transfer.setTransferUcret(jsonTransfer.getDouble("transferUcret"));
                    stop.setTransfer(transfer);
                }

                stopList.add(stop);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stopList;
    }

    public static Taxi getTaxi() {
        if (taxi == null) {
            getStops(); // getStops içinde zaten taxi oluşturuluyor
        }
        return taxi;
    }
}
