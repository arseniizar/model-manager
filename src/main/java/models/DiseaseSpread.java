package models;

import annotations.Bind;

public class DiseaseSpread extends Model {
    @Bind
    private double[] transmissionRate, recoveryRate, deathRate;
    @Bind
    private double[] susceptible, infected, recovered, deceased;
    private double temp;

    public DiseaseSpread() {
        super("DiseaseSpread", "This model simulates the spread " +
                "of infectious diseases in a population. " +
                "It accounts for factors such as transmission rates, " +
                "recovery rates, and death rates. " +
                "The model is useful for studying epidemic dynamics " +
                "and evaluating strategies for disease control.");
    }

    @Override
    public void run() {
        for (int t = 1; t < LL; t++) {
            double newInfections = transmissionRate[t] * susceptible[t - 1] * infected[t - 1];
            double newRecoveries = recoveryRate[t] * infected[t - 1];
            double newDeaths = deathRate[t] * infected[t - 1];

            susceptible[t] = susceptible[t - 1] - newInfections;
            infected[t] = infected[t - 1] + newInfections - newRecoveries - newDeaths;
            recovered[t] = recovered[t - 1] + newRecoveries;
            deceased[t] = deceased[t - 1] + newDeaths;
        }
    }
}
