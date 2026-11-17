// Lấy form nạp tiền
const depositForm = document.getElementById("depositForm");

// Bắt sự kiện "submit" (gửi yêu cầu)
depositForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    // Lấy dữ liệu nạp tiền
    const depositCustomerId =
        document.getElementById("depositCustomerId").value;
    const depositAccountSource =
        document.getElementById("depositAccountSource").value;
    const depositAmount =
        document.getElementById("depositAmount").value;

    // Tạo đối tượng chứa dữ liệu nạp tiền
    const depositRequest = {
        customerId: depositCustomerId,
        accountSource: depositAccountSource,
        amount: depositAmount
    };

    // Chuyển đối tượng thành định dạng Json
    const jSonDataDepositRequest = JSON.stringify(depositRequest);

    // Gửi HTTP Request cho Server
    // Đợi và nhận HTTP Response từ Server
    const responseDepositRequest = await fetch("http://localhost:8080/api/transactions/deposits", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: jSonDataDepositRequest
    });

    // Chuyển Response thành định dạng Json
    const resultDepositRequest = await responseDepositRequest.json();

    const resultElement = document.getElementById("depositResult");

    // Chuyển dữ liệu sang thẻ
    resultElement.innerHTML = "";

    resultElement.textContent = resultDepositRequest.message;

});

// =================================================================
// Lấy Modal nạp tiền
const depositModal = document.getElementById("depositModal");

// Bắt sự kiện khi Modal đã đóng
depositModal.addEventListener("hidden.bs.modal", function () {

    // Reset trường nhập liệu
    depositForm.reset();

    // Xóa thông báo kết quả cũ
    document.getElementById("depositResult").textContent = "";

});