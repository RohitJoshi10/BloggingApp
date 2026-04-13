package com.BloggingApp.BloggingApp.controllers;

import com.BloggingApp.BloggingApp.entities.Transaction;
import com.BloggingApp.BloggingApp.infrastructure.payment.StripeService;
import com.BloggingApp.BloggingApp.repositories.TransactionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createIntent(@RequestParam Long amount){
        try{
            Map<String, String> result = stripeService.createPaymentIntent(amount);
            return ResponseEntity.ok(result);
        }catch (StripeException e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestParam Long amount) {
        try {
            String checkoutUrl = stripeService.createCheckoutSession(amount);
            // Hum link bhej rahe hain jise copy karke browser mein kholna hai
            return ResponseEntity.ok(Map.of("checkoutUrl", checkoutUrl));
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/success")
    public void handlePaymentSuccess(@RequestParam("session_id") String sessionId, HttpServletResponse response) {
        try {
            // 1. Stripe se session retrieve karo
            Session session = Session.retrieve(sessionId);

            // 2. Session se invoice ID nikal kar Invoice fetch karo
            String invoiceId = session.getInvoice();
            if (invoiceId != null) {
                Invoice invoice = Invoice.retrieve(invoiceId);

                // 3. hosted_invoice_url par redirect kar do (Wahi page jo tune link mein bheja tha)
                String receiptUrl = invoice.getHostedInvoiceUrl();
                response.sendRedirect(receiptUrl);
            } else {
                // Agar invoice turant generate nahi hui toh default success page pe bhej do
                response.sendRedirect("https://www.google.com");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/confirm/{intentId}")
    public ResponseEntity<?> confirmPayment(@PathVariable String intentId) {
        try {
            // 1. Stripe se fresh status fetch karo
            PaymentIntent intent = PaymentIntent.retrieve(intentId);
            System.out.println("Stripe Status: " + intent.getStatus());

            // 2. DB se transaction nikalo
            Transaction txn = transactionRepository.findByPaymentIntentId(intentId);

            if (txn == null) {
                return ResponseEntity.status(404).body("Transaction not found in our DB for ID: " + intentId);
            }

            // 3. Agar Stripe kehta hai success, toh DB update karo
            if ("succeeded".equals(intent.getStatus())) {
                txn.setStatus("SUCCESS");
                transactionRepository.save(txn);
                return ResponseEntity.ok(Map.of("message", "Payment Successful! 🎉", "status", "SUCCESS"));
            }

            return ResponseEntity.ok(Map.of("message", "Stripe status is: " + intent.getStatus(), "status", txn.getStatus()));

        } catch (StripeException e) {
            return ResponseEntity.badRequest().body("Stripe Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server Error: " + e.getMessage());
        }
    }
}
