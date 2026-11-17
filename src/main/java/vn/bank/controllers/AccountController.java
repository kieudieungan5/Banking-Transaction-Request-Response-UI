package vn.bank.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.bank.digitalbank.exceptions.BusinessException;
import vn.bank.digitalbank.exceptions.DatabaseConnectionException;
import vn.bank.digitalbank.exceptions.DatabaseOperationException;
import vn.bank.digitalbank.models.Account;

import vn.bank.digitalbank.services.AccountService;
import vn.bank.dts.AccountSearchResult;

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
// Accept requests from port 63342
@CrossOrigin(origins = "http://localhost:63342")
public class AccountController {

   // Mapping request about account that have method "GET" & endpoint "/api/accounts"
    @GetMapping("/api/accounts")
    public AccountSearchResult getAccounts(
            @RequestParam String customerId) {

        try {
            List<Account> accounts = AccountService.getAccountById(customerId);

            return new AccountSearchResult(true,
                    accounts.isEmpty()
                            ? "No accounts"
                            : "Get accounts success",
                    accounts);

        } catch (DatabaseOperationException |
                 BusinessException |
                 DatabaseConnectionException exc) {
            return new AccountSearchResult(false,
                    exc.getMessage(),
                    null);
        }

    }

}
