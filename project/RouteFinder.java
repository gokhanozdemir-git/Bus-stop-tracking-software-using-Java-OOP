package moovit;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import java.util.*;

public class RouteFinder {
    private final List<Stop> allStops;
    private final Map<String, Stop> stopMap = new HashMap<>();
    private final Taxi taxi;
    private double startLat;
    private double startLon;
    private WebEngine webEngine;
    private double endLat;
    private double endLon;
    
    public RouteFinder(List<Stop> allStops, Taxi taxi) {
        this.allStops = allStops;
        this.taxi = taxi;
        for (Stop stop : allStops) {
            stopMap.put(stop.getId(), stop);
        }
    }
    public void setEndCoordinates(double lat, double lon) {
        this.endLat = lat;
        this.endLon = lon;
    }
    public void setStartCoordinates(double lat, double lon) {
        this.startLat = lat;
        this.startLon = lon;
    }

    public void setWebEngine(WebEngine engine) {
        this.webEngine = engine;
    }
    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        var R = 6371;
        var dLat = Math.toRadians(lat2 - lat1);
        var dLon = Math.toRadians(lon2 - lon1);
        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public String findCheapestRoute(Stop start, Stop end, Passenger passenger) {
        StringBuilder sb = new StringBuilder();
        Map<String, RouteInfo> routeInfoMap = new HashMap<>();
        PriorityQueue<StopCost> queue = new PriorityQueue<>(Comparator.comparingDouble(sc -> sc.cost));

        // Initialize route info for all stops
        for (Stop stop : allStops) {
            routeInfoMap.put(stop.getId(), new RouteInfo(Double.MAX_VALUE));
        }

        // Set starting point
        routeInfoMap.get(start.getId()).cost = 0.0;
        queue.add(new StopCost(start, 0.0));

        // Dijkstra's algorithm with modifications
        while (!queue.isEmpty()) {
            StopCost current = queue.poll();
            Stop currentStop = current.stop;

            // If we reached the end stop
            if (currentStop.getId().equals(end.getId())) break;

            // Process direct connections
            for (NextStop next : currentStop.getNextStops()) {
                Stop nextStop = stopMap.get(next.getStopId());
                processRoute(currentStop, nextStop, next.getUcret(), next.getMesafe(), next.getSure(), 
                           "normal", routeInfoMap, queue);
            }

            // Process transfers
            Transfer transfer = currentStop.getTransfer();
            if (transfer != null) {
                Stop transferStop = stopMap.get(transfer.getTransferStopId());
                processRoute(currentStop, transferStop, transfer.getTransferUcret(), 0.0, 
                           transfer.getTransferSure(), "transfer", routeInfoMap, queue);
            }
        }

        // If no route found to end stop, find best alternative
        if (routeInfoMap.get(end.getId()).cost == Double.MAX_VALUE) {
            // Find the closest reachable stop to destination
            Stop bestStop = null;
            double minDistance = Double.MAX_VALUE;
            
            for (Stop stop : allStops) {
                if (routeInfoMap.get(stop.getId()).cost != Double.MAX_VALUE) {
                    double distToEnd = getDistance(stop.getLat(), stop.getLon(), endLat, endLon);
                    if (distToEnd < minDistance) {
                        minDistance = distToEnd;
                        bestStop = stop;
                    }
                }
            }

            if (bestStop != null) {
                // Build route to best stop
                buildRoute(sb, start, bestStop, routeInfoMap, passenger);
                
                // Calculate final leg (taxi or walking)
                double finalDistance = getDistance(bestStop.getLat(), bestStop.getLon(), endLat, endLon);
                if (finalDistance > 3.0) {
                    // Use taxi
                    double taxiFare = taxi.calculateFare(finalDistance);
                    sb.append(String.format("\n🚕 TAKSİ ile son durağa: %.2f km, %.2f TL\n", 
                            finalDistance, taxiFare));
                    sendLineToMap(bestStop.getLat(), bestStop.getLon(), endLat, endLon, "yellow");
                } else {
                    // Walking
                    double walkingTime = (finalDistance / 6.0) * 60.0;
                    sb.append(String.format("\n Son durağa YÜRÜYÜŞ: %.2f km, %.1f dk\n", 
                            finalDistance, walkingTime));
                    sendLineToMap(bestStop.getLat(), bestStop.getLon(), endLat, endLon, "red");
                }
            }
        } else {
            // Build complete route if found
            buildRoute(sb, start, end, routeInfoMap, passenger);
            
            // Add final walking segment if needed for complete routes
            double finalWalkDistance = getDistance(end.getLat(), end.getLon(), endLat, endLon);
            if (finalWalkDistance <= 3.0) {
                double walkingTime = (finalWalkDistance / 6.0) * 60.0;
                sb.append(String.format("\n🚶‍♂️ Son noktaya YÜRÜYÜŞ: %.2f km, %.1f dk\n", 
                        finalWalkDistance, walkingTime));
                sendLineToMap(end.getLat(), end.getLon(), endLat, endLon, "red");
            }
        }

        // Add initial walking segment if needed
        double initialWalkDistance = getDistance(startLat, startLon, start.getLat(), start.getLon());
        if (initialWalkDistance <= 3.0) {
            double walkingTime = (initialWalkDistance / 6.0) * 60.0;
            sb.insert(0, String.format("🚶‍♂️ Başlangıç noktasından durağa YÜRÜYÜŞ: %.2f km, %.1f dk\n\n", 
                    initialWalkDistance, walkingTime));
            sendLineToMap(startLat, startLon, start.getLat(), start.getLon(), "red");
        }

        return sb.toString();
    }

