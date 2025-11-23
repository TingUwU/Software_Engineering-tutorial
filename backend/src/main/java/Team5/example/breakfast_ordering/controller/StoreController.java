package Team5.example.breakfast_ordering.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.repository.StoreRepository;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "http://localhost:8080")
public class StoreController {
    @Autowired
    private StoreRepository storeRepository;

    // 回傳 ID 為 storeId 的店家
    @GetMapping("/{storeId}")
    public Store getStoreDetails(@PathVariable String storeId){
        Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new RuntimeException("找不到該店家 ID: " + storeId));
      
    // 回傳在 ID 為 storeId 的店家中、名字包含 keyword 的商品
    @GetMapping("/{storeId}/search-product")
    public List<Store.MenuItem> searchProductInStore(@PathVariable String storeId, @PathVariable String keyword){
        Store store = storeRepository.findById(storeId).
        orElseThrow( () -> new RuntimeException("找不到該店家 ID: " + storeId));

        if(store.getMenu() == null){
            return new ArrayList<>();
        }

        return store.getMenu().stream()
               .filter(item -> item.getItemName() != null && item.getItemName().contains(keyword))
               .collect(Collectors.toList());
    }

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

    // 更新店家菜單
    @PutMapping("/{id}/menu")
    public ResponseEntity<?> updateMenu(
            @PathVariable("id") String id,
            @RequestBody List<Store.MenuItem> menu,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String userId = storeService.parseUserIdFromAuthorization(authorization);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {                                       // 嘗試更新菜單
            Store updated = storeService.updateMenu(id, menu, userId);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {             // 沒有權限
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        } catch (IllegalArgumentException e) {      // 店家不存在或其他錯誤
            if ("STORE_NOT_FOUND".equals(e.getMessage()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("STORE_NOT_FOUND");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
        return store;
    }
}