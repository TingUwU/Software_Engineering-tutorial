package Team5.example.breakfast_ordering.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Document(collection = "user")
public class User {

    @Id
    private String id; // 使用者 ID

    @Indexed(unique = true)
    @NotBlank(message = "使用者帳號不能為空")
    private String account;       // 使用者帳號

    @NotBlank(message = "使用者密碼不能為空")
    private String password;      // 使用者密碼，儲存加密後的密碼

    private String nickname = ""; // 使用者名稱

    @NotBlank(message = "角色不能為空")
    @Pattern(regexp = "^(owner|buyer|admin)$", message = "角色必須是 owner, buyer 或 admin 其中之一")
    private String role;          // 角色

    private String photo = "";    // 大頭貼

    private String phone = "";    // 電話

    private String email;         // 電子郵件

    private AuthProvider provider;  // 紀錄是使用哪種方式登入

    private String providerId;      // 記錄第三方登入給的唯一 ID

    // 儲存 Store 的 ID 字串
    private List<String> favorStores = new ArrayList<>();       // 收藏店家

    private List<FavorItem> favorItems = new ArrayList<>();     // 收藏商品

    private List<CustomCombo> customCombos = new ArrayList<>(); // 自訂義組合

    @LastModifiedDate
    private Date updatedAt;      // 更新時間

    ///////////////////////////////////////////
    
    public enum AuthProvider {
        LOCAL, GOOGLE, FACEBOOK
    }

    public static class FavorItem {
        private String storeId;
        private String itemId;
        private List<String> customizations; // 客製化選項
        private Integer quantity; // 商品數量

        public FavorItem(){}

        public FavorItem(String storeId, String itemId){
            this.storeId = storeId;
            this.itemId = itemId;
            this.customizations = new ArrayList<>();
            this.quantity = 1;
        }

        public FavorItem(String storeId, String itemId, List<String> customizations){
            this.storeId = storeId;
            this.itemId = itemId;
            this.customizations = customizations != null ? customizations : new ArrayList<>();
            this.quantity = 1;
        }

        public FavorItem(String storeId, String itemId, List<String> customizations, Integer quantity){
            this.storeId = storeId;
            this.itemId = itemId;
            this.customizations = customizations != null ? customizations : new ArrayList<>();
            this.quantity = quantity != null ? quantity : 1;
        }

        public String getStoreId(){
            return storeId;
        }
        public void setStoreId(String storeId){
            this.storeId = storeId;
        }

        public String getItemId(){
            return itemId;
        }
        public void setItemId(String itemId){
            this.itemId = itemId;
        }

        public List<String> getCustomizations(){
            return customizations;
        }
        public void setCustomizations(List<String> customizations){
            this.customizations = customizations != null ? customizations : new ArrayList<>();
        }

        public Integer getQuantity(){
            return quantity;
        }
        public void setQuantity(Integer quantity){
            this.quantity = quantity != null ? quantity : 1;
        }
    }

    public static class CustomCombo {
        private String comboId;
        private String comboName;
        private String storeId;
        private List<FavorItem> items = new ArrayList<>();

        public CustomCombo(){
            this.comboId = UUID.randomUUID().toString();
        }

        public CustomCombo(String comboName){
            this();
            this.comboName = comboName;
        }

        ////////////////////////////////
        
        public String getComboId(){
            return comboId;
        }
        public void setComboId(String comboId){
            this.comboId = comboId;
        }

        public String getComboName(){
            return comboName;
        }
        public void setComboName(String comboName){
            this.comboName = comboName;
        }

        public String getStoreId(){
            return storeId;
        }
        public void setStoreId(String storeId){
            this.storeId = storeId;
        }

        public List<FavorItem> getItems(){
            return items;
        }
        public void setItems(List<FavorItem> items){
            this.items = items;
        }

    }

    ///////////////////////////////////////////

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRole(){
        return role;
    }
    public void setRole(String role){
        this.role = role;
    }

    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public AuthProvider getProvider(){
        return provider;
    }

    public void setProvider(AuthProvider provider){
        this.provider = provider;
    }

    public String getProviderId(){
        return providerId;
    }

    public void setProviderId(String providerId){
        this.providerId = providerId;
    }

    public List<String> getFavorStores() {
        return favorStores;
    }
    public void setFavorStores(List<String> favorStores) {
        this.favorStores = favorStores;
    }

    public List<FavorItem> getFavorItems() {
        return favorItems;
    }
    public void setFavorItems(List<FavorItem> favorItems) {
        this.favorItems = favorItems;
    }

    public List<CustomCombo> getCustomCombos() {
        return customCombos;
    }
    public void setCustomCombos(List<CustomCombo> customCombos) {
        this.customCombos = customCombos;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
