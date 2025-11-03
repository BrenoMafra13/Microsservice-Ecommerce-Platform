package ca.gbc.comp3095.inventoryservice;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryServiceApplicationTests {

	@ServiceConnection
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

	@LocalServerPort
	private Integer port;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;

		jdbcTemplate.execute("DELETE FROM t_inventory;");

		jdbcTemplate.execute("INSERT INTO t_inventory (sku_code, quantity) VALUES ('SKU001', 200);");
		jdbcTemplate.execute("INSERT INTO t_inventory (sku_code, quantity) VALUES ('SKU002', 50);");
	}

	static {
		postgreSQLContainer.start();
	}

	@Test
	void shouldReturnTrueWhenItemIsInStock() {
		given()
				.queryParam("skuCode", "SKU001")
				.queryParam("quantity", 100)
				.when()
				.get("/api/inventory")
				.then()
				.log().all()
				.statusCode(200)
				.body(is("true"));
	}

	@Test
	void shouldReturnFalseWhenItemDoesNotExist() {
		given()
				.queryParam("skuCode", "NON_EXISTENT_SKU")
				.queryParam("quantity", 100)
				.when()
				.get("/api/inventory")
				.then()
				.log().all()
				.statusCode(200)
				.body(is("false"));
	}
}
