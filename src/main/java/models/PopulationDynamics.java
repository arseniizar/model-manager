package models;

import annotations.Bind;

public class PopulationDynamics extends Model {
    @Bind
    private double[] birthRate, deathRate, migrationRate; // population dynamics rates
    @Bind
    private double[] population; // population over time
    private double temp; // auxiliary field, not bound to input/output

    public PopulationDynamics() {
        super("PopulationDynamics", "This model studies the " +
                "changes in population size over time. " +
                "It incorporates factors such as birth rates, death rates, " +
                "immigration, and emigration. " +
                "The model can be applied to predict population trends " +
                "and their implications for resource management and urban planning.");
    }

    @Override
    public void run() {
        population = new double[LL];
        for (int t = 1; t < LL; t++) {
            population[t] = population[t - 1]
                    + (birthRate[t] - deathRate[t] + migrationRate[t]) * population[t - 1];
        }
    }
}
