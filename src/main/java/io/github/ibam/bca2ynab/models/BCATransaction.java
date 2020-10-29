package io.github.ibam.bca2ynab.models;

public class BCATransaction {

    private int dayOfMonth = 0;
    private int month = 0;
    private TransactionFlow flow = TransactionFlow.UNKNOWN;
    private double amount = 0;
    private String context = "";

    public String getOriginalLine() {
        return originalLine;
    }

    public void setOriginalLine(String originalLine) {
        this.originalLine = originalLine;
    }

    private String originalLine = "";

    public void setDayOfMonth(final int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public void setMonth(final int mount) {
        this.month = mount;
    }

    public void setFlowDirection(final TransactionFlow flow) {
        this.flow = flow;
    }

    public void setAmount(final double amount) {
        this.amount = amount;
    }


    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getMonth() {
        return month;
    }

    public TransactionFlow getFlow() {
        return flow;
    }

    public double getAmount() {
        return amount;
    }

    public void setContext(final String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "BCATransaction{" +
                "dayOfMonth=" + dayOfMonth +
                ", month=" + month +
                ", flow=" + flow +
                ", amount=" + amount +
                ", context='" + context + '\'' +
                ", originalLine='" + originalLine + '\'' +
                '}';
    }

    public double getSignedAmount() {
        return (this.flow == TransactionFlow.OUTFLOW ? -1 : 1) * amount;
    }
}
