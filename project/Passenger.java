package moovit;

public abstract class Passenger {
    protected String name;
    protected int age;
    protected String paymentMethod;
    protected String profession;
   
    public Passenger(String name, int age, String paymentMethod, String profession) {
        this.name = name;
        this.age = age;
        this.paymentMethod = paymentMethod;
        this.profession = profession;
    }

    public abstract double calculateFare(double baseFare);

    protected double applyPaymentAdjustment(double fare) {
        if (!paymentMethod.equalsIgnoreCase("Kent Kart")) {
            return fare * 1.5; 
        }
        return fare;
    }

    public void printPassengerInfo() {
        System.out.println("Yolcu Adı: " + name);
        System.out.println("Yaş: " + age);
        System.out.println("Meslek: " + profession);
        System.out.println("Ödeme Yöntemi: " + paymentMethod);
    }
}
