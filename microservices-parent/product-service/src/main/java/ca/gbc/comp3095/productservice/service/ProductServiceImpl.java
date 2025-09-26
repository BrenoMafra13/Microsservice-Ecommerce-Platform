package ca.gbc.comp3095.productservice.service;

import ca.gbc.comp3095.productservice.dto.ProductRequest;
import ca.gbc.comp3095.productservice.dto.ProductResponse;
import ca.gbc.comp3095.productservice.model.Product;
import ca.gbc.comp3095.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository _productRepository;
    private final MongoTemplate _mongoTemplate;

    @Override
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.debug("Creating new product {}", productRequest);

        Product product = Product.builder()
                .name(productRequest.name())
                .price(productRequest.price())
                .description(productRequest.description())
                .build();

        _productRepository.save(product);
        log.debug("Saved product {}", product);

        return new ProductResponse(product.getId(), product.getName(),
                product.getDescription(), product.getPrice());
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        log.debug("Retrieving all products");

        List<Product> products = _productRepository.findAll();

        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    private  ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(),
                product.getDescription(), product.getPrice());
    }

    @Override
    public String updateProduct(String productId, ProductRequest productRequest) {
        log.debug("Updating product with Id {}", productId);
        Query query = Query.query(Criteria.where("id").is(productId));
        Product updateProduct = _mongoTemplate.findOne(query, Product.class);

        if(updateProduct != null){
            updateProduct.setName(productRequest.name());
            updateProduct.setDescription(productRequest.description());
            updateProduct.setPrice(productRequest.price());
            _productRepository.save(updateProduct).getId();
        }
        return productId;
    }

    @Override
    public void deleteProduct(String productId) {
        log.debug("Deleting product with id {}", productId);
        _productRepository.deleteById(productId);
    }
}
