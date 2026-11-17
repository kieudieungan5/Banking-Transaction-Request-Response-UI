const customerForm = document.getElementById("customerForm");

//=======================================================================================================
customerForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const data =
        document.getElementById("accountNumberToRequestCustomer").value;

    const customerRequest = {
        accountNumber: data
    }

    const url =
        `http://localhost:8080/api/customer?accountNumber=${encodeURIComponent(customerRequest.accountNumber)}`;

    const customerResponse = await fetch(url, {
        method: "GET"
    });

    const result = await customerResponse.json();

    const resultElement = document.getElementById("customerResult");

    resultElement.innerHTML = "";

    if (!result.success) {
        resultElement.textContent = result.message;
    } else {
        resultElement.textContent = result.customerId;
    }

});

//=======================================================================================================
const customerModal = document.getElementById("customerModal");

customerModal.addEventListener("hidden.bs.modal", function () {

    customerForm.reset();

    document.getElementById("customerResult").textContent = "";

});