package Team5.example.breakfast_ordering.service;

import Team5.example.breakfast_ordering.model.Store;
import Team5.example.breakfast_ordering.repository.CMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CMService {

    @Autowired
    private CMRepository cmRepository;

    // 處理顧客主頁面的資料請求
    public List<Store> getHomePageData(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 如果顧客有輸入關鍵字 -> 執行搜尋 (CM-002)
            return cmRepository.searchStores(keyword.trim());
        } else {
            // 如果沒輸入 -> 列出所有營業中店家 (CM-001)
            return cmRepository.findByIsActiveTrue();
        }
    }
}