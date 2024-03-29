package se.kth.iv1350.posSystem.integration;

import se.kth.iv1350.posSystem.dto.CustomerDTO;
import se.kth.iv1350.posSystem.dto.DiscountDTO;
import se.kth.iv1350.posSystem.dto.ItemDTO;
import se.kth.iv1350.posSystem.dto.ReceiptDTO;
import se.kth.iv1350.posSystem.utilities.Amount;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the handler of external systems, databases and the sale log
 */
public class SystemHandler {
    private final ExternalInventorySystem externalInventorySystem;
    private final ExternalAccountingSystem externalAccountingSystem;
    private final CustomerDatabase customerDatabase;
    private final DiscountDatabase discountDatabase;
    private final CashRegister cashRegister;
    private final ReceiptPrinter receiptPrinter;
    private final SaleLog saleLog;
    private final List<SystemHandlerObserver> systemHandlerObserversList = new ArrayList<>();

    /**
     * Creates an instance of <code>SystemHandler</code> and registers reference to external systems and logs
     */
    public SystemHandler() {
        this.externalInventorySystem = ExternalInventorySystem.getExternalInventorySystem();
        this.externalAccountingSystem = ExternalAccountingSystem.getExternalAccountingSystem();
        this.discountDatabase = DiscountDatabase.getDiscountDB();
        this.customerDatabase = CustomerDatabase.getCustomerDB();
        this.receiptPrinter = ReceiptPrinter.getReceiptPrinter();
        this.saleLog = SaleLog.getSaleLog();
        this.cashRegister = new CashRegister(new Amount(5000));
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
     * Retrieves registered customer information if the provided <code>customerID</code> is registered
     *
     * @param customerID The unique customer identification
     * @return The <code>customerDTO</code> containing registered customer information
     * @throws CustomerRegistrationException If the provided <code>customerID</code> is not registered
     */
    public CustomerDTO fetchCustomerDTO(String customerID) throws CustomerRegistrationException {
        return this.customerDatabase.getCustomerDTO(customerID);
    }

    /**
     * Retrieves all discounts offered
     *
     * @return The <code>DiscountDTO</code> containing all discounts
     */
    public DiscountDTO fetchDiscounts() {
        return this.discountDatabase.getDiscountDTO();
    }


    /**
     * Registers transaction data by setting the new amount of cash in <code>CashRegister</code>,
     * printing a receipt and updating logs in relevant systems.
     *
     * @param receiptDTO The DTO containing all data on a single sale
     */
    public void registerTransaction(ReceiptDTO receiptDTO) {
        this.cashRegister.setCashInRegister(receiptDTO);
        this.receiptPrinter.printReceipt(receiptDTO);
        updateLogs(receiptDTO);
    }

    private void updateLogs(ReceiptDTO receiptDTO) {
        this.externalInventorySystem.setItemInventory(receiptDTO);
        this.externalAccountingSystem.setPaymentRecords(receiptDTO);
        this.saleLog.setSaleInstance(receiptDTO);
        notifyObservers();
    }

    private void notifyObservers() {
        ReceiptDTO latestReceiptDTO = this.saleLog.getTransactionsList().getLast();
        for (SystemHandlerObserver systemHandlerObserver : systemHandlerObserversList) {
            systemHandlerObserver.updateLogs(latestReceiptDTO);
        }
    }

    /**
     * Adds all new observers to the observers list that should be notified
     *
     * @param systemHandlerObserversList The list of observers that should be added and notified.
     */
    public void addNewSystemHandlerObservers(List<SystemHandlerObserver> systemHandlerObserversList) {
        for (SystemHandlerObserver systemHandlerObserver : systemHandlerObserversList)
            addSystemHandlerObserver(systemHandlerObserver);
    }

    /**
     * Adds a new observer to the observers list that should be notified
     *
     * @param systemHandlerObserver The observer to add and to be notified
     */
    public void addSystemHandlerObserver(SystemHandlerObserver systemHandlerObserver) {
        if (!this.systemHandlerObserversList.contains(systemHandlerObserver)) {
            this.systemHandlerObserversList.add(systemHandlerObserver);
        }
    }
}
