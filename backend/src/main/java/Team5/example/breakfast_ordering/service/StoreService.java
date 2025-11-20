package Team5.example.breakfast_ordering.service;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    // 功能 1: 建立新店家
    public Store createStore(Store store) {
        return storeRepository.save(store);
    }

    // 功能 2: 取得所有店家
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }
}