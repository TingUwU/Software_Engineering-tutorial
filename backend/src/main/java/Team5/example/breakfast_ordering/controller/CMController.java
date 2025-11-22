package Team5.example.breakfast_ordering.controller;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.service.CMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cm") // 我們把這個頁面的 API 都歸類在 /api/cm 底下
@CrossOrigin(origins = "*") // 允許前端跨域呼叫
public class CMController {

    @Autowired
    private CMService cmService;

    // API: 取得顧客主頁面資料 (包含 CM-001 列表 & CM-002 搜尋)
    // 網址: GET /api/cm/stores
    // 搜尋: GET /api/cm/stores?keyword=蛋餅
    @GetMapping("/stores")
    public List<Store> getCustomerHomePage(@RequestParam(required = false) String keyword) {
        return cmService.getHomePageData(keyword);
    }
}