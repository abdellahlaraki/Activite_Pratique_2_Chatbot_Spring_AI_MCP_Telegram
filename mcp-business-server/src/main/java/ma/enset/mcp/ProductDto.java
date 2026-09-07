package ma.enset.mcp;

import java.math.BigDecimal;

public record ProductDto(Long id, String name, String category, BigDecimal price, int quantity) {
}
