package Team5.example.breakfast_ordering;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/store-page")
public class StorePageController {
    @Autowired
    private StoreRepository storeRepository;

    // 回傳 ID 為 storeId 的店家
    @GetMapping("/{storeId}")
    public Store getStoreDetails(@PathVariable String storeId){
        Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new RuntimeException("找不到該店家 ID: " + storeId));

        return store;
    }

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
}
