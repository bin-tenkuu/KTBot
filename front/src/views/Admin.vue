<template id="app">
  <el-select v-model="room.name" class="m-2" placeholder="请选择房间"
      :loading="room.loading"
      :remote-method="remoteMethod"
      filterable reserve-keyword
      remote remote-show-suffix>
    <el-option
        v-for="item in room.rooms"
        :key="item"
        :label="item"
        :value="item"
    >
    </el-option>
  </el-select>
</template>

<script>
import {Edit, Plus, StarFilled} from '@element-plus/icons-vue'

export default {
    name: 'Admin-page',
    props: {
        host: String
    },
    setup() {
    },
    data() {
        return {
            room: {
                rooms: [],
                loading: true,
                name: ""
            },
        }
    },
    methods: {
        remoteMethod(query) {
            console.log("query=", query)
            this.loading = true
            fetch(`http://${this.host}/api/rooms?name=${query}`)
                .then(res => res.json())
                .then(res => {
                    this.loading = false
                    this.rooms = res
                })
        },
    },
    components: {
        Edit,
        Plus,
        StarFilled
    }
}
</script>
<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
<!--suppress CssUnusedSymbol -->
<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: left;
  color: #2c3e50;
  margin: 5px;
}

img {
  vertical-align: top;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.edit-button {
  /*position: absolute;*/
}
</style>
