// Lấy form tra tài khoản
const accountForm = document.getElementById("accountForm");

// Bắt sự kiện "submit" (gửi yêu cầu)
accountForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    // Lấy dữ liệu yêu cầu tài khoản
    const requestCustomerId =
        document.getElementById("customerIdToRequestAccount").value;

    // Tạo đối tượng chứa dữ liệu yêu cầu tài khoản
    const accountRequest = {
        customerId: requestCustomerId
    }

    // Tạo URL và truyền ID khách  cần tra cứu
    const url =
        `http://localhost:8080/api/accounts?customerId=${encodeURIComponent(accountRequest.customerId)}`;

    // Gửi yêu cầu GET đến Server
    // Đợi và nhận phản hồi từ Server
    const accountResponse = await fetch(url, {
        method: "GET"
    });

    const result = await accountResponse.json();

    const resultElement = document.getElementById("accountsResult");

    // Chuyển dữ liệu sang thẻ
    resultElement.innerHTML = "";

    if (!result.success) {
        resultElement.textContent = result.message;
    } else if (result.accounts.length === 0) {
        resultElement.textContent = result.message;
    } else {
        let tableHTML = `
        <table class="table table-bordered table-striped mt-3">
            <thead>
                <tr>
                    <th>ID Customer</th>
                    <th>Number Account</th>
                    <th>Balance</th>
                </tr>
            </thead>
            <tbody>
        `;

        result.accounts.forEach(a => {
            tableHTML += `
                <tr>
                    <td>${a.customerId}</td>
                    <td>${a.accountNumber}</td>
                    <td>${a.balance}</td>
                </tr>
            `;
        });

        tableHTML += `
            </tbody>
        </table>
        `;

        resultElement.innerHTML += tableHTML;

    }

});

// Lấy Modal tra tài khoản
const accountModal = document.getElementById("accountModal");

// Bắt sự kiện khi Modal đã đóng
accountModal.addEventListener("hidden.bs.modal", function () {

    // Reset trường nhập liệu
    accountForm.reset();

    // Xóa thông báo kết quả cũ
    document.getElementById("accountsResult").textContent = "";

});