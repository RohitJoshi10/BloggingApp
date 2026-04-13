package com.BloggingApp.BloggingApp.controllers;

import com.BloggingApp.BloggingApp.entities.Transaction;
import com.BloggingApp.BloggingApp.repositories.TransactionRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;



@RestController
@RequestMapping("/api/v1/payments")
public class WebHookController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("--- Webhook Hit Received ---");

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            System.out.println("Event Type: " + event.getType());

            // 1. Handle Checkout Session Completed (SUCCESS Flow)
            if ("checkout.session.completed".equals(event.getType())) {
                com.stripe.model.checkout.Session session = (com.stripe.model.checkout.Session) event.getData().getObject();

                String txnIdStr = session.getMetadata().get("txnId");
                if (txnIdStr != null) {
                    Integer txnId = Integer.parseInt(txnIdStr);
                    //Long txnId = Long.parseLong(txnIdStr); // Integer ki jagah Long use kiya
                    Transaction txn = transactionRepository.findById(txnId).orElse(null);

                    if (txn != null) {
                        txn.setStatus("SUCCESS");
                        txn.setPaymentIntentId(session.getPaymentIntent());
                        transactionRepository.save(txn);
                        System.out.println("✅ SUCCESS: DB Updated for ID: " + txnId);
                    }
                }
            }

            // 2. Handle Payment Intent Succeeded (Fallback)
            else if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent intent = (PaymentIntent) event.getData().getObject();
                String piId = intent.getId();

                Transaction txn = transactionRepository.findByPaymentIntentId(piId);
                if (txn != null && !"SUCCESS".equals(txn.getStatus())) {
                    txn.setStatus("SUCCESS");
                    transactionRepository.save(txn);
                    System.out.println("✅ SUCCESS: DB Updated via PaymentIntent ID: " + piId);
                }
            }

            // 3. 🔥 Handle Payment Failure (The Error Handling Part)
            else if ("payment_intent.payment_failed".equals(event.getType())) {
                PaymentIntent intent = (PaymentIntent) event.getData().getObject();

                // Metadata se txnId nikalne ki koshish (Checkout session se aata hai)
                String txnIdStr = intent.getMetadata().get("txnId");
                String errorMsg = intent.getLastPaymentError() != null ?
                        intent.getLastPaymentError().getMessage() : "Unknown Error";

                if (txnIdStr != null) {
                    Integer txnId = Integer.parseInt(txnIdStr);
                    Transaction txn = transactionRepository.findById(txnId).orElse(null);
                    if (txn != null) {
                        txn.setStatus("FAILED");
                        transactionRepository.save(txn);
                        System.err.println("❌ FAILED: Txn ID " + txnId + " due to: " + errorMsg);
                    }
                } else {
                    // Agar metadata nahi mila toh PaymentIntent ID se search karo
                    Transaction txn = transactionRepository.findByPaymentIntentId(intent.getId());
                    if (txn != null) {
                        txn.setStatus("FAILED");
                        transactionRepository.save(txn);
                        System.err.println("❌ FAILED: PI ID " + intent.getId() + " due to: " + errorMsg);
                    }
                }
            }

            return ResponseEntity.ok("Success");

        } catch (Exception e) {
            System.err.println("❌ Webhook Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook Error");
        }
    }
}


//
//@RestController
//@RequestMapping("/api/v1/payments")
//public class WebHookController {
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Value("${stripe.webhook.secret}")
//    private String endpointSecret;
//// WebHookController.java ka updated part
//
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
//                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
//
//        System.out.println("--- Webhook Hit Received ---");
//
//        try {
//            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
//            System.out.println("Event Type: " + event.getType());
//
//            // 1. Handle Checkout Session Completed (Best for Metadata)
//            if ("checkout.session.completed".equals(event.getType())) {
//                com.stripe.model.checkout.Session session = (com.stripe.model.checkout.Session) event.getData().getObject();
//
//                String txnIdStr = session.getMetadata().get("txnId");
//                System.out.println("Checkout Session Metadata txnId: " + txnIdStr);
//
//                if (txnIdStr != null) {
//                    Integer txnId = Integer.parseInt(txnIdStr);
//                    Transaction txn = transactionRepository.findById(txnId).orElse(null);
//
//                    if (txn != null) {
//                        txn.setStatus("SUCCESS");
//                        // Session se PaymentIntent ID nikal kar save karo
//                        txn.setPaymentIntentId(session.getPaymentIntent());
//                        transactionRepository.save(txn);
//                        System.out.println("✅ SUCCESS: DB Updated via Checkout Session for ID: " + txnId);
//                    }
//                }
//            }
//
//            // 2. Handle Payment Intent Succeeded (Fallback)
//            else if ("payment_intent.succeeded".equals(event.getType())) {
//                PaymentIntent intent = (PaymentIntent) event.getData().getObject();
//                String piId = intent.getId();
//
//                Transaction txn = transactionRepository.findByPaymentIntentId(piId);
//                if (txn != null && !"SUCCESS".equals(txn.getStatus())) {
//                    txn.setStatus("SUCCESS");
//                    transactionRepository.save(txn);
//                    System.out.println("✅ SUCCESS: DB Updated via PaymentIntent ID: " + piId);
//                }
//            }
//
//            return ResponseEntity.ok("Success");
//
//        } catch (Exception e) {
//            System.err.println("❌ Webhook Error: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook Error");
//        }
//    }
//}