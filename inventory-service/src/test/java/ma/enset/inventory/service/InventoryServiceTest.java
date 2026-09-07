package ma.enset.inventory.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryServiceTest {
    private final InventoryService service = new InventoryService();

    @Test
    void shouldFindLowStockProducts() {
        assertThat(service.findLowStock(5))
                .extracting("name")
                .containsExactly("Écran 27 pouces", "Clavier mécanique");
    }
}
