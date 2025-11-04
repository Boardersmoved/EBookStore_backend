package com.example.ebookstore_backend.client;

import com.example.ebookstore_backend.dto.PriceCalculationRequest;
import com.example.ebookstore_backend.dto.PriceCalculationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
    name = "price-calculator-service",
    fallback = PriceCalculatorFallback.class
)
public interface PriceCalculatorClient {
    
    @PostMapping("/api/price/calculate")
    PriceCalculationResponse calculatePrice(@RequestBody PriceCalculationRequest request);
}

