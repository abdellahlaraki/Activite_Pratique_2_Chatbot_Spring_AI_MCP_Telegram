package ma.enset.customer.service;

import ma.enset.customer.model.Customer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CustomerService {
    private final List<Customer> customers = List.of(
            new Customer(1L, "Amine El Idrissi", "amine@example.ma", "Casablanca"),
            new Customer(2L, "Sara Alaoui", "sara@example.ma", "Rabat"),
            new Customer(3L, "Youssef Bennani", "youssef@example.ma", "Mohammedia")
    );

    public List<Customer> findAll() {
        return customers;
    }

    public Customer findById(Long id) {
        return customers.stream()
                .filter(customer -> customer.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Client introuvable : " + id));
    }
}
