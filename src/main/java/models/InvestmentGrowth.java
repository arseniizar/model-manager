package models;

import annotations.Bind;

public class InvestmentGrowth extends Model {
    @Bind
    private double[] interestRate, reinvestmentRate; // financial growth parameters
    @Bind
    private double[] capital; // capital value over time
    private double temp; // auxiliary field, not bound to input/output

    public InvestmentGrowth() {
        super("InvestmentGrowths",
                "This model simulates the dynamics of " +
                        "economic investment growth over time. " +
                        "It includes variables such as capital investments, " +
                        "economic returns, and growth rates. " +
                        "It is useful for forecasting economic " +
                        "trends and assessing the impact of different investment strategies.");
    }

    @Override
    public void run() {
        for (int t = 1; t < LL; t++) {
            capital[t] = capital[t - 1] * (1 + interestRate[t] + reinvestmentRate[t]);
        }
    }
}
