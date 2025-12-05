package ca.gbc.comp3095.apigateway.routes;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Slf4j
@Configuration
public class Routes {

    @Value("${services.product-url}")
    private String productServiceUrl;

    @Value("${services.order-url}")
    private String orderServiceUrl;

    @Value("${services.inventory-url}")
    private String inventoryServiceUrl;


    @Bean
    public RouterFunction<ServerResponse> productServiceRoute(){

        log.info("Initializing product service route with URL: {}", productServiceUrl);

        return route("product_service")
                .route(
                        RequestPredicates.path("/api/product/**"),
                        HandlerFunctions.http(productServiceUrl)
                )
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceCircuitBreaker",
                        URI.create("forward:/fallBackRoute")))
                .build();
    }


    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute(){

        log.info("Initializing order service route with URL: {}", orderServiceUrl);

        return route("order_service")
                .route(
                        RequestPredicates.path("/api/order"),
                        HandlerFunctions.http(orderServiceUrl)
                )
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceCircuitBreaker",
                        URI.create("forward:/fallBackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceRoute() {
        log.info("Initializing product service route with URL: {}", inventoryServiceUrl);

        return route("inventory_service")
                .route(
                        RequestPredicates.path("/api/inventory"),
                        HandlerFunctions.http(inventoryServiceUrl)
                )
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceCircuitBreaker",
                        URI.create("forward:/fallBackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productServiceSwaggerRoute(){
        return route("product_service_swagger")
                .route(RequestPredicates.path("/aggregate/product-service/v3/api-docs"),
                        HandlerFunctions.http(productServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("productServiceSwaggerRouteCircuitBreaker",
                        URI.create("forward:/fallBackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceSwaggerRoute(){
        return route("order_service_swagger")
                .route(RequestPredicates.path("/aggregate/order-service/v3/api-docs"),
                        HandlerFunctions.http(orderServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("orderServiceSwaggerRouteCircuitBreaker",
                        URI.create("forward:/fallBackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceSwaggerRoute(){
        return route("inventory_service_swagger")
                .route(RequestPredicates.path("/aggregate/inventory-service/v3/api-docs"),
                        HandlerFunctions.http(inventoryServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("inventoryServiceSwaggerRouteCircuitBreaker",
                        URI.create("forward:/fallBackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }


    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        log.info("Registering fallback route...");

        return route("fallBackRoute")
                .route(
                        RequestPredicates.path("/fallBackRoute"),
                        request -> {
                            log.warn("Fallback handler invoked for original request: {}",
                                    request.attribute("org.springframework.cloud.gateway.server.mvc.HandlerFunctions.originalRequestUrl"));

                            return ServerResponse
                                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                                    .body("Service Unavailable, please try again later");
                        }
                )
                .build();
    }
}
