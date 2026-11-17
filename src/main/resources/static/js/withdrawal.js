const withdrawalForm = document.getElementById("withdrawalForm");
//=======================================================================================================

withdrawalForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const withdrawalCustomerId =
        document.getElementById("withdrawalCustomerId").value;
    const withdrawalAccountSource =
        document.getElementById("withdrawalAccountSource").value;
    const withdrawalAmount =
        document.getElementById("withdrawalAmount").value;

    const withdrawalRequest = {
        customerId: withdrawalCustomerId,
        accountSource: withdrawalAccountSource,
        amount: withdrawalAmount
    };

    const jSonDataWithdrawalRequest = JSON.stringify(withdrawalRequest);

    const responseWithdrawalRequest = await fetch("http://localhost:8080/api/transactions/withdrawals", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: jSonDataWithdrawalRequest
    });

    const resultWithdrawalRequest = await responseWithdrawalRequest.json();

    document.getElementById("withdrawalResult").textContent = resultWithdrawalRequest.message;

});

const withdrawalModal = document.getElementById("withdrawalModal");

withdrawalModal.addEventListener("hidden.bs.modal", function () {

    withdrawalForm.reset();

    document.getElementById("withdrawalResult").textContent = "";

});