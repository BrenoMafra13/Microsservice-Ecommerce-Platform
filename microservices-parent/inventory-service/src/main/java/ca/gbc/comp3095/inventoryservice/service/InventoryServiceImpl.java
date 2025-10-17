package ca.gbc.comp3095.inventoryservice.service;

import ca.gbc.comp3095.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository _inventoryRepository;

    @Override
    public boolean isInStock(String skuCode, Integer quantity) {
        return _inventoryRepository.existsBySkuCodeAndQuantityGreaterThanEqual(skuCode, quantity);
    }


}
