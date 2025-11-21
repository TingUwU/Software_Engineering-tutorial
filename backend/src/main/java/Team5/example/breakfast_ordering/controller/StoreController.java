package Team5.example.breakfast_ordering.controller;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "http://localhost:8080")
public class StoreController {

    @Autowired
    private StoreService storeService;

    // 創建新店家
    @PostMapping
    public ResponseEntity<Store> createStore(
            @RequestBody Store store,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String userId = storeService.parseUserIdFromAuthorization(authorization);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Store createdStore = storeService.createStore(store, userId);
        return ResponseEntity.ok(createdStore);
    }

    // 獲取所有店家
    @GetMapping
    public ResponseEntity<List<Store>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

}
