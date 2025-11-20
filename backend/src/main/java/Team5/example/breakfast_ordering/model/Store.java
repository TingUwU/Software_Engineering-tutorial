package Team5.example.breakfast_ordering.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "store") // 對應資料庫中的 store 集合
public class Store {

    @Id
    private String id; // 店家ID: ObjectId (PK) 

    // required: true
    private String name; // 店家名稱 

    // default: ""
    private String description = ""; // 店家簡介 

    // default: ""
    private String phone = ""; // 聯絡電話 

    // default: ""
    private String email = ""; // 聯絡信箱 

    // required: true
    private String address; // 店家地址 

    // required: true (格式: [經度, 緯度])
    private List<Double> coordinates = new ArrayList<>(); // 經緯度座標 

    // default: []
    private List<BusinessHour> businessHours = new ArrayList<>(); // 營業時間 

    // default: []
    private List<MenuItem> menu = new ArrayList<>(); // 菜單 

    // default: true
    private Boolean isActive = true; // 店家狀態 

    @LastModifiedDate
    private Date updatedAt; // 更新時間: default 現在時間 

    // --- 內部類別 (Inner Classes) 用於定義複雜結構 ---

    public static class BusinessHour {
        private String day;
        private String time;
        private String close;
        // getters, setters...
    }

    public static class MenuItem {
        private String id; // ObjectId
        private String itemName;
        private Integer price;
        private String description;
        private String imgUrl;
        private Boolean isAvailable;
        // getters, setters...
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public List<BusinessHour> getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(List<BusinessHour> businessHours) {
        this.businessHours = businessHours;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public void setMenu(List<MenuItem> menu) {
        this.menu = menu;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // (記得生成所有欄位的 Getters 和 Setters)
    
}
