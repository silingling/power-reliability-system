<template>
  <div id="app">
    <router-view class="router-view" v-if="isRouterAlive"/>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import cache from '@/utils/cache'
import disableDevtool from 'disable-devtool';

export default {
  name: 'App',
  data() {
    return {
      isRouterAlive: true  // 控制 router-view 是否显示达到刷新效果
    }
  },
  computed: {
    ...mapGetters(['activeIndex', 'userInfo'])
  },
  watch: {
    $route(to, from) {}
  },
  created(){
    if(process.env.NODE_ENV == 'production' && process.env.DEV_TOOL_FORBID){
      disableDevtool()
    }
  },
  mounted() {
    this.addDocumentVisibilityChange()
    this.setMinHeight()
  },
  provide () {
    return {
      reload: this.reload
    }
  },
  methods: {
    reload () {
      this.isRouterAlive = false
      this.$nextTick(function () {
        this.isRouterAlive = true
      })
    },
    addDocumentVisibilityChange() {
      var state, visibilityChange
      if (typeof document.hidden !== 'undefined') {
        visibilityChange = 'visibilitychange'
        state = 'visibilityState'
      } else if (typeof document.mozHidden !== 'undefined') {
        visibilityChange = 'mozvisibilitychange'
        state = 'mozVisibilityState'
      } else if (typeof document.msHidden !== 'undefined') {
        visibilityChange = 'msvisibilitychange'
        state = 'msVisibilityState'
      } else if (typeof document.webkitHidden !== 'undefined') {
        visibilityChange = 'webkitvisibilitychange'
        state = 'webkitVisibilityState'
      }
      document.addEventListener(visibilityChange, () => {
        if (document[state] == 'visible') {
          cache.updateAxiosHeaders()
        }
        this.$bus.emit('document-visibility', document[state])
      }, false)
    },
    setMinHeight() {
      this.$nextTick(() => {
        const dpr = window.devicePixelRatio || 1
        const clientWidth = document.body.clientWidth
        const dom = document.getElementById('app')
        if (dpr !== 1 && clientWidth > 1600) {
          dom.style.minHeight = '800px'
        } else if (dpr === 1 && clientWidth > 1600) {
          dom.style.minWidth = '1650px'
        } else {
          dom.style.minHeight = '605px'
        }
      })
    }
  }
}
</script>

<style>
#app {
  width: 100%;
  position: relative;
  height: 100%;
  min-width: 1200px;
  min-height: 605px;
}
</style>
