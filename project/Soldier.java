package moovit;

public class Soldier extends Passenger {
    public Soldier(String name, int age, String paymentMethod) {
        super(name, age, paymentMethod, "Asker");
    }

    @Override
    public double calculateFare(double baseFare) {
        double discounted = 0.0; // Asker bedava
        return applyPaymentAdjustment(discounted);
    }
}
