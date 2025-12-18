package Team5.example.breakfast_ordering.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.repository.StoreRepository;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    // 建立新店家（以 ownerId）
    public Store createStore(Store store, String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("ownerId is required");
        }
        // 防止客戶端偽造 ownerId：若 store 已帶 ownerId 且與登入身分不一致則拒絕
        if (store.getOwnerId() != null && !store.getOwnerId().isBlank() && !ownerId.equals(store.getOwnerId())) {
            throw new SecurityException("FORBIDDEN");
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
        if (menu != null) {
            for (Store.MenuItem mi : menu) {
                if (mi.getId() == null) {
                    mi.setId(new ObjectId());
                }
            }
        }
        store.setMenu(menu);
        store.setUpdatedAt(Instant.now().toString());
        return storeRepository.save(store);
    }

    // 獲取店家詳情
    public Store getStoreById(String storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("找不到該店家 ID: " + storeId));
    }

    // 在指定店家中搜尋商品
    public List<Store.MenuItem> searchProductInStore(String storeId, String keyword) {
        Store store = getStoreById(storeId);
        if (store.getMenu() == null) return new ArrayList<>();
        return store.getMenu().stream()
                .filter(item -> item.getItemName() != null &&
                        (keyword == null || keyword.isBlank() || item.getItemName().contains(keyword)))
                .collect(Collectors.toList());
    }

    // 更新店家分類（分類由菜單項目的 tag 欄位自動衍生，此方法僅做權限驗證）
    public Store updateCategories(String storeId, List<String> categories, String userId) {
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) throw new IllegalArgumentException("STORE_NOT_FOUND");
        if (userId == null || !userId.equals(store.getOwnerId())) throw new SecurityException("FORBIDDEN");
        // 分類是從菜單項目的 tag 欄位衍生的，不需要單獨儲存
        // 此端點保留用於未來擴展
        return store;
    }

    // 修復資料庫中格式不正確的 businessHours 資料
    public int fixBusinessHoursData() {
        // 使用 MongoTemplate 直接操作，避免反序列化錯誤
        int totalFixed = 0;
        
        // 修復 1: 查找 businessHours 不是陣列類型的文檔（type 4 = array）
        Query query1 = new Query();
        query1.addCriteria(Criteria.where("businessHours").exists(true).not().type(4));
        
        Update update1 = new Update();
        update1.set("businessHours", new ArrayList<>());
        
        var result1 = mongoTemplate.updateMulti(query1, update1, "store");
        totalFixed += (int) result1.getModifiedCount();
        
        // 修復 2: 查找 businessHours 是字符串類型的文檔（type 2 = string）
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("businessHours").type(2));
        
        Update update2 = new Update();
        update2.set("businessHours", new ArrayList<>());
        
        var result2 = mongoTemplate.updateMulti(query2, update2, "store");
        totalFixed += (int) result2.getModifiedCount();
        
        // 修復 3: 查找 businessHours 不存在的文檔，設置為空陣列
        Query query3 = new Query();
        query3.addCriteria(Criteria.where("businessHours").exists(false));
        
        Update update3 = new Update();
        update3.set("businessHours", new ArrayList<>());
        
        var result3 = mongoTemplate.updateMulti(query3, update3, "store");
        totalFixed += (int) result3.getModifiedCount();
        
        return totalFixed;
    }
}
