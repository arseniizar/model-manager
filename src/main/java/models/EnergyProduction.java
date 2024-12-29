package models;

import annotations.Bind;

public class EnergyProduction extends Model {
    @Bind
    private double[] growthSolar, growthWind, growthHydro;
    @Bind
    private double[] solarEnergy, windEnergy, hydroEnergy, totalEnergy;
    private double temp;

    public EnergyProduction() {
        super("EnergyProduction", "This model focuses on simulating " +
                "the production and consumption of energy resources. " +
                "It includes variables such as renewable energy growth, " +
                "fossil fuel consumption, and energy efficiency improvements. " +
                "The model can be used to analyze future energy scenarios " +
                "and their environmental impact.");
    }

    @Override
    public void run() {
        totalEnergy = new double[LL];
        for (int t = 1; t < LL; t++) {
            solarEnergy[t] = growthSolar[t] * solarEnergy[t - 1];
            windEnergy[t] = growthWind[t] * windEnergy[t - 1];
            hydroEnergy[t] = growthHydro[t] * hydroEnergy[t - 1];
            totalEnergy[t] = solarEnergy[t] + windEnergy[t] + hydroEnergy[t];
        }
    }
}
