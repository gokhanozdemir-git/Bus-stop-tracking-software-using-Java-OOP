package moovit;

public class Taxi {
	private double openingFee;
	private double costPerKm;
	
public double getopeningFee() { return openingFee; }
public void setopeningFee(double openingFee) { this.openingFee =openingFee; } 
	
public double getCostPerKm() {return costPerKm; }
public void setCostPerKm(double costPerKm) {this.costPerKm =costPerKm;}

public double calculateFare(double distance) {
	return openingFee + (distance * costPerKm);
}

}

