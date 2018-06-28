package StationSim;

import java.util.ArrayList;
import java.util.List;

public class LocationWeightedAbstractSimState {

    private static final double sigmaSpace = 1.0;
    private static final double sigmaVelocity = 1.0;
    private double weightedMeanDensity;
    private double weightedMeanVelocity;
    private double weightedMeanTurbulentVelocity;

    public LocationWeightedAbstractSimState(Station station, double x, double y, double s) {
        calculate(station, x, y, s);
    }

    public double getWeightedMeanDensity() {
        return weightedMeanDensity;
    }

    public double getWeightedMeanVelocity() {
        return weightedMeanVelocity;
    }

    public double getWeightedMeanTurbulentVelocity() {
        return weightedMeanTurbulentVelocity;
    }

    public void calculate(Station station, double x, double y, double s) {
//        double twoSigmaSq = 2.0 * sigma * sigma;
//        double oneOverRootTwoPiSigmaSq = 1.0 / Math.sqrt(Math.PI * twoSigmaSq);

//        List<Person> people = getPeople(station);

//        double sumDensity = 0.0;

//        for (Person p : people) {
//            double dx = p.location.getX() - x;
//            double dy = p.location.getY() - y;
//            double ds = p.getDesiredSpeed() - s;
//            double density = oneOverRootTwoPiSigmaSq * Math.exp(-(dx * dx + dy * dy + ds * ds) / twoSigmaSq);
//            sumDensity += density;
//        }
//
//        weightedMeanDensity = sumDensity;
//        weightedMeanVelocity = sumVelocity / people.size();
//
//        double sumTurbulentVelocity = 0.0;
//
//        for (double v : velocities) {
//            sumTurbulentVelocity = Math.pow(v - weightedMeanVelocity, 2.0);
//        }
//
//        weightedMeanTurbulentVelocity = sumTurbulentVelocity / people.size();
    }

    public List<Person> getPeople(Station station) {
        List<Person> people = new ArrayList<>();
        for (Object o : station.area.allObjects) {
            people.add((Person)o);
        }
        return people;
    }
}
