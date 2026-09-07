package ma.enset.customer.web;

import ma.enset.customer.model.Customer;
import ma.enset.customer.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> all() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> byId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(customerService.findById(id));
        } catch (RuntimeException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
