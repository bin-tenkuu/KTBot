<template>
    <el-button @click="getLoginUrl" size="large" type="primary">
        登录
    </el-button>
</template>
<script>
import axios from "axios";

export default {
    name: "login-page",
    props: {},
    setup() {

    },
    data() {
        return {
            host: process.env.NODE_ENV === 'development' ? "127.0.0.1:8088" : location.host,
        }
    },
    methods: {
        getLoginUrl() {
            axios.get(`https://uniqueker.top/connect.php`, {
                params: {
                    act: "login",
                    appid: "1637",
                    appkey: "90c563c89c08c8ac4fea54c1e8bd9c0c",
                    type: "qq",
                    redirect_uri: "v.binsrc.club"
                }
            })
        }
    },
}
</script>

<style scoped>

</style>
