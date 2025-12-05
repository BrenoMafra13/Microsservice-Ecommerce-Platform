package ca.gbc.comp3095.orderservice.client;


import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @GetExchange("/api/inventory")
    @CircuitBreaker(name="inventory", fallbackMethod = "fallbackMethod")
    @Retry(name="inventory")
    boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity);


    default boolean fallbackMethod(String skuCode, Integer quantity, Throwable throwable) {
        if(throwable instanceof CallNotPermittedException){
            log.warn("Circuit breaker is OPEN for SkuCode {}: {}", skuCode, throwable.getMessage());
        }else{
            log.warn("Fallback is triggered for SkuCode {}: {}", skuCode, throwable.getMessage());
        }
        return false;
    }

}
