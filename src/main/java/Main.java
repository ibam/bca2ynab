import io.github.ibam.bca2ynab.BCAProcessor;
import io.github.ibam.bca2ynab.CSVWriter;
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
            final BCAStatement bcaStatement = BCAProcessor.parse(csvLines);

            final List<BCATransaction> bcaTransactions = bcaStatement.getTransactions();

            for (final BCATransaction bcaTransaction : bcaTransactions) {
                System.out.println(bcaTransaction);
            }

            System.out.println("Total Inflow (" + bcaStatement.getInflowCount() + ") = " + String.format("%.2f", bcaStatement.getTotalInflow()));
            System.out.println("Total Outflow (" + bcaStatement.getOutflowCount() + ") = " + String.format("%.2f", bcaStatement.getTotalOutflow()));

            CSVWriter.write(bcaStatement, "files/output.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
