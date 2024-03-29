package se.kth.iv1350.posSystem.view;

import se.kth.iv1350.posSystem.dto.ReceiptDTO;
import se.kth.iv1350.posSystem.integration.SystemHandlerObserver;
import se.kth.iv1350.posSystem.utilities.Amount;
import se.kth.iv1350.posSystem.utilities.FileLogger;
import se.kth.iv1350.posSystem.utilities.TimeAndDate;

/**
 * Class that shows the revenue generated by sales and outputs the current total revenue to a text file.
 */
public class TotalRevenueFileOutput implements SystemHandlerObserver {
    private final static String REVENUE_FILE_PATH = "Seminar4/textFiles/totalRevenue.txt";
    private final FileLogger fileLogger = new FileLogger(REVENUE_FILE_PATH);
    private Amount totalRevenue = new Amount(0);

    /**
     * Stores the time of update and the total revenue generated since program startup
     *
     * @param receiptDTO The transaction information, including revenue generated by the sale.
     */
    public void updateLogs(ReceiptDTO receiptDTO) {
        TimeAndDate timeAndDate = new TimeAndDate();
        this.totalRevenue = this.totalRevenue.plus(receiptDTO.getTotalPriceAfterDiscount());
        String logEntry = "Revenue at " + timeAndDate.getTimeAndDate() + ": " + this.totalRevenue;
        fileLogger.addEntryToLog(logEntry);
    }
}