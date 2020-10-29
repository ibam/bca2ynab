package io.github.ibam.bca2ynab.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BCAStatement {
    final List<BCATransaction> transactions = new ArrayList<>();
    double totalInflow = 0;
    int inflowCount = 0;

    double totalOutflow = 0;
    int outflowCount = 0;

    public List<BCATransaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(final BCATransaction bcaTransaction) {
        transactions.add(bcaTransaction);
        final double amount = bcaTransaction.getAmount();
        switch (bcaTransaction.getFlow()) {
            case INFLOW: {
                totalInflow += amount;
                inflowCount++;
                break;
            }
            case OUTFLOW: {
                totalOutflow += amount;
                outflowCount++;
                break;
            }
        }
    }

    public double getTotalInflow() {
        return totalInflow;
    }

    public double getTotalOutflow () {
        return totalOutflow;
    }

    public int getInflowCount() {
        return inflowCount;
    }

    public int getOutflowCount() {
        return outflowCount;
    }
}
