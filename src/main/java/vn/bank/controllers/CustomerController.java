package vn.bank.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.bank.digitalbank.exceptions.BusinessException;
import vn.bank.digitalbank.exceptions.DatabaseConnectionException;
import vn.bank.digitalbank.exceptions.DatabaseOperationException;
import vn.bank.digitalbank.models.Customer;
import vn.bank.digitalbank.services.CustomerService;
import vn.bank.dts.CustomerSearchResult;

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
public class CustomerController {

    // Mapping request about customer that have method "GET" & endpoint "/api/customer"
    @GetMapping("/api/customer")
    public CustomerSearchResult getCustomer(
            @RequestParam String accountNumber) {
        try {
            Customer customer = CustomerService.getCustomerByAccount(accountNumber);

            return new CustomerSearchResult(customer != null,
                    customer == null
                            ? "No customer"
                            : "Get customer success",
                    customer == null
                            ? null
                            : customer.getCustomerId());

        } catch (DatabaseOperationException |
                BusinessException |
                DatabaseConnectionException exc) {

            return new CustomerSearchResult(false,
                    exc.getMessage(),
                    null);

        }
    }

}
