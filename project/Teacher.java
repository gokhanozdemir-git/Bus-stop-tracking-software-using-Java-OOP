package moovit;

public class Teacher extends Passenger {
    public Teacher(String name, int age, String paymentMethod) {
        super(name, age, paymentMethod, "Öğretmen");
    }

    @Override
    public double calculateFare(double baseFare) {
        double discounted = baseFare;
        return applyPaymentAdjustment(discounted);
    }
}
