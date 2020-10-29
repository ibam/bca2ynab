import io.github.ibam.bca2ynab.processors.BCACSVProcessor;
import io.github.ibam.bca2ynab.writers.YnabCSVWriter;
import io.github.ibam.bca2ynab.models.BCAStatement;
import io.github.ibam.bca2ynab.models.BCATransaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            final List<String> csvLines = Files.readAllLines(Path.of("files/input.csv"));
            final BCACSVProcessor bcaProcessor = new BCACSVProcessor();
            final BCAStatement bcaStatement = bcaProcessor.parse(csvLines);

            final List<BCATransaction> bcaTransactions = bcaStatement.getTransactions();

            for (final BCATransaction bcaTransaction : bcaTransactions) {
                System.out.println(bcaTransaction);
            }

            System.out.println("Total Inflow (" + bcaStatement.getInflowCount() + ") = " + String.format("%.2f", bcaStatement.getTotalInflow()));
            System.out.println("Total Outflow (" + bcaStatement.getOutflowCount() + ") = " + String.format("%.2f", bcaStatement.getTotalOutflow()));

            YnabCSVWriter.write(bcaStatement, "files/output.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
