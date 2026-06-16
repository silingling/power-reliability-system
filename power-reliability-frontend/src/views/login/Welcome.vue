<template>
  <div
    class="welcome-wrapper">
    <div class="top-nav">
      <img   :onerror='`this.src="${require("@/assets/noimgsrc.png")}"`'   
        src="~@/assets/logo_white.png"
        alt="">
    </div>
    <div class="container">

      <div class="welcome-main-content">
        <div class="welcome__title">
          <i class="el-icon-success"/>欢迎使用低压供电可靠性全流程管控系统
        </div>

        <div class="welcome__des">系统部署成功，请完成管理员账号初始化设置。</div>

        <div class="welcome__main">
          <div class="welcome__tips">请设置管理员账号和密码，该账号将作为您登录系统的管理员帐号。
          设置完成后即可进入系统进行业务管理。</div>

          <login-by-welcome />
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import LoginByWelcome from './component/LoginByWelcome'

export default {
  name: 'Login',
  components: {
    LoginByWelcome
  },
  data() {
    return {}
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

.welcome-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  background: url('~@/assets/login/welcome_bg.png') center no-repeat;
  background-size: cover;
  display: flex;
  flex-direction: column;
  overflow: auto;

  .top-nav {
    width: 100%;
    padding: 23px 2.8% 0;
    display: flex;
    img {
      width: 164px;
      height: 50px;
    }
  }

  .container {
    width: 100%;
    flex: 1;
    text-align: center;
  }

  .welcome-main-content {
    position: relative;
    width: 700px;
    border-radius: 6px;
    overflow: hidden;
    display: inline-block;

    .welcome {
      &__title {
        font-weight: bold;
        font-size: 30px;
        color: white;
        margin: 15px 0 20px;
        .el-icon-success {
          font-size: 30px;
          color: #20b559;
          background-color: white;
          border-radius: 15px;
          margin-right: 24px;
        }
      }

      &__des {
        color: white;
        line-height: 2;
        padding: 10px 0;
      }

      &__main {
        padding: 0 40px;
        margin-bottom: 100px;
      }

      &__tips {
        background-color: white;
        line-height: 2;
        margin: 20px 0;
        padding: 20px 40px;
        border-radius: 6px;
        overflow: hidden;

        a {
          color: $xr-color-primary;
        }
      }
    }
  }
}
</style>
