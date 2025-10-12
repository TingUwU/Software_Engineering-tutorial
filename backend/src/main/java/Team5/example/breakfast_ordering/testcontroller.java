package Team5.example.breakfast_ordering;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testcontroller {

    @GetMapping("/api/hello")
    public String sayHello() {
        return "Hello from Spring Boot Backend!";
    }
}