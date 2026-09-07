package ma.enset.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class BusinessTools {
    private final RestClient customerClient;
    private final RestClient inventoryClient;

    public BusinessTools(RestClient.Builder builder,
                         @Value("${services.customer.url}") String customerUrl,
                         @Value("${services.inventory.url}") String inventoryUrl) {
        this.customerClient = builder.clone().baseUrl(customerUrl).build();
        this.inventoryClient = builder.clone().baseUrl(inventoryUrl).build();
    }

    @Tool(description = "Retourne la liste complète des clients enregistrés dans le microservice client")
    public List<CustomerDto> listCustomers() {
        return customerClient.get()
                .uri("/api/customers")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Tool(description = "Recherche un client métier par son identifiant numérique")
    public CustomerDto getCustomer(
            @ToolParam(description = "Identifiant numérique du client") Long customerId) {
        return customerClient.get()
                .uri("/api/customers/{id}", customerId)
                .retrieve()
                .body(CustomerDto.class);
    }

    @Tool(description = "Retourne tous les produits disponibles et leurs quantités en stock")
    public List<ProductDto> listProducts() {
        return inventoryClient.get()
                .uri("/api/products")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Tool(description = "Recherche les informations et la quantité en stock d'un produit à partir de son identifiant")
    public ProductDto getProductStock(
            @ToolParam(description = "Identifiant numérique du produit") Long productId) {
        return inventoryClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .body(ProductDto.class);
    }

    @Tool(description = "Retourne les produits dont la quantité est inférieure ou égale au seuil fourni")
    public List<ProductDto> listLowStockProducts(
            @ToolParam(description = "Seuil minimal de stock, entier positif") int threshold) {
        return inventoryClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/products/low-stock")
                        .queryParam("threshold", Math.max(0, threshold))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
