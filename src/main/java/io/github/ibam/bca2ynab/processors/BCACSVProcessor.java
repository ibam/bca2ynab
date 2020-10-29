package io.github.ibam.bca2ynab.processors;

import io.github.ibam.bca2ynab.models.BCAStatement;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class BCACSVProcessor extends BaseBCAProcessor {
    public BCAStatement parse(final List<String> csvLines) throws IOException {
        final String cleanedCsvLines = extractCsvLines(csvLines);
        final Iterable<CSVRecord> csvRecords = CSVFormat.EXCEL.parse(new StringReader(cleanedCsvLines));
        final BCAStatement bcaStatement = new BCAStatement();

        for (final CSVRecord csvRecord : csvRecords) {
            bcaStatement.addTransaction(constructTransaction(csvRecord));
        }

        return bcaStatement;
    }
}
