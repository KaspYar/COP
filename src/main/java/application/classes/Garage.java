package application.classes;

public class Garage {

    private Car car;

    public Garage() {
    }

    public Garage(Car car) {
        this.car = car;
    }

    @Override
    public String toString() {
        return "garage with car: "+ car.toString();
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}
