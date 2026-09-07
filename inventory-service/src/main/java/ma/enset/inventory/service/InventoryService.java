package ma.enset.inventory.service;

import ma.enset.inventory.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class InventoryService {
    private final List<Product> products = List.of(
            new Product(1L, "Ordinateur portable", "Informatique", new BigDecimal("8999.00"), 12),
            new Product(2L, "Écran 27 pouces", "Informatique", new BigDecimal("2399.00"), 4),
            new Product(3L, "Clavier mécanique", "Accessoires", new BigDecimal("749.00"), 2),
            new Product(4L, "Souris sans fil", "Accessoires", new BigDecimal("299.00"), 30)
    );

    public List<Product> findAll() {
        return products;
    }

    public Product findById(Long id) {
        return products.stream()
                .filter(product -> product.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Produit introuvable : " + id));
    }

    public List<Product> findLowStock(int threshold) {
        return products.stream().filter(product -> product.quantity() <= threshold).toList();
    }
}
