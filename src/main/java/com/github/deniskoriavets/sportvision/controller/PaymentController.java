package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.request.PaymentRequest;
import com.github.deniskoriavets.sportvision.dto.response.PaymentResponse;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Stripe payment integration")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final SubscriptionService subscriptionService;

    @Value("${sportvision.stripe.webhook-secret}")
    private String webhookSecret;

    @Operation(summary = "Initiate Stripe checkout session for a subscription")
    @PreAuthorize("hasRole('PARENT')")
    @PostMapping("/checkout")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Checkout session created successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied - PARENT role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Subscription plan or child not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Child already has active subscription for this section",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "502", description = "Payment service error (Stripe)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PaymentResponse> initiateCheckout(
        @Valid @RequestBody PaymentRequest request) throws StripeException {
        return ResponseEntity.ok(subscriptionService.initiatePayment(request));
    }

    @Operation(summary = "Stripe Webhook (Internal use only)")
    @PostMapping("/webhook")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or signature",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> handleStripeWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Webhook security warning: Invalid signature detected. IP might be spoofed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature.");
        } catch (Exception e) {
            log.error("Webhook processing error: Invalid payload format.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload.");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getData().getObject();
            String stripeSessionId = session.getId();

            log.info("Stripe confirmed payment for session: {}", stripeSessionId);
            subscriptionService.completePayment(stripeSessionId);
        }

        return ResponseEntity.ok("Success");
    }
}