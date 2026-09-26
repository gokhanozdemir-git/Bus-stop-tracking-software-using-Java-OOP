package moovit;

public class Disabled extends Passenger {
    public Disabled(String name, int age, String paymentMethod) {
        super(name, age, paymentMethod, "Engelli");
    }

    @Override
    public double calculateFare(double baseFare) {
        return 0.0; // Her durumda bedava
    }
}
