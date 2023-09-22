package antifraud.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Value;

public record SusIPRequest(
        @NotNull
        @NotEmpty
        @Pattern(regexp = "^((25[0-5]|(2[0-4]|1[0-9]|[1-9])\\d?)\\.){3}(25[0-5]|(2[0-4]|1[0-9]|[1-9])\\d?)$",
                message = "Invalid IP address"
        )
        String ip
) {
}
