package vn.bank.dts;

import vn.bank.digitalbank.commons.TransactionStatus;

public class TransactionResponse {
    private final TransactionStatus status;
    private final String message;
    private final String tranId;

    public TransactionResponse(TransactionStatus status,
                               String message,
                               String tranId) {
        this.status = status;
        this.message = message;
        this.tranId = tranId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getTranId() {
        return tranId;
    }
}
