package antifraud.dto;

import antifraud.model.Region;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record TransactionRequest(
        @Min(1)
        @NotNull
        Long amount,
        @NotEmpty
        @Pattern(regexp = "^((25[0-5]|(2[0-4]|1[0-9]|[1-9])\\d?)\\.){3}(25[0-5]|(2[0-4]|1[0-9]|[1-9])\\d?)$",
                message = "Invalid IP address"
        )
        String ip,
        @NotEmpty
        @Pattern(regexp = "^[\\d]{16}$", message = "Wrong card number format!")
        String number,
        @NotEmpty
        @NotNull
        String region,

        @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
        LocalDateTime date
) {

}
