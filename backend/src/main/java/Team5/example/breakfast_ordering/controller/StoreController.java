package Team5.example.breakfast_ordering.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.service.StoreService;

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

     // 回傳 ID 為 storeId 的店家
    @GetMapping("/{storeId}")
    public Store getStoreDetails(@PathVariable String storeId) {
        return storeService.getStoreById(storeId);
    }

    // 回傳在 ID 為 storeId 的店家中、名字包含 keyword 的商品
    @GetMapping("/{storeId}/search-product")
    public List<Store.MenuItem> searchProductInStore(
            @PathVariable String storeId,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return storeService.searchProductInStore(storeId, keyword);
    }

    // 更新店家分類
    @PatchMapping("/{id}/categories")
    public ResponseEntity<?> updateCategories(
            @PathVariable("id") String id,
            @RequestBody List<String> categories,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String userId = storeService.parseUserIdFromAuthorization(authorization);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            Store updated = storeService.updateCategories(id, categories, userId);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        } catch (IllegalArgumentException e) {
            if ("STORE_NOT_FOUND".equals(e.getMessage()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("STORE_NOT_FOUND");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 臨時修復端點：修復資料庫中格式不正確的店家資料
    @PostMapping("/fix-data")
    public ResponseEntity<String> fixStoreData() {
        try {
            int fixed = storeService.fixBusinessHoursData();
            return ResponseEntity.ok("已修復 " + fixed + " 個店家的資料");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("修復失敗: " + e.getMessage());
        }
    }
}