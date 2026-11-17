const transferForm = document.getElementById("transferForm");
//=======================================================================================================

transferForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    // =================================================================
    const transferCustomerId =
        document.getElementById("transferCustomerId").value;
    const transferAccountSource =
        document.getElementById("transferAccountSource").value;
    const transferAccountDestination =
        document.getElementById("transferAccountDestination").value;
    const transferAmount =
        document.getElementById("transferAmount").value;

    // =================================================================
    const transferRequest = {
        customerId: transferCustomerId,
        accountSource: transferAccountSource,
        accountDestination: transferAccountDestination,
        amount: transferAmount
    };

    // =================================================================
    const jSonDataTransferRequest = JSON.stringify(transferRequest);
    const responseTransferRequest = await fetch("http://localhost:8080/api/transactions/transfers", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: jSonDataTransferRequest
    });
    const resultTransferRequest = await responseTransferRequest.json();
    // console.log("Kq backend: ", result);
    document.getElementById("transferResult").textContent = resultTransferRequest.message;

});

// =================================================================

const transferModal = document.getElementById("transferModal");

transferModal.addEventListener("hidden.bs.modal", function () {

    transferForm.reset();

    document.getElementById("transferResult").textContent = "";

});


