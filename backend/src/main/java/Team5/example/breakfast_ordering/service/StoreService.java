package Team5.example.breakfast_ordering.service;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    // 建立新店家
    public Store createStore(Store store, String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("ownerId is required");
        }
        store.setOwnerId(ownerId);
        store.setUpdatedAt(Instant.now().toString());
        store.setActive(true);
        return storeRepository.save(store);
    }

    // 取得所有店家
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    // 解析 Authorization Header 取得 userId（支援 "Bearer uid:{userId}" 或 "Bearer {userId}"）
    public String parseUserIdFromAuthorization(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) return null;
        if (token.startsWith("uid:")) return token.substring(4);
        return token;
    }

    // 更新店家菜單
    public Store updateMenu(String storeId, List<Store.MenuItem> menu, String userId) {
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) throw new IllegalArgumentException("STORE_NOT_FOUND");
        if (userId == null || !userId.equals(store.getOwnerId())) throw new SecurityException("FORBIDDEN");
        if (menu != null) {  // null 時，不修改菜單
            for (Store.MenuItem mi : menu) {  // 為每個還不存在的菜單項目生成 ID
                if (mi.getId() == null || mi.getId().isBlank()) {
                    mi.setId(new ObjectId().toString());
                }
            }
        }
        store.setMenu(menu);
        store.setUpdatedAt(Instant.now().toString());
        return storeRepository.save(store);
    }
}
