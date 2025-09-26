package ca.gbc.comp3095.productservice.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String id,
        String name,
        String description,
        BigDecimal price
) {}
