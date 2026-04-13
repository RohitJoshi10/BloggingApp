package com.BloggingApp.BloggingApp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String paymentIntentId; // Stripe ki Transaction id
    private Long amount; // Amount in Paisa/Cents
    private String status; // PENDING, SUCCESS, FAILED
    private String clientSecret;    // Jo humne Stripe se liya
    private String customerEmail;

    @ManyToOne
    private User user; // Kisne donate kiya (Optional: can be null for anonymous)

    private java.util.Date date;
}
