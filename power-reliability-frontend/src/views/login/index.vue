<template>
  <div class="login-wrapper">
    <div class="top-nav">
    </div>
    <div class="container">
      <div class="left"></div>
      <div class="right">
        <div class="login-main-content">
          <img :onerror='`this.src="${require("@/assets/noimgsrc.png")}"`' class="tit_img" src="~@/assets/login/tit_img.png" />
          <div class="tips_msg">你好，欢迎登录</div>
          <component :is="activeCom"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import LoginByPwd from './component/LoginByPwd'

export default {
  name: 'Login',
  components: {
    LoginByPwd
  },
  data() {
    return {
      activeCom: 'LoginByPwd',
      titleMap: {
        LoginByPwd: '欢迎登录'
      }
    }
  },
  watch: {},
  created() {
  },
  methods: {}
}
</script>

<style scoped lang="scss">
@import "../../styles/xr-theme.scss";
@import 'index';
.login-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  background: url('~@/assets/login/login_bg3.png') center no-repeat;
  background-size: cover;
  display: flex;
  flex-direction: column;
  overflow: auto;

  .top-nav {
    width: 100%;
    padding: 23px 2.8% 0;
    display: flex;
    img {
      width: 204px;
      height: 50px;
    }
  }

  .container {
    width: 935px;
    height: 460px;
    background-color: #fff;
    background: url('~@/assets/login/container_bg.png') center no-repeat;
    position: relative;
    left: 50%;
    top: 50%;
    transform: translate(-50%,-50%);
    display: flex;
    align-items: center;
    overflow: auto;
    margin-top: -30px;
    border-radius: 10px;
    .left {
      width: 58%;
      height: 100%;
      position: relative;
    }
    .right {
      width: 42%;
      .login-main-content {
        position: relative;
        width: 100%;
        height: 460px;
        border-radius: 6px;
        overflow: hidden;
      }
    }
  }
}

.tit_img{
  margin-top: 63px;
}
.tips_msg{
  color: #4887FF;
  font-size: 14px;
  margin-top: 30px;
  margin-bottom: 34px;
}
</style>
