package ca.gbc.comp3095.productservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import ca.gbc.comp3095.productservice.repository.ProductRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductServiceApplicationTests {

	@Container
	@ServiceConnection(name = "mongodb")
	static MongoDBContainer mongo =
			new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
					.withStartupTimeout(Duration.ofSeconds(120));

	@Container
	@ServiceConnection(name = "redis")
	static GenericContainer<?> redis =
			new GenericContainer<>(DockerImageName.parse("redis:7.4.3"))
					.withExposedPorts(6379)
					.waitingFor(Wait.forListeningPort())
					.withStartupTimeout(Duration.ofSeconds(120));

	@LocalServerPort
	private Integer port;

	@Autowired private ProductRepository productRepository;
	@Autowired private RedisConnectionFactory redisConnectionFactory;
	@Autowired private CacheManager cacheManager;

	private Cache productCache() {
		Cache c = cacheManager.getCache("PRODUCT_CACHE");
		if (c == null) throw new IllegalStateException("PRODUCT_CACHE must be configured");
		return c;
	}

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;

		productRepository.deleteAll();

		try (var conn = redisConnectionFactory.getConnection()) {
			conn.serverCommands().flushDb();
		}

		productCache().clear();
	}

	@Test
	void createProductTest() {
		String requestBody = """
                {
                   "name": "Samsung TV",
                   "description": "Samsung TV - Model 2025",
                   "price": 2500
                }
                """;

		RestAssured.given()
				.contentType(ContentType.JSON)
				.body(requestBody)
				.when()
				.post("/api/product")
				.then()
				.log().all()
				.statusCode(HttpStatus.CREATED.value())
				.body("id", Matchers.notNullValue())
				.body("name", Matchers.equalTo("Samsung TV"))
				.body("description", Matchers.equalTo("Samsung TV - Model 2025"))
				.body("price", Matchers.equalTo(2500));
	}

	@Test
	void getAllProductsTest() {
		String id = createProductAndReturnId("Samsung TV", "Samsung TV - Model 2025", 2500);

		RestAssured.given()
				.contentType(ContentType.JSON)
				.when()
				.get("/api/product")
				.then()
				.log().all()
				.statusCode(HttpStatus.OK.value())
				.body("size()", Matchers.greaterThanOrEqualTo(1))
				.body("id", Matchers.hasItem(id))
				.body("find { it.id == '%s'}.name".formatted(id), Matchers.equalTo("Samsung TV"))
				.body("find { it.id == '%s'}.description".formatted(id), Matchers.equalTo("Samsung TV - Model 2025"))
				.body("find { it.id == '%s'}.price".formatted(id), Matchers.equalTo(2500));
	}

	private String createProductAndReturnId(String name, String description, int price) {
		String requestBody = """
                {
                   "name": "%s",
                   "description": "%s",
                   "price": %d
                }
                """.formatted(name, description, price);

		return RestAssured.given()
				.contentType(ContentType.JSON)
				.body(requestBody)
				.when()
				.post("/api/product")
				.then()
				.statusCode(HttpStatus.CREATED.value())
				.extract()
				.path("id");
	}

	@Test
	void updateProductTest() {
		String id = createProductAndReturnId("LG Monitor", "LG 27-inch 4K", 800);

		String updateBody = """
                {
                   "name": "LG Monitor",
                   "description": "LG 27-inch 4K",
                   "price": 1000
                }
                """;

		RestAssured.given()
				.contentType(ContentType.JSON)
				.body(updateBody)
				.when()
				.put("/api/product/{id}", id)
				.then()
				.statusCode(HttpStatus.NO_CONTENT.value())
				.header("Location", "/api/product/" + id);

		RestAssured.given()
				.when()
				.get("/api/product")
				.then()
				.log().all()
				.statusCode(HttpStatus.OK.value())
				.body("id", Matchers.hasItem(id))
				.body("find { it.id == '%s'}.name".formatted(id), Matchers.equalTo("LG Monitor"))
				.body("find { it.id == '%s'}.description".formatted(id), Matchers.equalTo("LG 27-inch 4K"))
				.body("find { it.id == '%s'}.price".formatted(id), Matchers.equalTo(1000));
	}

	@Test
	void deleteProductTest() {
		String id = createProductAndReturnId("Temp Item", "Disposable", 10);

		RestAssured.given()
				.when()
				.get("/api/product")
				.then()
				.statusCode(HttpStatus.OK.value())
				.body("id", Matchers.hasItem(id));

		RestAssured.given()
				.when()
				.delete("/api/product/{id}", id)
				.then()
				.log().all()
				.statusCode(HttpStatus.NO_CONTENT.value());

		productCache().clear();

		RestAssured.given()
				.when()
				.get("/api/product")
				.then()
				.statusCode(HttpStatus.OK.value())
				.body("id", Matchers.not(Matchers.hasItem(id)));
	}
}
