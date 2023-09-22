package antifraud.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record CreditCardDTO(@JsonIgnore Long id, String number) {
}
