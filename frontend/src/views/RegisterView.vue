<template>
  <div class="register-page">
    <div class="register-container">
      <h1 class="register-title">會員註冊</h1>
      
      <form class="register-form" @submit.prevent="handleRegister">
        <div class="form-group">
          <input 
            type="text" 
            id="account" 
            v-model="account" 
            placeholder="請輸入帳號"
            required
          />
        </div>

        <div class="form-group">
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            placeholder="請輸入密碼"
            required
          />
        </div>

        <div class="form-group">
          <input 
            type="password" 
            id="confirmPassword" 
            v-model="confirmPassword" 
            placeholder="請再次輸入密碼"
            required
          />
        </div>

        <div class="form-group">
          <input 
            type="email" 
            id="email" 
            v-model="email" 
            placeholder="請輸入電子郵件"
            required
          />
        </div>

        <div class="form-group">
          <input 
            type="text" 
            id="nickname" 
            v-model="nickname" 
            placeholder="請輸入名稱"
          />
        </div>

        <div class="form-group">
          <input 
            type="tel" 
            id="phone" 
            v-model="phone" 
            placeholder="請輸入電話"
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
      </div>

      <div class="register-actions">
        <button type="submit" class="btn-register" @click="handleRegister">註冊</button>
      </div>

      <div style="border-top: 2px solid #0069D9; margin-top: 20px;"></div>
      <h5 style="text-align: left; margin-top: 10px;">其他方式註冊</h5>
      
      <div class="other-register-way">
        <div class="other-way-image google" @click="registerWithGoogle">
          <span>Google</span>
        </div>
        <div class="other-way-image facebook" @click="registerWithFacebook">
          <span>Facebook</span>
        </div>
      </div>

      <div class="back-to-login">
        <a @click="backToLogin" class="link">已有帳號？返回登入</a>
      </div>
    </div>
  </div>
</template>

<script setup>
    import { ref } from 'vue'
    import { useRouter } from 'vue-router'
    import { useStore } from 'vuex'

    const router = useRouter()
    const store = useStore()

    const account = ref('')
    const password = ref('')
    const confirmPassword = ref('')
    const email = ref('')
    const nickname = ref('')
    const phone = ref('')
    const role = ref('buyer') // 預設選擇顧客，對應後端 enum: buyer/owner/admin

    const handleRegister = async () => {
        if (password.value !== confirmPassword.value) {
            alert('密碼與確認密碼不一致，請重新輸入')
            return
        }

        const registerData = {
            account: account.value,
            password: password.value,
            email: email.value,
            nickname: nickname.value,
            phone: phone.value,
            role: role.value,
            photo: ''
        }

        try {
            const response = await fetch('http://localhost:3000/api/users/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(registerData)
            })

            if (!response.ok) {
                const err = await response.json()
                throw new Error(err.message || '註冊失敗')
            }

            const data = await response.json()

            // 更新 Vuex 狀態
            store.commit('user/UPDATE_CUSTOMER', data)
            store.commit('user/LOGIN')

            alert('註冊成功！')
            router.push('/login')

        } catch (err) {
            console.error('註冊錯誤:', err)
            alert(`註冊失敗: ${err.message}`)
        }
    }

const backToLogin = () => {
  router.push('/login')
}

const registerWithGoogle = () => {
  alert('未實作')
}

const registerWithFacebook = () => {
  alert('未實作')
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background-color: #0069D9;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  background-color: white;
  border-radius: 10px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  font-size: 28px;
  margin-bottom: 10px;
  color: #333;
}



.register-form {
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
  justify-content: flex-start;
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

.register-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.btn-register {
  width: 120px;
  padding: 14px 20px;
  background-color: #0069D9;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-register:hover {
  background-color: #0056b3;
}

.back-to-login {
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

.other-register-way {
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
</style>

