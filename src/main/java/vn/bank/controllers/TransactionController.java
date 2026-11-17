package vn.bank.controllers;

import org.springframework.web.bind.annotation.*;
import vn.bank.digitalbank.exceptions.BusinessException;
import vn.bank.digitalbank.exceptions.DatabaseConnectionException;
import vn.bank.digitalbank.exceptions.DatabaseOperationException;
import vn.bank.digitalbank.models.Transaction;
import vn.bank.digitalbank.models.TransactionResult;
import vn.bank.digitalbank.services.TransactionService;
import vn.bank.dts.TransactionHistoryResult;
import vn.bank.dts.TransactionRequest;
import vn.bank.dts.TransactionResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Web server receives HTTP request.
 * Spring MVC extracts request data and dispatches the request to the appropriate Controller method.
 * Controller invokes the program's business logic and receives the result.
 * Spring MVC converts the returned Java object into transferable data & write to HTTP response body.
 * Web server sends HTTP response containing data to the UI.
 * This class solves HTTP requests sent to API
 */

@RestController
// Map all HTTP requests relating to transactions to this base path
@RequestMapping ("/api/transactions")
// Accept requests from port 63342
@CrossOrigin(origins = "http://localhost:63342")
public class TransactionController {

    // Mapping transfer request that have method "POST" & endpoint "/api/transactions/transfers"
    @PostMapping("/transfers")
    public TransactionResponse transfer(
        @RequestBody TransactionRequest request) {

        TransactionResult result = TransactionService.transfer(
                request.getCustomerId(),
                request.getAccountSource(),
                request.getAccountDestination(),
                request.getAmount()
        );

        return new TransactionResponse(result.getStatus(),
                result.getMessage(),
                result.getTransactionId());

    }

    // Mapping withdraw request that have method POST + endpoint "/api/transactions/withdrawals"
    @PostMapping("/withdrawals")
    public TransactionResponse withdraw(
            @RequestBody TransactionRequest request) {

        TransactionResult result = TransactionService.withdraw(
                request.getCustomerId(),
                request.getAccountSource(),
                request.getAmount()
        );

        return new TransactionResponse(result.getStatus(),
                result.getMessage(),
                result.getTransactionId());

    }

    // Mapping deposit request that have method POST + endpoint "/api/transactions/deposits"
    @PostMapping("/deposits")
    public TransactionResponse deposit(
            @RequestBody TransactionRequest request) {

        TransactionResult result = TransactionService.deposit(
                request.getCustomerId(),
                request.getAccountSource(),
                request.getAmount()
        );

        return new TransactionResponse(result.getStatus(),
                result.getMessage(),
                result.getTransactionId());

    }

    // Mapping getting transactions request that have method GET + endpoint "/api/transactions/histories"
    @GetMapping("/histories")
    public TransactionHistoryResult getTransactionHistory(
            @RequestParam String accountNumber) {

        try {
            List<Transaction> transactions = TransactionService.getTransactionHistory(accountNumber);

            return new TransactionHistoryResult(true,
                    transactions.isEmpty()
                            ? "No transaction"
                            : "Get transaction history success",
                    transactions);
        } catch (BusinessException |
                 DatabaseConnectionException |
                 DatabaseOperationException exc) {
            return new TransactionHistoryResult(false,
                    exc.getMessage(),
                    null);
        }

    }

}
