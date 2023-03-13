<template id="app">
  <el-tabs v-model="activeName">
    <el-tab-pane label="创建房间" name="create">
      <el-input></el-input>
      <el-button type="success">创建房间</el-button>
    </el-tab-pane>
    <el-tab-pane label="修改房间" name="edit">
      <el-button type="success">保存房间</el-button>
    </el-tab-pane>
    <el-tab-pane label="进入房间" name="join">
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
        <div v-for="(msg,index) in msgs" :key="index">
          <template v-if="msg.type==='sysText'">
            <span>{{ msg.msg }}</span>
          </template>
          <template v-else-if="msg.type==='text'">
            <el-tag
                v-for="tag in getRole(msg.role)"
                size="large"
                :key="tag.key"
                :color="tag.color"
                effect="plain">
              {{ tag.key }}
            </el-tag>
            <span>：{{ msg.msg }}</span>
          </template>
          <template v-else-if="msg.type==='pic'">
            <el-tag
                v-for="tag in getRole(msg.role)"
                size="large"
                :key="tag.key"
                :color="tag.color"
                effect="plain">
              {{ tag.key }}
            </el-tag>
            <img alt="img" :src="msg.msg"/>
          </template>
        </div>
        <el-divider>
          <el-icon>
            <star-filled/>
          </el-icon>
        </el-divider>
        <label>输入框：</label><br>
        <el-input type="textarea" class="el-textarea" placeholder="请输入内容" v-model="message"/>
        <br>
        <el-button type="primary" :disabled="message.length<1" @click="sendMessage">发送</el-button>
        <el-button type="primary" :disabled="image==null" @click="sendBase64Image">发送图片</el-button>
        <el-button type="info" @click="clear"><i class="el-icon-delete"></i>清空</el-button>
        <el-button type="danger" @click="disconnect">离开房间</el-button>
        <el-upload
            class="avatar-uploader"
            accept="image"
            list-type="picture"
            :show-file-list="false" :auto-upload="false" action="#"
            :on-change="handlePictureChange">
          <img v-if="image" :src="image.url" class="avatar" alt="img"/>
          <el-icon v-else class="avatar-uploader-icon">
            <Plus/>
          </el-icon>
        </el-upload>
      </template>
    </el-tab-pane>
  </el-tabs>
</template>

<script>
import {Plus, StarFilled} from '@element-plus/icons-vue'

export default {
    name: 'App',
    data() {
        return {
            activeName: "join",
            host: "127.0.0.1:8081",
            room: {
                name: "test",
                role: "admin",
            },
            /**
             * @type {WebSocket}
             */
            ws: null,
            roles: {
                a: [{
                    key: "a",
                    color: ""
                }],
                b: [{
                    key: "b",
                    color: ""
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
                // ws.close()
                this.send({
                    type: "role",
                    name: this.room.role
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
                role: this.room.role,
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
                    role: this.room.role,
                })
                this.image = null
            };
            reader.readAsDataURL(this.image.raw);
        },
    },
    components: {Plus, StarFilled}
}
</script>
<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
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
</style>
