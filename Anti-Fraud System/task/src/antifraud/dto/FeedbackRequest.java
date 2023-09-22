package antifraud.dto;

import antifraud.model.TransactionStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        @Min(1)
        long transactionId,
        @NotNull
        TransactionStatus feedback
) {
}
