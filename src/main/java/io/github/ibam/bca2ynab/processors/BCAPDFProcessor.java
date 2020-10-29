package io.github.ibam.bca2ynab.processors;

import io.github.ibam.bca2ynab.models.BCAStatement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

public class BCAPDFProcessor extends BaseBCAProcessor {
    public BCAStatement parse(final PDDocument bcaFile) throws IOException {
        final String[] bcaLines = new PDFTextStripper().getText(bcaFile).split("\r\n");
        final BCAStatement bcaStatement = new BCAStatement();
        boolean isInTransactionBlock = false;

        for (int i = 0; i < bcaLines.length; i++) {
            final String bcaLine = bcaLines[i];
            if (!isInTransactionBlock) {
                isInTransactionBlock = isTransactionBlockStarting(bcaLine);
                continue;
            }

            if (!isTransactionLine(bcaLine)) {
                continue;
            }

            bcaStatement.addTransaction(constructTransaction(bcaLine, extractContext(bcaLines, i)));
        }

        return bcaStatement;
    }
}
