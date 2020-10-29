package io.github.ibam.bca2ynab.processors;

import io.github.ibam.bca2ynab.models.BCATransaction;
import io.github.ibam.bca2ynab.models.TransactionFlow;
import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseBCAProcessor {

    protected static final Predicate<String> TRANSACTION_REGEX_MATCHER = Pattern.compile("\\d\\d/\\d\\d.+(DB|CR|KR|BUNGA).*").asMatchPredicate();
    protected static final Pattern AMOUNT_REGEX_PATTERN = Pattern.compile("\\d+([,\\.]\\d+)+");
    protected static final Pattern DATE_MONTH_REGEX_PATTERN_ALPHA = Pattern.compile("(\\d\\d/\\d\\d)");
    protected static final Pattern DATE_MONTH_REGEX_PATTERN_BETA = Pattern.compile("(\\d\\d\\d\\d)");

    protected boolean isTransactionBlockStarting(final String bcaLine) {
        return "TANGGAL KETERANGAN CBG MUTASI SALDO".equals(bcaLine);
    }

    protected String extractContext(final String[] bcaLines, final int currentLineIndex) {
        final StringBuilder contextBuilder = new StringBuilder();
        boolean skippingPageFooterHeader = false;

        for (int i = currentLineIndex + 1; i < bcaLines.length; i++) {
            final String bcaLine = bcaLines[i];
            if (isTransactionLine(bcaLine)) {
                break;
            }

            if (isEndOfPage(bcaLine)) {
                skippingPageFooterHeader = true;
                continue;
            }

            if (skippingPageFooterHeader) {
                skippingPageFooterHeader = !isTransactionBlockStarting(bcaLine);
                continue;
            }

            final String lineContext = bcaLine.strip().replaceAll("\\s+", " ");
            if (lineContext.length() > 1) {
                contextBuilder.append(" ").append(lineContext);
            }
        }
        return contextBuilder.toString().strip();
    }

    private boolean isEndOfPage(final String bcaLine) {
        return bcaLine.contains("Bersambung ke Halaman berikut");
    }

    protected BCATransaction constructTransaction(final String bcaLine, final String context) {
        final BCATransaction transaction = new BCATransaction();
        try {
            transaction.setDayOfMonth(extractDayOfMonth(bcaLine));
            transaction.setMonth(extractMonth(bcaLine));
            transaction.setFlowDirection(extractFlowDirection(bcaLine));
            transaction.setAmount(extractAmount(bcaLine));
            transaction.setContext(context);
            transaction.setOriginalLine(bcaLine);

            System.out.println(bcaLine);
            System.out.println(transaction.getFlow() + "\n");
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }
        return transaction;
    }

    private int extractDayOfMonth(final String bcaLine) {
        return Integer.parseInt(bcaLine.substring(0, 2));
    }

    private int extractMonth(final String bcaLine) {
        return Integer.parseInt(bcaLine.substring(3, 5));
    }

    private TransactionFlow extractFlowDirection(final String bcaLine) {
        if (bcaLine.indexOf(" DB ") > 0 || bcaLine.endsWith("DB")) {
            return TransactionFlow.OUTFLOW;
        } else if (bcaLine.indexOf(" CR ") > 0
                || bcaLine.indexOf(" KR ") > 0
                || bcaLine.indexOf(" BUNGA ") > 0) {
            return TransactionFlow.INFLOW;
        }
        System.err.println("Unknown transaction flow from statement " + bcaLine);
        return TransactionFlow.UNKNOWN;
    }

    private double extractAmount(final String bcaLine) {
        final Matcher amountMatcher = AMOUNT_REGEX_PATTERN.matcher(bcaLine);
        if (!amountMatcher.find()) {
            System.err.println("Cannot extract amount from line " + bcaLine);
        }

        final String amountString = amountMatcher.group(0).strip().replaceAll(",", "");
        return Double.parseDouble(amountString);
    }

    protected boolean isTransactionLine(final String bcaLine) {
        return TRANSACTION_REGEX_MATCHER.test(bcaLine);
    }

    protected BCATransaction constructTransaction(final CSVRecord csvRecord) {
        final BCATransaction transaction = new BCATransaction();
        try {
            transaction.setDayOfMonth(extractDayOfMonth(csvRecord));
            transaction.setMonth(extractMonth(csvRecord));
            transaction.setFlowDirection(extractFlowDirection(csvRecord));
            transaction.setAmount(extractAmount(csvRecord));
            transaction.setContext(csvRecord.get(1));
            transaction.setOriginalLine(extractOriginalLine(csvRecord));

            System.out.println(transaction.getOriginalLine());
            System.out.println(transaction.getFlow() + "\n");
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }
        return transaction;
    }

    private String extractOriginalLine(final CSVRecord csvRecord) {
        final StringBuilder originalLineBuilder = new StringBuilder();
        for (int i = 0; i < csvRecord.size(); i++) {
            originalLineBuilder.append(" ").append(csvRecord.get(i));
        }
        return originalLineBuilder.toString();
    }

    private int extractDayOfMonth(final CSVRecord csvRecord) {
        if (csvRecord.get(0).startsWith("'PEND")) {
            final String recordContext = csvRecord.get(1);

            Matcher matcher = DATE_MONTH_REGEX_PATTERN_ALPHA.matcher(recordContext);
            if (!matcher.find()) {
                matcher = DATE_MONTH_REGEX_PATTERN_BETA.matcher(recordContext);
                if (!matcher.find()) {
                    System.err.println("Cannot extract month from record context " + recordContext);
                    return -1;
                }
            }
            final String capturedDateMonth = matcher.group(0);
            return Integer.parseInt(capturedDateMonth.substring(0, 2));
        } else {
            return Integer.parseInt(csvRecord.get(0).substring(1, 3));
        }
    }

    private int extractMonth(final CSVRecord csvRecord) {
        if (csvRecord.get(0).startsWith("'PEND")) {
            final String recordContext = csvRecord.get(1);
            boolean isUsingBetaPattern = false;

            Matcher matcher = DATE_MONTH_REGEX_PATTERN_ALPHA.matcher(recordContext);
            if (!matcher.find()) {
                matcher = DATE_MONTH_REGEX_PATTERN_BETA.matcher(recordContext);
                isUsingBetaPattern = true;
                if (!matcher.find()) {
                    System.err.println("Cannot extract month from record context " + recordContext);
                    return -1;
                }
            }
            final String capturedDateMonth = matcher.group(0);
            final int startIdx = isUsingBetaPattern ? 2 : 3;
            return Integer.parseInt(capturedDateMonth.substring(startIdx, startIdx + 2));
        } else {
            return Integer.parseInt(csvRecord.get(0).substring(4, 6));
        }
    }

    private TransactionFlow extractFlowDirection(final CSVRecord csvRecord) {
        switch (csvRecord.get(4)) {
            case "DB" : return TransactionFlow.OUTFLOW;
            case "CR" : return TransactionFlow.INFLOW;
        }
        System.err.println("Unknown transaction flow from statement " + csvRecord.toString());
        return TransactionFlow.UNKNOWN;
    }

    private double extractAmount(final CSVRecord csvRecord) {
        return Double.parseDouble(csvRecord.get(3));
    }

    protected String extractCsvLines(final List<String> csvLines) {
        final StringBuilder csvLineBuilder = new StringBuilder();
        int lineIndex = 0;
        while (lineIndex < csvLines.size() && !csvLines.get(lineIndex).startsWith("Tanggal,Keterangan,Cabang,Jumlah,,Saldo")) {
            lineIndex++;
        }

        lineIndex++;

        while (lineIndex < csvLines.size() && !csvLines.get(lineIndex).startsWith("Saldo Awal,=,")) {
            csvLineBuilder.append(csvLines.get(lineIndex)).append("\n");
            lineIndex++;
        }

        return csvLineBuilder.toString();
    }
}
