<template id="app">
  <div v-if="ws==null">
    <el-input v-model="room.name" style="width: 20em" clearable>
      <template #prepend>房间：</template>
    </el-input>
    <br>
    <el-input v-model="room.role" style="width: 20em;" clearable>
      <template #prepend>角色：</template>
    </el-input>
    <br>
    <el-button type="primary" @click="connect">进入房间</el-button>
  </div>
  <template v-else>
    <label>输入框：</label><br>
    <el-input type="textarea" class="el-textarea" placeholder="请输入内容" v-model="message"/>
    <br>
    <el-button type="primary" :disabled="message.length<1" @click="sendMessage">
      发送
    </el-button>
    <el-button type="info" @click="clear"><i class="el-icon-delete"></i>清空</el-button>
    <el-button type="danger" @click="disconnect">离开房间</el-button>
  </template>
</template>

<script>
import {Edit, Plus, StarFilled} from '@element-plus/icons-vue'

export default {
    name: 'Admin-page',
    data() {
        return {
            host: "127.0.0.1:80",
            edit: {
                inputVisible: false,
                inputValue: "",
            },
            room: {
                name: "a",
                role: "a",
            },
            /**
             * @type {WebSocket}
             */
            ws: null,
            roles: {
                a: [{
                    key: "a",
                }],
                b: [{
                    key: "b",
                    type: ""
                }, {
                    key: "b",
                    type: "success",
                }],
            },
            /**
             * @type {[{type:string,msg:string,role:string}]}
             */
            msgs: [],
            message: "",
            /**
             * @type {UploadFile}
             */
            image: null,
        }
    },
    methods: {
        /**
         * @param uploadFile {UploadFile}
         */
        handlePictureChange(uploadFile) {
            if (uploadFile.raw.type.startsWith("image")) {
                this.image = uploadFile
            }
        },
        connect() {
            if (this.ws != null) {
                return
            }
            const ws = this.ws = new WebSocket(`ws://${this.host}/ws/${this.room.name}`);
            ws.onopen = () => {
                this.append("------连接成功-----")
                this.send({
                    type: "role",
                    role: this.room.role
                })
            }
            /**
             * @param ev {WebSocket.CloseEvent}
             */
            ws.onclose = (ev) => {
                this.msgs.push({
                    type: "text",
                    msg: `------断开连接(${ev.code}):${ev.reason}-----`,
                    role: "system"
                })
                this.disconnect()
            }
            /**
             * @param ev {WebSocket.ErrorEvent}
             */
            ws.onerror = (ev) => {
                this.msgs.push({
                    type: "text",
                    msg: `------连接出错:${ev.message}-----`,
                    role: "system"
                })
                this.disconnect()
            }
            ws.onmessage = (ev) => {
                const json = JSON.parse(ev.data);
                if (json.type === 'roles') {
                    this.roles = json["roles"];
                } else {
                    this.append(json)
                }
            }
        },
        disconnect() {
            if (this.ws == null) {
                return
            }
            this.ws.close()
            this.ws = null
        },
        clear() {
            this.message = "";
            this.image = null
        },
        sendMessage() {
            this.send({
                type: "text",
                msg: this.message,
            })
            this.message = ""
        },
        send(json) {
            console.log(json)
            this.ws.send(JSON.stringify(json))
        },
        append(msg) {
            this.msgs[msg.id] = msg
        },
        getRole(roleId) {
            return this.roles[roleId]
        },
        sendBase64Image() {
            let reader = new FileReader();
            reader.onloadend = () => {
                this.send({
                    type: "pic",
                    msg: reader.result,
                })
                this.image = null
            };
            reader.readAsDataURL(this.image.raw);
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
