package moovit;

public class Student extends Passenger {
    public Student(String name, int age, String paymentMethod) {
        super(name, age, paymentMethod, "Öğrenci");
    }

    @Override
    public double calculateFare(double baseFare) {
        double discounted = baseFare * 0.5;
        return applyPaymentAdjustment(discounted);
    }
}
