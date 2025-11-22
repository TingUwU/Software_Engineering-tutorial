package Team5.example.breakfast_ordering.repository;

import Team5.example.breakfast_ordering.model.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CMRepository extends MongoRepository<Store, String> {

    // 對應 CM-001: 只列出「營業中 (isActive=true)」的店家
    // 這樣顧客就不會看到已經倒閉或休息的店
    List<Store> findByIsActiveTrue();

    // 對應 CM-002: 搜尋功能
    // 同時搜尋「店家名稱 (name)」 OR 「菜單裡的餐點名稱 (menu.itemName)」
    // ?0 代表第一個參數 keyword
    // $options: 'i' 代表忽略大小寫 (Case-insensitive)
    @Query("{ '$or': [ { 'name': { $regex: ?0, $options: 'i' } }, { 'menu.itemName': { $regex: ?0, $options: 'i' } } ] }")
    List<Store> searchStores(String keyword);
}