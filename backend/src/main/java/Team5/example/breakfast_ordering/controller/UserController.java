package Team5.example.breakfast_ordering.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.ArrayList;

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

    // 新增自訂義組合（內容為空，只有 combo 名稱）
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
                                   @PathVariable String storeId, @PathVariable String itemId
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

        boolean exists = targetCombo.getItems().stream()
                         .anyMatch(item -> item.getItemId().equals(itemId));

        if(exists){
            throw new RuntimeException("該商品已在組合中");
        }

        targetCombo.getItems().add(new FavorItem(storeId, itemId));

        return userRepository.save(user);
    }

    // 將商品移除自訂義組合
    @DeleteMapping("/{userId}/custom-combos/{comboId}/items/{storeId}/{itemId}")
    public User deleteCustomComboItem(@PathVariable String userId, @PathVariable String comboId,
                                   @PathVariable String storeId, @PathVariable String itemId
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

        boolean isRemoved = targetCombo.getItems()
                            .removeIf(item -> item.getItemId().equals(itemId));

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
