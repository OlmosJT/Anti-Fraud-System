package antifraud.dto;

import antifraud.model.Transaction;
import antifraud.model.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

public record TransactionDTO(
        long transactionId,
        long amount,
        String ip,
        String number,
        @JsonProperty("region")
        String regionCode,
        LocalDateTime date,
        TransactionStatus result,
        String feedback

) {

}
