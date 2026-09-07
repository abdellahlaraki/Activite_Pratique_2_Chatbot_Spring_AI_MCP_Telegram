package ma.enset.inventory.model;

import java.math.BigDecimal;

public record Product(Long id, String name, String category, BigDecimal price, int quantity) {
}
