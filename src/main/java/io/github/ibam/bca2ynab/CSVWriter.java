package io.github.ibam.bca2ynab;

import io.github.ibam.bca2ynab.models.BCAStatement;
import io.github.ibam.bca2ynab.models.BCATransaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.NumberFormat;

public class CSVWriter {
    public static void write(final BCAStatement bcaStatement, final String filename) throws IOException {

        final CSVPrinter csvPrinter = CSVFormat.DEFAULT.withHeader("Date", "Payee", "Memo", "Amount").print(new File(filename), Charset.defaultCharset());

        for (final BCATransaction transaction : bcaStatement.getTransactions()) {
            final String dateString = transaction.getMonth() + "/" + transaction.getDayOfMonth() + "/20";
            final String signedAmount = NumberFormat.getNumberInstance().format(transaction.getSignedAmount());
            csvPrinter.printRecord(dateString, transaction.getContext(), transaction.getOriginalLine(), signedAmount.replace(",", ""));
        }

        csvPrinter.close(true);
    }
}
