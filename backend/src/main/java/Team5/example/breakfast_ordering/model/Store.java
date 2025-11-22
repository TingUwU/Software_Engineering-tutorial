package Team5.example.breakfast_ordering.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;

@Document(collection = "store")
public class Store {
    @Id
    private String id;   // 店家 ID

    private String name;               // 店家名稱
    private String description;        // 店家簡介
    private String ownerId;            // 擁有者 ID
    private String phone;              // 聯絡電話
    private String email;              // 聯絡信箱
    private String address;            // 店家地址
    private List<Double> coordinates;  // 經緯度座標，MongoDB標準順序是[經度, 緯度]、Google Maps顯示的順序是[緯度, 經度]

    private List<BusinessHour> businnessHours;  // 營業時間
    private List<MenuItem> menu;                // 菜單
    private Date updatedAt;      // 更新時間

    private Boolean isActive;    // 店家狀態

    /////////////////////////////////////////////

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public String getOwnerId(){
        return ownerId;
    }
    public void setOwnerId(String ownerId){
        this.ownerId = ownerId;
    }

    public String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public String getAddress(){
        return address;
    }
    public void setAddress(String address){
        this.address = address;
    }

    public List<Double> getCoordinates(){
        return coordinates;
    }
    public void setCoordinates(List<Double> coordinates){
        this.coordinates = coordinates;
    }

    public List<BusinessHour> getBusinessHour(){
        return businnessHours;
    }
    public void setBusinessHour(List<BusinessHour> businnessHours){
        this.businnessHours = businnessHours;
    }

    public List<MenuItem> getMenu(){
        return menu;
    }
    public void setMenu(List<MenuItem> menu){
        this.menu = menu;
    }

    public Date getUpdatedAt(){
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt){
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive(){
        return isActive;
    }
    public void setIsActive(Boolean isActive){
        this.isActive = isActive;
    }

    /////////////////////////////////////////////

    public static class BusinessHour{
        private String day;        // 星期幾
        private String start;      // 開始營業時間
        private String close;      // 打烊時間
        private String note;

        public String getDay(){
            return day;
        }
        public void setDay(String day){
            this.day = day;
        }

        public String getStart(){
            return start;
        }
        public void setStart(String start){
            this.start = start;
        }

        public String getClose(){
            return close;
        }
        public void setClose(String close){
            this.close = close;
        }

        public String getNote(){
            return note;
        }
        public void setNote(String note){
            this.note = note;
        }
    }

    public static class MenuItem{
        @Id
        private String id;

        private String itemName;
        private Double price;
        private String description;
        private String imgUrl;
        private List<String> customizationOptions = new ArrayList<>();
        private Integer sales = 0;  // for 銷量統計

        private Boolean isAvailable;
    
        /////////////////////////////////////////////

        public MenuItem() {
            this.id = new org.bson.types.ObjectId().toString();
        }
        
        public String getId(){
            return id;
        }
        public void setId(String id){
            this.id = id;
        }

        public String getItemName(){
            return itemName;
        }
        public void setItemName(String itemName){
            this.itemName = itemName;
        }

        public Double getPrice(){
            return price;
        }
        public void setPrice(Double price){
            this.price = price;
        }

        public String getDescription(){
            return description;
        }
        public void setDescription(String description){
            this.description = description;
        }

        public String getImgUrl(){
            return imgUrl;
        }
        public void setImgUrl(String imgUrl){
            this.imgUrl = imgUrl;
        }

        public List<String> getCustomizationOptions(){
            return customizationOptions;
        }
        public void setCustomizationOptions(List<String> customizationOptions){
            this.customizationOptions = customizationOptions;
        }

        public Integer getSales(){
            return sales;
        }
        public void setSales(Integer sales){
            this.sales = sales;
        }

        public Boolean getIsAvailable(){
            return isAvailable;
        }
        public void setIsAvailable(Boolean isAvailable){
            this.isAvailable = isAvailable;
        }
    }
}