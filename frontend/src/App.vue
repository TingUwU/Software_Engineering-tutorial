<template>
    <div id="app">
        <nav>
            <router-link to="/">
                <img class="avatar"
                     :src="require('@/assets/logo.png')"
                     alt="logo">
                早一點
            </router-link>
        </nav>

        <!-- HomeView.vue 會在這裡渲染 -->
        <router-view />
    </div>
</template>

<script>
    export default {
        data() {
            return {
                customer: {
                    photo: null
                }
            };
        }
    };
</script>

<style>
    #app {
        font-family: Avenir, Helvetica, Arial, sans-serif;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        text-align: center;
    }

    nav {
        padding: 30px;
    }

        nav a {
            font-weight: bold;
            color: #000; /* 黑色字 */
            text-decoration: none; /* 去掉底線 */
        }

            /* 當前頁面文字顯示藍色 */
            nav a.router-link-exact-active {
                color: #0069D9; /* 藍色 */
            }

            nav a:hover {
                color: #0069D9;
            }


    .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        vertical-align: middle;
        margin-right: 10px;
    }
</style>

