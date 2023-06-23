<template>
  <el-button @click="getLoginUrl" size="large" type="primary">
    登录
  </el-button>
</template>
<script>
import axios from "axios";
import {ElMessage} from "element-plus";

export default {
  name: "login-page",
  props: {},
  setup() {

  },
  data() {
    let host = process.env.NODE_ENV === 'development' ? "127.0.0.1:8088" : location.host
    return {
      host: host,
    }
  },
  methods: {
    getLoginUrl() {
      axios.get(`http://${this.host}/api/qqUrl`).then(res => {
        let data = res.data;
        console.log(data)
        if (data.url) {
          window.open(
              data.url,
              '_self'
          );
        } else {
          ElMessage({
            message: `获取qq登陆链接失败`,
            type: 'error',
            showClose: true
          });
        }
      }).catch(err => {
        console.log(err)
      })
    }
  },
}
</script>

<style scoped>

</style>
