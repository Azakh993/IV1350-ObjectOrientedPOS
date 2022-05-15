package se.kth.iv1350.posSystem.integration;

import se.kth.iv1350.posSystem.model.ItemDTO;
import se.kth.iv1350.posSystem.model.SaleDTO;
import se.kth.iv1350.posSystem.utilities.Amount;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the handler of external systems, databases and the sale log
 */
public class SystemHandler {
    private static final SystemHandler SYSTEM_HANDLER = new SystemHandler();
    private final ExternalInventorySystem externalInventorySystem;
    private final ExternalAccountingSystem externalAccountingSystem;
    private final CashRegister cashRegister;
    private final ReceiptPrinter receiptPrinter;
    private final SaleLog saleLog;
    private final List<SaleLogObserver> saleLogObserversList = new ArrayList<>();



    private SystemHandler() {
        this.externalInventorySystem = ExternalInventorySystem.getExternalInventorySystem();
        this.externalAccountingSystem = ExternalAccountingSystem.getExternalAccountingSystem();
        this.receiptPrinter = ReceiptPrinter.getReceiptPrinter();
        this.saleLog = SaleLog.getSaleLog();
        this.cashRegister = new CashRegister(new Amount(5000));
    }

    public static SystemHandler getSystemHandler () {
        return SYSTEM_HANDLER;
    }

    /**
     * Retrieves an item from the external inventory system, based on provided item identifier
     *
     * @param itemID The unique item identifier of an item
     * @return The item instance representing an item with the provided item identifier
     * @throws ItemIdentifierException if the registered item identifier is invalid
     * @throws ExternalSystemException if the external system cannot be reached
     */
    public ItemDTO fetchItem(String itemID) throws ItemIdentifierException, ExternalSystemException {
        return this.externalInventorySystem.getItem(itemID);
    }

    /**
     * Adds cash to the cash register
     *
     * @param saleDTO The object instance specifying amount paid and amount change to give back
     */
    public void addCashToRegister(SaleDTO saleDTO) {
        this.cashRegister.setCashInRegister(saleDTO);
    }

    /**
     * Forwards receipt data to receipt printer for printing
     *
     * @param saleDTO The receipt information
     */
    public void printReceipt(SaleDTO saleDTO) {
        this.receiptPrinter.printReceipt(saleDTO);
    }

    /**
     * Updates sale log, the inventory of items and the payment records
     *
     * @param saleDTO The sale data, containing all information on the transaction
     */
    public void updateLogs(SaleDTO saleDTO) {
        this.externalInventorySystem.setItemInventory(saleDTO);
        this.externalAccountingSystem.setPaymentRecords(saleDTO);
        this.saleLog.setSaleInstance(saleDTO);
        notifyObservers();
    }

    /**
     * The observers to notify when transaction has been finalized.
     *
     * @param saleLogObserversList The list of observers that should be notified.
     */
    public void addNewSaleLogObservers(List<SaleLogObserver> saleLogObserversList) {
        for(SaleLogObserver saleLogObserver : saleLogObserversList)
            addNewSaleLogObserver(saleLogObserver);
    }

    /**
     * The observer to notify when transaction has been finalized.
     *
     * @param saleLogObserver The observer that should be notified.
     */
    public void addNewSaleLogObserver(SaleLogObserver saleLogObserver) {
        if (!this.saleLogObserversList.contains(saleLogObserver)) {
            this.saleLogObserversList.add(saleLogObserver);
        }
    }

    private void notifyObservers() {
        SaleDTO latestSaleDTO = this.saleLog.getTransactionsList().getLast();
        for (SaleLogObserver saleLogObserver : saleLogObserversList) {
            saleLogObserver.updateLogs(latestSaleDTO);
        }
    }
}
