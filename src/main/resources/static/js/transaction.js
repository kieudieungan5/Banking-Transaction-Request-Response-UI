const historyForm = document.getElementById("historyForm");

//=======================================================================================================

historyForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const requestAccount =
        document.getElementById("accountToRequestTransactionHistories").value;

    const transactionHistoriesRequest = {
        accountNumber: requestAccount
    };

    const url =
        `http://localhost:8080/api/transactions/histories?accountNumber=${encodeURIComponent(transactionHistoriesRequest.accountNumber)}`;

    const responseTransactionHistoriesRequest = await fetch(url, {
        method: "GET"
    });

    const result = await responseTransactionHistoriesRequest.json();

    const resultElement = document.getElementById("transactionHistoriesResult");

    resultElement.innerHTML = "";

    if (!result.success) {
        resultElement.textContent = result.message;
    } else if (result.transactions.length === 0) {
        resultElement.textContent = result.message;
    } else {
        let tableHTML = `
        <table class="table table-bordered table-striped mt-3">
            <thead>
                <tr>
                    <th>Transaction ID</th>
                    <th>Type</th>
                    <th>Account Source</th>
                    <th>Account Destination</th>
                    <th>Amount</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
        `;

        result.transactions.forEach(t => {
            tableHTML += `
                <tr>
                  <td>${t.id}</td>
                  <td>${t.type}</td>
                  <td>${t.accountSource}</td>
                  <td>${t.accountDestination}</td>
                  <td>${t.amount}</td>
                  <td>${t.status}</td>
                  <td>${t.time}</td>
                </tr>
            `;
        });

        tableHTML += `
            </tbody>
        </table>
        `;

        resultElement.innerHTML = tableHTML;
    }

});

//=======================================================================================================
const historyModal = document.getElementById("historyModal");

historyModal.addEventListener("hidden.bs.modal", function () {

    historyForm.reset();

    document.getElementById("accountResponseResult").textContent = "";

});