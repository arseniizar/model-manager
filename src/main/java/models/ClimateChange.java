package models;

import annotations.Bind;

public class ClimateChange extends Model {
    @Bind
    private double[] co2EmissionRate, absorptionRate, temperatureChange; // climate indicators
    @Bind
    private double[] co2Level, globalTemperature; // global CO2 level and temperature
    private double temp; // auxiliary field, not bound to input/output

    public ClimateChange() {
        super("ClimateChange", "This model simulates " +
                "the dynamics of global climate change " +
                "by considering various factors such as CO2 emissions, absorption rates, " +
                "temperature changes, and the buildup of greenhouse gases. " +
                "It helps analyze how human activities and environmental processes " +
                "impact climate over time.");
    }

    @Override
    public void run() {
        globalTemperature = new double[LL];
        for (int t = 1; t < LL; t++) {
            co2Level[t] = co2Level[t - 1] + (co2EmissionRate[t] - absorptionRate[t]);
            globalTemperature[t] = temperatureChange[t] * co2Level[t];
        }
    }
}
