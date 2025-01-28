package com.vcib.ecommerce.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.vcib.ecommerce.client.StoreClient;
import com.vcib.ecommerce.entities.Product;

@Service
public class ProductService {
	
	
	@Autowired
	private StoreClient storeClient; 
	
	public Product getProductById(Long id, boolean ft) {
		if(ft) {
			return getProductWithRetry(id);
		}
		
		return getProductWithoutRetry(id);
	}
	
	
	private Product getProductWithRetry(Long id) {
		int maxAttempts = 5;
		int attempt = 0;
		while (attempt < maxAttempts) {
				try {
						return storeClient.product(id);
				} catch (Exception e) {
						attempt++;
						System.out.println("Attempt " + attempt + " failed: " + e.getMessage());
						if (attempt >= maxAttempts) {
								throw new RuntimeException("Max retry attempts reached", e);
						}
				}
		}
		throw new RuntimeException("Unexpected error");
	}
	
	private Product getProductWithoutRetry(Long id) {
		return storeClient.product(id);
	}
}
