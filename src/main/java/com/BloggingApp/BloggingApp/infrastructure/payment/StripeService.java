package com.BloggingApp.BloggingApp.infrastructure.payment;

import com.BloggingApp.BloggingApp.entities.Transaction;
import com.BloggingApp.BloggingApp.entities.User;
import com.BloggingApp.BloggingApp.repositories.TransactionRepository;
import com.BloggingApp.BloggingApp.repositories.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.model.checkout.Session;


import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostConstruct
    public void inti(){
        // Stripe ko apni secret key se initialize kro
        Stripe.apiKey = stripeSecretKey;
    }

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, String> createPaymentIntent(Long amount) throws StripeException {
        // 1. Logged-in User ki details token se
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Stripe Params
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount * 100) // ₹100 -> 10000 paise
                .setCurrency("inr")
                .setReceiptEmail(user.getEmail())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                // Isse CLI mein return_url ka error nahi aayega
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build()
                )
                // Metadata mein dynamic data daalo
                .putMetadata("userId", user.getId().toString())
                .putMetadata("userName", user.getName())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        // 3. Database Entry
        Transaction txn = new Transaction();
        txn.setPaymentIntentId(intent.getId());
        txn.setAmount(amount);
        txn.setStatus("PENDING");
        txn.setClientSecret(intent.getClientSecret());
        txn.setCustomerEmail(user.getEmail());
        txn.setUser(user);
        txn.setDate(new java.util.Date());

        transactionRepository.save(txn);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("intentId", intent.getId());
        return response;
    }

    public String createCheckoutSession(Long amount) throws StripeException {
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Transaction save karo taaki ID generate ho jaye (metadata ke liye)
        Transaction txn = new Transaction();
        txn.setAmount(amount);
        txn.setStatus("PENDING");
        txn.setCustomerEmail(user.getEmail());
        txn.setUser(user);
        txn.setDate(new java.util.Date());
        txn = transactionRepository.save(txn);

        // 2. Stripe Session create karo
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                // StripeService.java mein createCheckoutSession method ke andar
                .setSuccessUrl(baseUrl + "/api/v1/payments/success?session_id={CHECKOUT_SESSION_ID}")
                //.setSuccessUrl("http://localhost:8080/api/v1/payments/success?session_id={CHECKOUT_SESSION_ID}")
                //.setSuccessUrl("https://www.google.com") // Yahan apna success page dalna
                .setCancelUrl("https://www.google.com")
                // 🔥 Automatic Invoice Enable
                .setInvoiceCreation(SessionCreateParams.InvoiceCreation.builder()
                        .setEnabled(true)
                        .build())
                // 🔥 Customer Email zaroori hai invoice bhejne ke liye
                .setCustomerEmail(user.getEmail())
                .putMetadata("txnId", txn.getId().toString())
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("inr")
                                .setUnitAmount(amount * 100)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Blogging App Premium Subscription")
                                        .setDescription("Enjoy ad-free experience and premium features")
                                        .build())
                                .build())
                        .build())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
