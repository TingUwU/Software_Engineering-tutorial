package Team5.example.breakfast_ordering.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import Team5.example.breakfast_ordering.model.User;
import Team5.example.breakfast_ordering.model.User.FavorItem;
import Team5.example.breakfast_ordering.model.User.CustomCombo;
import Team5.example.breakfast_ordering.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    // 註冊
   @PostMapping("/register")
    public User register(@Valid @RequestBody User user){
        if(userRepository.existsByAccount(user.getAccount())){
            throw new RuntimeException("帳號已存在");
        }

        // 驗證角色是否有效（必須是 buyer 或 owner）
        if(user.getRole() == null || user.getRole().isBlank()){
            throw new RuntimeException("請選擇身份（顧客或店家）");
        }
        if(!user.getRole().equals("buyer") && !user.getRole().equals("owner")){
            throw new RuntimeException("身份選擇無效，請選擇顧客或店家");
        }

        return userRepository.save(user);
    }

      // 登入
    @PostMapping("/login")
    public User login(@RequestBody User loginRequest){
        User user = userRepository.findByAccount(loginRequest.getAccount())
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        if(!user.getPassword().equals(loginRequest.getPassword())){  // 密碼尚未加密，待更改
            throw new RuntimeException("密碼錯誤");
        }

        // 驗證前端選擇的角色是否與數據庫中的角色一致
        if(loginRequest.getRole() == null || loginRequest.getRole().isBlank()){
            throw new RuntimeException("請選擇身份（顧客或店家）");
        }
        if(!user.getRole().equals(loginRequest.getRole())){
            throw new RuntimeException("身份選擇錯誤，該帳號註冊時選擇的是" + 
                (user.getRole().equals("owner") ? "店家" : "顧客"));
        }

        return user;
    }

    // 回傳使用者資訊
    @GetMapping("/{userId}")
    public User getUserProfile(@PathVariable String userId){
        return userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));
    }

    // 修改使用者資訊
    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable String userId, @RequestBody User userUpdates){
        User newUser = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        // 能修改的資訊只有密碼、名稱、大頭貼、電話、電子郵件
        if(userUpdates.getPassword() != null && !userUpdates.getPassword().isBlank()){
            newUser.setPassword(userUpdates.getPassword());   // 密碼尚未加密，待更改
        }
        if(userUpdates.getNickname() != null && !userUpdates.getNickname().isBlank()){
            newUser.setNickname(userUpdates.getNickname());
        }
        if(userUpdates.getPhoto() != null && !userUpdates.getPhoto().isBlank()){
            newUser.setPhoto(userUpdates.getPhoto());
        }
        if(userUpdates.getPhone() != null && !userUpdates.getPhone().isBlank()){
            newUser.setPhone(userUpdates.getPhone());
        }
        if(userUpdates.getEmail() != null && !userUpdates.getEmail().isBlank()){
            newUser.setEmail(userUpdates.getEmail());
        }

        return userRepository.save(newUser);
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable String userId){
        if(!userRepository.existsById(userId)){
            throw new RuntimeException("帳號不存在");
        }

        userRepository.deleteById(userId);

        return "使用者 ID: " + userId + " 已成功刪除";
    }

    // 將店家加入收藏
    @PostMapping("/{userId}/favorite-stores/{storeId}")
    public User addFavoriteStore(@PathVariable String userId, @PathVariable String storeId){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        if(!user.getFavorStores().contains(storeId)){
            user.getFavorStores().add(storeId);
            return userRepository.save(user);
        }

        return user;
    }

    // 將店家移除收藏
    @DeleteMapping("/{userId}/favorite-stores/{storeId}")
    public User deleteFavoriteStore(@PathVariable String userId, @PathVariable String storeId){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        if(user.getFavorStores()!= null){
            user.getFavorStores().removeIf(id -> id.equals(storeId));
            return userRepository.save(user);
        }

        return user;
    }

    // 回傳收藏的店家 ID 列表
    @GetMapping("/{userId}/favorite-stores")
    public List<String> getFavoriteStores(@PathVariable String userId){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        return user.getFavorStores() != null ? user.getFavorStores() : new ArrayList<>();
    }

    // 將商品加入收藏
    @PostMapping("/{userId}/favorite-items/{storeId}/{itemId}")
    public User addFavoriteItem(@PathVariable String userId, @PathVariable String storeId, @PathVariable String itemId){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        boolean exists = user.getFavorItems().stream()
                         .anyMatch(item -> item.getStoreId().equals(storeId)
                                        && item.getItemId().equals(itemId));

        if(!exists){
            User.FavorItem newItem = new User.FavorItem(storeId, itemId);
            user.getFavorItems().add(newItem);

            return userRepository.save(user);
        }

        return user;
    }

    // 將商品移除收藏
    @DeleteMapping("/{userId}/favorite-items/{storeId}/{itemId}")
    public User deleteFavoriteItem(@PathVariable String userId, @PathVariable String storeId, @PathVariable String itemId){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

         if(user.getFavorItems() != null){
            boolean isRemoved = user.getFavorItems().removeIf(item ->
                                item.getStoreId().equals(storeId) &&
                                item.getItemId().equals(itemId));
            if(isRemoved){
                return userRepository.save(user);
            }
         }

        return user;
    }

    // 回傳收藏的商品列表
    @GetMapping("/{userId}/favorite-items")
    public List<FavorItem> getFavoriteItems(@PathVariable String userId){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        return user.getFavorItems() != null ? user.getFavorItems() : new ArrayList<FavorItem>();
    }

   
    @PostMapping("/{userId}/custom-combos")
    public User addCustomCombo(@PathVariable String userId, @RequestBody CustomCombo comboRequest){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        boolean exists = user.getCustomCombos().stream()
                         .anyMatch(combo -> combo.getComboName().equals(comboRequest.getComboName()));

        if(exists){
            throw new RuntimeException("combo 名稱已存在");
        }

        CustomCombo newCombo = new CustomCombo(comboRequest.getComboName());
        user.getCustomCombos().add(newCombo);

        return userRepository.save(user);
    }

    // 修改自訂義組合名稱
    @PatchMapping("/{userId}/custom-combos/{comboId}")
    public User updateCustomCombo(@PathVariable String userId, @PathVariable String comboId, @RequestBody CustomCombo comboRequest){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        CustomCombo targetCombo = user.getCustomCombos().stream()
                                  .filter(combo -> combo.getComboId().equals(comboId))
                                  .findFirst()
                                  .orElseThrow(() -> new RuntimeException("找不到該 combo"));

        boolean exists = user.getCustomCombos().stream()
                         .anyMatch(combo -> !combo.getComboId().equals(comboId) &&
                                            combo.getComboName().equals(comboRequest.getComboName()));

        if(exists){
            throw new RuntimeException("combo 名稱已存在");
        }

        if(comboRequest.getComboName() != null && !comboRequest.getComboName().isBlank()){
            targetCombo.setComboName(comboRequest.getComboName());
        }
        else{
            throw new RuntimeException("combo 名稱不能為空");  // 只能修改一處的要加 else
        }

        return userRepository.save(user);
    }

    // 刪除自訂義組合
    @DeleteMapping("/{userId}/custom-combos/{comboId}")
    public User deleteCustomCombo(@PathVariable String userId, @PathVariable String comboId){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        if (user.getCustomCombos() == null) {
             throw new RuntimeException("找不到該 combo ID (列表為空)");
        }

        user.getCustomCombos().removeIf(combo -> combo.getComboId().equals(comboId));

        return userRepository.save(user);
    }

    // 將商品加入自訂義組合 (要檢查是否為同一店家)
    @PostMapping("/{userId}/custom-combos/{comboId}/items/{storeId}/{itemId}")
    public User addCustomComboItem(@PathVariable String userId, @PathVariable String comboId,
                                   @PathVariable String storeId, @PathVariable String itemId,
                                   @RequestBody(required = false) Map<String, Object> requestBody
    ){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        CustomCombo targetCombo = user.getCustomCombos().stream()
                                  .filter(combo -> combo.getComboId().equals(comboId))
                                  .findFirst()
                                  .orElseThrow(() -> new RuntimeException("找不到該 combo"));

        if(targetCombo.getStoreId() == null){
            targetCombo.setStoreId(storeId);
        }
        else{
            if(!targetCombo.getStoreId().equals(storeId)){
                throw new RuntimeException("加入失敗，自訂義組合不可跨店家");
            }
        }

        // 處理客製化選項
        List<String> customizations = new ArrayList<>();
        Integer quantity = 1;

        if (requestBody != null) {
            if (requestBody.containsKey("customizations")) {
                @SuppressWarnings("unchecked")
                List<String> customList = (List<String>) requestBody.get("customizations");
                customizations = customList != null ? customList : new ArrayList<>();
            }
            if (requestBody.containsKey("quantity")) {
                quantity = (Integer) requestBody.get("quantity");
            }
        }

        // 檢查是否已經存在相同客製化的商品（不考慮順序）
        final List<String> targetCustomizations = customizations; // 創建 effectively final 的副本
        boolean exists = targetCombo.getItems().stream()
                         .anyMatch(item -> {
                             if (!item.getItemId().equals(itemId)) {
                                 return false;
                             }
                             // 比較客製化選項，忽略順序
                             List<String> existingCustoms = item.getCustomizations();
                             if (existingCustoms.size() != targetCustomizations.size()) {
                                 return false;
                             }
                             return existingCustoms.containsAll(targetCustomizations) && targetCustomizations.containsAll(existingCustoms);
                         });

        if(exists){
            throw new RuntimeException("相同客製化的商品已在組合中");
        }

        targetCombo.getItems().add(new FavorItem(storeId, itemId, customizations, quantity));

        return userRepository.save(user);
    }

    // 更新自訂義組合中的商品 (數量和客製化選項)
    @PatchMapping("/{userId}/custom-combos/{comboId}/items/{storeId}/{itemId}")
    public User updateCustomComboItem(@PathVariable String userId, @PathVariable String comboId,
                                      @PathVariable String storeId, @PathVariable String itemId,
                                      @RequestBody Map<String, Object> updateData) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        CustomCombo targetCombo = user.getCustomCombos().stream()
                                  .filter(combo -> combo.getComboId().equals(comboId))
                                  .findFirst()
                                  .orElseThrow(() -> new RuntimeException("找不到該 combo"));

        // 需要原始客製化信息來識別具體的商品實例
        @SuppressWarnings("unchecked")
        List<String> originalCustomizations = (List<String>) updateData.get("originalCustomizations");

        if (originalCustomizations == null) {
            throw new RuntimeException("需要提供原始客製化信息來識別商品");
        }

        final List<String> targetOriginalCustomizations = originalCustomizations; // 創建 effectively final 的副本
        FavorItem targetItem = targetCombo.getItems().stream()
                              .filter(item -> {
                                  if (!item.getStoreId().equals(storeId) || !item.getItemId().equals(itemId)) {
                                      return false;
                                  }
                                  // 比較客製化選項，忽略順序
                                  List<String> existingCustoms = item.getCustomizations();
                                  if (existingCustoms.size() != targetOriginalCustomizations.size()) {
                                      return false;
                                  }
                                  return existingCustoms.containsAll(targetOriginalCustomizations) && targetOriginalCustomizations.containsAll(existingCustoms);
                              })
                              .findFirst()
                              .orElseThrow(() -> new RuntimeException("找不到該商品"));

        // 更新數量
        if (updateData.containsKey("quantity")) {
            Integer newQuantity = (Integer) updateData.get("quantity");
            targetItem.setQuantity(newQuantity != null ? newQuantity : 1);
        }

        // 更新客製化選項
        if (updateData.containsKey("customizations")) {
            @SuppressWarnings("unchecked")
            List<String> newCustomizations = (List<String>) updateData.get("customizations");
            targetItem.setCustomizations(newCustomizations != null ? newCustomizations : new ArrayList<>());
        }

        return userRepository.save(user);
    }

    // 將商品移除自訂義組合
    @DeleteMapping("/{userId}/custom-combos/{comboId}/items/{storeId}/{itemId}")
    public User deleteCustomComboItem(@PathVariable String userId, @PathVariable String comboId,
                                   @PathVariable String storeId, @PathVariable String itemId,
                                   @RequestBody(required = false) Map<String, Object> deleteData
    ){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        if(user.getCustomCombos() == null){
            throw new RuntimeException("combo 列表為空");
        }

        CustomCombo targetCombo = user.getCustomCombos().stream()
                                  .filter(combo -> combo.getComboId().equals(comboId))
                                  .findFirst()
                                  .orElseThrow(() -> new RuntimeException("找不到該 combo"));

        boolean isRemoved;

        // 如果提供了客製化信息，則用來識別具體的商品實例
        if (deleteData != null && deleteData.containsKey("customizations")) {
            @SuppressWarnings("unchecked")
            final List<String> targetCustomizations = (List<String>) deleteData.get("customizations"); // 創建 effectively final 的副本

            isRemoved = targetCombo.getItems().removeIf(item -> {
                if (!item.getStoreId().equals(storeId) || !item.getItemId().equals(itemId)) {
                    return false;
                }
                // 比較客製化選項，忽略順序
                List<String> existingCustoms = item.getCustomizations();
                if (existingCustoms.size() != targetCustomizations.size()) {
                    return false;
                }
                return existingCustoms.containsAll(targetCustomizations) && targetCustomizations.containsAll(existingCustoms);
            });
        } else {
            // 如果沒有提供客製化信息，刪除第一個匹配的商品（向後兼容）
            isRemoved = targetCombo.getItems()
                        .removeIf(item -> item.getStoreId().equals(storeId) && item.getItemId().equals(itemId));
        }

        if(!isRemoved){
            throw new RuntimeException("移除失敗， combo 中找不到該商品");
        }

        if(targetCombo.getItems().isEmpty()){      // 解除 combo 的店家設定
            targetCombo.setStoreId(null);
        }

        return userRepository.save(user);
    }

    // 回傳自訂義組合列表
    @GetMapping("/{userId}/custom-combos")
    public List<CustomCombo> getAllCustomCombos(@PathVariable String userId){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        return user.getCustomCombos() != null ? user.getCustomCombos() : new ArrayList<>();
    }

    // 回傳特定自訂義組合的內容
    @GetMapping("/{userId}/custom-combos/{comboId}")
    public CustomCombo getCustomComboDetail(@PathVariable String userId, @PathVariable String comboId){
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("帳號不存在"));

        if(user.getCustomCombos() == null){
            throw new RuntimeException("combo 列表為空");
        }

        return user.getCustomCombos().stream()
               .filter(combo -> combo.getComboId().equals(comboId))
               .findFirst()
               .orElseThrow(() -> new RuntimeException("找不到該 combo"));
    }
}
