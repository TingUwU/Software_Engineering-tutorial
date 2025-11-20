package Team5.example.breakfast_ordering;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testcontroller {

    @Autowired
    private StoreService storeService;

    @GetMapping("/api/hello")
    public String sayHello() {
        return "Hello from Spring Boot Backend!";
    }

    @PostMapping("/api/store")
    public String createStore(@RequestBody Store store) {
        storeService.createStore(store);
        return "儲存成功";
    }
}