    private void processRoute(Stop currentStop, Stop nextStop, double cost, double distance, 
                            int duration, String type, Map<String, RouteInfo> routeInfoMap, 
                            PriorityQueue<StopCost> queue) {
        double newCost = routeInfoMap.get(currentStop.getId()).cost + cost;
        RouteInfo nextInfo = routeInfoMap.get(nextStop.getId());
        
        if (newCost < nextInfo.cost) {
            nextInfo.cost = newCost;
            nextInfo.previousStop = currentStop;
            nextInfo.distance = distance;
            nextInfo.duration = duration;
            nextInfo.type = type;
            queue.add(new StopCost(nextStop, newCost));
        }
    }

    private void buildRoute(StringBuilder sb, Stop start, Stop end, 
                          Map<String, RouteInfo> routeInfoMap, Passenger passenger) {
        List<Stop> path = new ArrayList<>();
        Stop current = end;
        
        while (current != null && !current.getId().equals(start.getId())) {
            path.add(current);
            RouteInfo info = routeInfoMap.get(current.getId());
            current = info.previousStop;
        }
        path.add(start);
        Collections.reverse(path);

        sb.append("\nEN UYGUN ROTA:\n");
        double totalCost = 0;
        double totalDistance = 0;
        int totalDuration = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            Stop currentStop = path.get(i);
            Stop nextStop = path.get(i + 1);
            RouteInfo info = routeInfoMap.get(nextStop.getId());

            double adjustedCost = passenger.calculateFare(info.cost - routeInfoMap.get(currentStop.getId()).cost);
            totalCost += adjustedCost;
            totalDistance += info.distance;
            totalDuration += info.duration;

            sb.append(String.format("→ %s (%s) | [%s] %.1f km, %d dk, %.2f TL\n",
                    nextStop.getName(), nextStop.getType(), info.type,
                    info.distance, info.duration, adjustedCost));

            String color = info.type.equals("transfer") ? "blue" : "green";
            sendLineToMap(currentStop.getLat(), currentStop.getLon(), 
                        nextStop.getLat(), nextStop.getLon(), color);
        }

        sb.append(String.format("\nToplam Ücret: %.2f TL\n", totalCost));
        sb.append(String.format("Toplam Mesafe: %.2f km\n", totalDistance));
        sb.append(String.format("Toplam Süre: %d dakika\n", totalDuration));
    }

    private static class RouteInfo {
        double cost;
        Stop previousStop;
        double distance;
        int duration;
        String type;

        RouteInfo(double cost) {
            this.cost = cost;
            this.previousStop = null;
            this.distance = 0;
            this.duration = 0;
            this.type = "";
        }
    }

    private void sendLineToMap(double lat1, double lon1, double lat2, double lon2, String color) {
        if (webEngine != null) {
            Platform.runLater(() -> {
                String js = String.format(Locale.ENGLISH,
                        "drawRouteLine(%f, %f, %f, %f, '%s');",
                        lat1, lon1, lat2, lon2, color);
                webEngine.executeScript(js);
            });
        }
    }

    static class StopCost {
        Stop stop;
        double cost;

        StopCost(Stop stop, double cost) {
            this.stop = stop;
            this.cost = cost;
        }
    }
}
