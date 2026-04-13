package com.BloggingApp.BloggingApp.repositories;

import com.BloggingApp.BloggingApp.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Transaction findByPaymentIntentId(String paymentIntentId);
}
