package ca.gbc.comp3095.orderservice;

import ca.gbc.comp3095.orderservice.stubs.InventoryClientStub;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import ca.gbc.comp3095.orderservice.event.OrderPlacedEvent;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @LocalServerPort
    private Integer port;

    @Autowired
    private WireMockServer wireMockServer;

    @MockBean
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        when(kafkaTemplate.send(any(), any(OrderPlacedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    static {
        postgreSQLContainer.start();
    }

    @Test
    void placeOrderTest() {
        String orderJson = """
                {
                  "skuCode": "samsung_tv_2025",
                  "price": 5000,
                  "quantity": 10,
                  "userDetails": {
                    "email": "user@example.com",
                    "firstName": "Test",
                    "lastName": "User"
                  }
                }
                """;

        InventoryClientStub.stubInventoryCall("samsung_tv_2025", 10);

        var responseBodyString = RestAssured
                .given()
                .contentType("application/json")
                .body(orderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .body().asString();

        assertThat(responseBodyString, Matchers.is("Successfully Placed Order"));
    }
}
