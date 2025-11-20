package Team5.example.breakfast_ordering.service;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    public Store createStore(Store store) {
        // 設定更新時間
        store.setUpdatedAt(Instant.now().toString());
        // 預設啟用
        store.setActive(true);
        return storeRepository.save(store);
    }

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }
}
