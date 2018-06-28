package StationSim;

import java.util.ArrayList;
import java.util.List;

public class AbstractState {

    private static final double sigmaSpace = 1.0;
    private static final double sigmaVelocity = 1.0;

    private double weightedMeanDensity;

    public AbstractState(Station station, double x, double y, double s) {
        calculate(station, x, y, s);
    }

    public double getWeightedMeanDensity() {
        return weightedMeanDensity;
    }

    public void calculate(Station station, double x, double y, double s) {
        double twoSigmaSpaceSq = 2.0 * sigmaSpace * sigmaSpace;
        double twoSigmaVelocitySq = 2.0 * sigmaSpace * sigmaSpace;
        double oneOverRootTwoPiSigmaSq = 1.0 / Math.sqrt(Math.PI * twoSigmaSpaceSq * sigmaVelocity);

        List<Person> people = getPeople(station);

        double sumDensity = 0.0;

        for (Person p : people) {
            double dx = p.location.getX() - x;
            double dy = p.location.getY() - y;
            double ds = p.getDesiredSpeed() - s;
            double density = oneOverRootTwoPiSigmaSq * Math.exp((-(dx * dx + dy * dy) / twoSigmaSpaceSq) + (-(ds * ds) / twoSigmaVelocitySq));
            sumDensity += density;
        }

        weightedMeanDensity = sumDensity;
    }

    public List<Person> getPeople(Station station) {
        List<Person> people = new ArrayList<>();
        for (Object o : station.area.allObjects) {
            people.add((Person)o);
        }
        return people;
    }
}
