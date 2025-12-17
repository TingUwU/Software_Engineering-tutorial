<template>
  <div class="login-page">
    <div class="login-container">
      <h1 class="login-title">北寧路早餐店系統</h1>
      
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="account">帳號</label>
          <input 
            type="text" 
            id="account" 
            v-model="account" 
            placeholder="請輸入帳號"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密碼</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            placeholder="請輸入密碼"
            required
          />
        </div>
        </form>

        <div class="choose-and-register">
          <div class="radio-group">
            <label class="radio-item">
              <input type="radio" id="buyer" value="buyer" v-model="role">
              <span>顧客</span>
            </label>
            <label class="radio-item">
              <input type="radio" id="owner" value="owner" v-model="role">
              <span>店家</span>
            </label>
         </div>
          <div class="btn">
            <button type="submit" class="btn-login" @click="handleRegister">註冊</button>
          </div>
        </div>

        <div class="forget-and-login">
            <div class="forget-password">
                <a @click="forgetPassword" class="link">忘記密碼</a>
            </div>
            <div class="btn">
                <button type="submit" class="btn-login" @click="handleLogin">登入</button>
            </div>
        </div>

      <div style="border-top: 2px solid #0069D9; margin-top: 10px;"></div>
      <h5 style="text-align: left; margin-top: 10px;">其他方式登入</h5>
      
      <div class="other-login-way">
        <div class="other-way-image google" @click="loginWithGoogle">
          <span>Google</span>
        </div>
        <div class="other-way-image facebook" @click="loginWithFacebook">
          <span>Facebook</span>
        </div>
      </div>

      
      <div class="skip-login">
        <a @click="skipLogin" class="link">先不登入</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

const router = useRouter()
const account = ref('')
const password = ref('')
const role = ref('buyer') // 預設選擇顧客，對應enum: buyer/owner/admin
const store = useStore()


const handleRegister =() =>{
    console.log('註冊')
    router.push('/register')
}
const handleLogin = async () => {
  const loginData = {
    account: account.value,
    password: password.value,
    role: role.value
  }
  
  console.log('登入數據:', loginData)
  console.log('選擇的角色（僅供前端參考）:', role.value)

  try {
    // 將登入資訊存入 Vuex，並等待後端回應
    const userData = await store.dispatch('user/login', loginData)
    
    // 同時存入 localStorage，刷新頁面仍能保持登入
    localStorage.setItem('user', JSON.stringify(userData))
    
    alert('登入成功！')
    
    if (userData.role === 'owner') {
      router.push('/store-setting')
    } else if (userData.role === 'buyer') {
      router.push('/home')
    } 
    
  } catch (error) {
    // 登入失敗時顯示錯誤訊息
    console.error('登入錯誤:', error)
    alert(`登入失敗：${ error.message}`)
  }
}

const skipLogin = () => {
  // 跳過登入，直接進入主頁
  alert('訪客模式')
  router.push('/nologinhome')
}

const loginWithGoogle = () => {
  // Google 登入功能（未實作）
  alert('未實作')
}

const loginWithFacebook = () => {
  // Facebook 登入功能（未實作）
  alert('未實作')
}

const forgetPassword = () => {
  // 忘記密碼功能（未實作）
  alert('未實作')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background-color: #0069D9;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  background-color: white;
  border-radius: 10px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  font-size: 28px;
  margin-bottom: 30px;
  color: #333;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.choose-and-register {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    gap: 15px;
}

.radio-group {
    display: flex;
    flex-direction: row;
    gap: 20px;
    align-items: center;
}

.radio-item {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
    font-size: 16px;
    color: #333;
}

.radio-item input[type="radio"] {
    cursor: pointer;
    width: 18px;
    height: 18px;
}

.radio-item span {
    white-space: nowrap;
}

.btn {
    width:100%;
    display:flex;
    justify-content: flex-end;
}

.form-group label {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  padding: 12px 15px;
  font-size: 16px;
  border: 2px solid #ddd;
  border-radius: 5px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #0069D9;
}

.form-group input::placeholder {
  color: #999;
}

.forget-and-login {
    display: flex;
    align-items: flex-end;
    justify-content: flex-end;
    gap: 15px;
}

.forget-password {
    display: flex;
    justify-content: flex-end;
    text-align: right;
    width: 1200px;
}

.btn-login {
  width: 120px;
  padding: 14px 20px;
  background-color: #0069D9;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 18px;
  cursor: pointer;
  margin-top: 10px;
  transition: background-color 0.3s;
}

.btn-login:hover {
  background-color: #0056b3;
}

.skip-login {
  text-align: center;
  margin-top: 20px;
}

.link {
  color: #666;
  font-size: 14px;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.3s;
}

.link:hover {
  color: #0069D9;
}

.other-login-way {
  display: flex;
  margin-top: 15px;
  margin-bottom: 15px;
  gap: 15px;
}

.other-way-image {
  width: 300px;
  height: 120px;
  border: 2px solid #ddd;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  gap: 5px;
}

.other-way-image span {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}
nav {
  display: none;
}
</style>

