package Team5.example.breakfast_ordering.controller;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores") // 設定這家店的「總機號碼」
@CrossOrigin(origins = "*") // 允許前端 (Vue) 跨網域呼叫，這很重要！
public class StoreController {

    @Autowired
    private StoreService storeService;

    // API 1: 新增店家 (POST /api/stores)
    @PostMapping
    public Store createStore(@RequestBody Store store) {
        return storeService.createStore(store);
    }

    // API 2: 取得所有店家 (GET /api/stores)
    @GetMapping
    public List<Store> getAllStores() {
        return storeService.getAllStores();
    }
}