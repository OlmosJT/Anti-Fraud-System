package antifraud.controller;

import antifraud.dto.*;
import antifraud.model.Transaction;
import antifraud.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/antifraud", produces = "application/json")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/history")
    public ResponseEntity<?> getTransactionsHistory() {
        List<TransactionDTO> body = transactionService.getAllTransactions();
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<?> getTransactionsHistoryByCard(@PathVariable String number) {
        if(!transactionService.validateCardNumber(number)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credit-card number.");
        }
        List<TransactionDTO> body = transactionService.getAllTransactionsByCard(number);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @PreAuthorize(value = "hasAnyRole('MERCHANT')")
    @PostMapping("/transaction")
    public ResponseEntity<?> processTransaction(@Valid @RequestBody TransactionRequest request) {
        if(!transactionService.validateCardNumber(request.number())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong card number format!");
        }

        if(!transactionService.validateIP(request.ip())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong IP address format!");
        }

        Map<String, String> body = transactionService
                .processTransaction(
                        request.amount(),
                        request.ip(),
                        request.number(),
                        request.region(),
                        request.date()
                );

        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PreAuthorize(value = "hasAnyRole('SUPPORT')")
    @PutMapping("/transaction")
    public ResponseEntity<?> feedbackForTransaction(@Valid @RequestBody FeedbackRequest request) {
        TransactionDTO result = transactionService.processFeedBack(request.transactionId(), request.feedback());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PreAuthorize(value = "hasRole('SUPPORT')")
    @GetMapping("/suspicious-ip")
    public ResponseEntity<?> getSuspiciousIPs() {
        var body = transactionService.getBlackListedIPs();

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @PreAuthorize(value = "hasRole('SUPPORT')")
    @PostMapping("/suspicious-ip")
    public ResponseEntity<?> addSuspiciousIp(@Valid @RequestBody SusIPRequest request) {
        var body = transactionService.addIPToBlackList(request.ip());
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @PreAuthorize(value = "hasRole('SUPPORT')")
    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<?> removeSuspiciousIP(@PathVariable String ip) {
        if (!ip.matches("^((25[0-5]|(2[0-4]|1[0-9]|[1-9])\\d?)\\.){3}(25[0-5]|(2[0-4]|1[0-9]|[1-9])\\d?)$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong IP format");
        }
        transactionService.removeIPFromBlackList(ip);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "IP %s successfully removed!".formatted(ip)));
    }

    @PostMapping("/stolencard")
    public ResponseEntity<?> addStolenCard(@RequestBody CreditCardDTO creditCardDTO) {
        var cardEntity = transactionService.addToStolenCards(creditCardDTO.number());
        return ResponseEntity.status(HttpStatus.OK).body(cardEntity);
    }

    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<?> deleteStolenCard(@NotEmpty @PathVariable String number) {
        transactionService.deleteFromStolenCards(number);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "Card %s successfully removed!".formatted(number)));
    }

    @GetMapping("/stolencard")
    public ResponseEntity<?> getStolenCards() {
        var creditCards = transactionService.getStolenCards();
        return ResponseEntity.status(HttpStatus.OK).body(creditCards);
    }


}
