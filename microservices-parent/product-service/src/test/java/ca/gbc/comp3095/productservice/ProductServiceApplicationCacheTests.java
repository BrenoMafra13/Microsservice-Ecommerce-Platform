package ca.gbc.comp3095.productservice;

import ca.gbc.comp3095.productservice.dto.ProductRequest;
import ca.gbc.comp3095.productservice.dto.ProductResponse;
import ca.gbc.comp3095.productservice.model.Product;
import ca.gbc.comp3095.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProductServiceApplicationCacheTests {

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.4.3"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofSeconds(120));

    @Container
    @ServiceConnection(name = "mongodb")
    static MongoDBContainer mongodbContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0"))
            .withStartupTimeout(Duration.ofSeconds(120));

    @Autowired private MockMvc mockMvc;
    @Autowired private CacheManager cacheManager;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private ProductRepository productRepository;
    @MockitoSpyBean private ProductRepository productRepositorySpy;

    @Autowired private RedisConnectionFactory redisConnectionFactory;

    private Cache productCache() {
        Cache c = cacheManager.getCache("PRODUCT_CACHE");
        assertNotNull(c, "PRODUCT_CACHE must exist and be configured correctly");
        return c;
    }

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        try (var conn = redisConnectionFactory.getConnection()) {
            conn.serverCommands().flushDb();
        }

        Cache cache = cacheManager.getCache("PRODUCT_CACHE");
        if (cache != null) cache.clear();

        clearInvocations(productRepositorySpy);
    }

    @Test
    void createProduct_cachesProductResponseUnderId() throws Exception {
        var req = new ProductRequest(null, "Samsung TV", "2025 Model", BigDecimal.valueOf(2000));

        MvcResult result = mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var body = objectMapper.readValue(result.getResponse().getContentAsString(), ProductResponse.class);
        assertNotNull(body.id(), "Response must include generated product ID");

        ProductResponse cached = productCache().get(body.id(), ProductResponse.class);
        assertNotNull(cached, "ProductResponse should be cached under product ID");
        assertEquals("Samsung TV", cached.name());
        assertEquals(BigDecimal.valueOf(2000), cached.price());
    }

    @Test
    void getAllProducts_isCached_afterFirstCall_andSkipsRepository_onSecondCall() throws Exception {
        productRepository.save(Product.builder()
                .name("Text Book")
                .description("COMP3095")
                .price(BigDecimal.valueOf(100))
                .build());

        mockMvc.perform(get("/api/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Text Book"));

        verify(productRepositorySpy, times(1)).findAll();
        clearInvocations(productRepositorySpy);

        mockMvc.perform(get("/api/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Text Book"));

        verify(productRepositorySpy, times(0)).findAll();
    }

    @Test
    void updateProduct_cachesIdStringUnderIdKey() throws Exception {
        // Seed initial product
        var p = productRepository.save(Product.builder()
                .name("Laptop").description("Base").price(BigDecimal.valueOf(2000)).build());

        var update = new ProductRequest(p.getId(), "Gaming Laptop", "Pro", BigDecimal.valueOf(3000));

        mockMvc.perform(put("/api/product/{id}", p.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNoContent());

        String cachedId = productCache().get(p.getId(), String.class);
        assertNotNull(cachedId, "Updated product ID should be cached as String");
        assertEquals(p.getId(), cachedId);
    }

    @Test
    void deleteProduct_evictsSingleItemCacheEntry() throws Exception {
        var p = productRepository.save(Product.builder()
                .name("Phone").description("Base").price(BigDecimal.valueOf(500)).build());

        productCache().put(p.getId(), new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice()));
        assertNotNull(productCache().get(p.getId()), "Precondition: cache should contain product before deletion");

        mockMvc.perform(delete("/api/product/{id}", p.getId()))
                .andExpect(status().isNoContent());

        assertNull(productCache().get(p.getId()), "Cache entry must be evicted after deletion");
    }
}
