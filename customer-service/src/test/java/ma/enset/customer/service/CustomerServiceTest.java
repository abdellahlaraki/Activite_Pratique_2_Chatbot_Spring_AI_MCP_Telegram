package ma.enset.customer.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerServiceTest {
    private final CustomerService service = new CustomerService();

    @Test
    void shouldReturnKnownCustomer() {
        assertThat(service.findById(1L).name()).isEqualTo("Amine El Idrissi");
    }

    @Test
    void shouldRejectUnknownCustomer() {
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(RuntimeException.class);
    }
}
