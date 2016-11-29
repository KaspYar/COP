package application.classes;

public class Garage {

    private Car car;

    public Garage(Car car) {
        this.car = car;
    }

    @Override
    public String toString() {
        return "garage with car: "+ car.toString();
    }
}
