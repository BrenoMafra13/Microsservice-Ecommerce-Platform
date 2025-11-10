package ca.gbc.comp3095.productservice.service;

import ca.gbc.comp3095.productservice.dto.ProductRequest;
import ca.gbc.comp3095.productservice.dto.ProductResponse;
import ca.gbc.comp3095.productservice.model.Product;
import ca.gbc.comp3095.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * 1. @Cacheable -> "Read from cache of exists, otherwise run method & cache result"
 *    - value = "PRODUCT_CACHE"
 *    - key = ""
 *
 * 2. @CachePut -> "Always run the method, then PUT the result in cache"
 *    - Used to create/update so cache stays fresh
 *    - key = "#result.id()"
 *
 * 3. @CacheEvict -> "Remove entry from cache"
 *    - key: "#productId"
 *
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository _productRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @CachePut(value = "PRODUCT_CACHE", key="#result.id()")
    public ProductResponse createProduct(ProductRequest productRequest) {

        log.debug("Create new product {}", productRequest);

        Product product = Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .price(productRequest.price())
                .build();

        //save product to database
        _productRepository.save(product);
        log.debug("Successfully saved new product {}", product);

        /**
         *
         * Cache productCache = cacheManager.getCache("PRODUCT_CACHE");
         * productCache.put(product.getId(), Product)
         *
         */

        return new ProductResponse(product.getId(), product.getName(),
                product.getDescription(), product.getPrice());
    }

    @Override
    @Cacheable(value = "PRODUCT_CACHE", key = "'ALL_PRODUCTS'")
    public List<ProductResponse> getAllProducts() {

        log.debug("Returning a list of Products");
        List<Product> products = _productRepository.findAll();

        return products
                .stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(),
                product.getDescription(), product.getPrice());
    }

    @Override
    @CachePut(value = "PRODUCT_CACHE", key="#result")
    public String updateProduct(String productId, ProductRequest productRequest) {

        log.debug("Updating product with id {}",  productId);

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(productId));
        Product product = mongoTemplate.findOne(query, Product.class);

        if(product != null){
            product.setName(productRequest.name());
            product.setDescription(productRequest.description());
            product.setPrice(productRequest.price());
            return  _productRepository.save(product).getId();
        }
        return productId;
    }

    @Override
    @CacheEvict(value = "PRODUCT_CACHE", key="#productId")
    public void deleteProduct(String productId) {
        log.debug("Deleting product with id {}",  productId);
        _productRepository.deleteById(productId);
    }
}
