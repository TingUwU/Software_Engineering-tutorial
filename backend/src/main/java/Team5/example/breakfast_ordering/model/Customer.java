package Team5.example.breakfast_ordering.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "customer") // 對應資料庫中的 customer 集合 
public class Customer {

    @Id
    private String id; // 顧客ID 

    @Indexed(unique = true) // required: true, unique: true
    private String account; // 使用者帳號 

    // required: true (記得在 Service 層做加密)
    private String password; // 使用者密碼 

    // default: ""
    private String nickname = ""; // 使用者名稱 

    // required: true, enum: ['owner', 'buyer', 'admin']
    private String role; // 角色

    // default: ""
    private String photo = ""; // 大頭貼 

    // default: ""
    private String phone = ""; // 電話 

    @Indexed(unique = true) // required: true, unique: true
    private String email; // 電子郵件 

    // default: [], ref: store (這裡存 Store 的 ID 字串即可)
    private List<String> favorStores = new ArrayList<>(); // 收藏店家 

    // default: []
    private List<FavorItem> favorItems = new ArrayList<>(); // 收藏商品 

    // default: []
    private List<CustomCombo> customCombos = new ArrayList<>(); // 自訂義組合 

    @LastModifiedDate
    private Date updatedAt; // 更新時間 

    // --- 內部類別 ---

    public static class FavorItem {
        private String storeId;
        private String itemId;
        // getters, setters...
    }

    public static class CustomCombo {
        private String comboName;
        private List<FavorItem> items; // 組合內的商品列表
        // getters, setters...

    }
    // (記得生成 Getters 和 Setters)
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
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
