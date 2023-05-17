<template id="app">
    <div ref="main" class="el-main">
        <div v-if="ws==null">
            <el-input v-model="room.id" style="width: 20em" clearable>
                <template #prepend>房间：</template>
            </el-input>
            <br>
            <el-input v-model="role" style="width: 20em;" clearable>
                <template #prepend>角色：</template>
            </el-input>
            <br>
            <el-button type="primary" @click="connect">进入房间</el-button>
        </div>
        <div ref="editBtn" style="display: none">
            <el-icon>
                <Edit/>
            </el-icon>
        </div>
        <div ref="chatLogs" id="chatLogs"></div>
        <el-divider>
            <el-icon>
                <StarFilled/>
            </el-icon>
        </el-divider>
    </div>
    <div class="el-footer">
        <div v-if="ws">
            <el-space>
                <template v-for="(role,index) in getRole(role)" :key="index">
                    {{ role?.name }}
                    <el-tag
                            v-for="(tag,index) in role?.tags??[]" :key="index"
                            :type="tag.type??''"
                            :color="tag.color??''"
                            size="large"
                            effect="light">
                        {{ tag.name }}
                    </el-tag>
                </template>
                <br>
                <label>输入框：</label>
                <el-button
                        type="primary"
                        :disabled="message.length<1"
                        @click="sendMessage"
                >
                    {{ id ? "修改" : "发送" }}
                </el-button>
                <el-button type="primary" @click="sendBase64Image">发送图片</el-button>
                <el-switch
                        v-model="scrollDown"
                        size="large"
                        inline-prompt
                        active-text="保持底部"
                        inactive-text="自由滚动"
                        @change="scroll"
                />
            </el-space>
            <el-input ref="textarea" type="textarea" class="el-textarea" placeholder="请输入内容"
                      v-model="message"
                      :autosize="{ minRows: 2, maxRows: 10 }"
            />
            <br>
            <el-button type="info" @click="sendHistory" :disabled="minId<=1">20条历史消息</el-button>
            <el-button type="info" @click="clear">{{ id ? "取消" : "清空" }}</el-button>
            <el-button type="danger" @click="disconnect">离开房间</el-button>
            <el-upload
                    ref="picture"
                    class="avatar-uploader"
                    accept="image"
                    list-type="picture"
                    :show-file-list="false"
                    :auto-upload="false"
                    action="#"
                    :on-change="handlePictureChange">
                <img v-if="image" :src="image.url" class="avatar" alt="img"/>
                <el-icon v-else class="avatar-uploader-icon">
                    <Plus/>
                </el-icon>
            </el-upload>
        </div>
    </div>
</template>

<script>
import {Edit, Key, Plus, StarFilled} from '@element-plus/icons-vue'
import axios from "axios";
import {ElMessage} from "element-plus";
import {ref} from "vue";

export default {
    name: 'Index-page',
    setup() {
        return {
            textarea: ref(),
            picture: ref(),
            /**
             * @type {HTMLDivElement}
             */
            editBtn: ref(),
            /**
             * @type {HTMLDivElement}
             */
            chatLogs: ref(),
            /**
             * @type {HTMLDivElement}
             */
            main: ref()
        }
    },
    data() {
        let host = process.env.NODE_ENV === 'development' ? "127.0.0.1:8088" : location.host
        document.addEventListener("keyup", (e) => {
            if (e.key === "Enter") {
                if (e.ctrlKey) {
                    if (e.altKey) {
                        this.sendBase64Image()
                    } else {
                        this.sendMessage()
                    }
                }
                e.preventDefault()
            }
        })
        return {
            host: host,
            edit: {
                inputVisible: false,
                inputValue: "",
            },
            room: {
                id: "default",
                name: "default",
                /**
                 * @type {Record<string, {id: string, name: string, color: string}>}
                 */
                roles: {},
            },
            /**
             * @type {WebSocket}
             */
            ws: null,
            role: 0,
            minId: null,
            maxId: null,
            scrollDown: true,
            /**
             * @type {[{type:string,msg:string,role:string}]}
             */
            msgs: [],
            id: null,
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
            axios.get(`http://${this.host}/api/room`, {
                params: {
                    id: this.room.id,
                }
            }).then((res) => {
                this.room = res.data
                document.title = `${this.room.name} - ${this.role}`
            }).catch(() => {
                ElMessage({
                    message: `获取房间信息失败`,
                    type: 'error',
                    showClose: true
                });
            })
            const ws = this.ws = new WebSocket(`ws://${this.host}/ws/${this.room.id}`);
            ws.onopen = () => {
                ElMessage({
                    message: `连接成功`,
                    type: 'success',
                    duration: 1000,
                });
                this.chatLogs.innerHTML = ""
                this.msgs = []
                this.minId = null
                this.maxId = null
                this.sendHistory()
            }
            /**
             * @param ev {WebSocket.CloseEvent}
             */
            ws.onclose = (ev) => {
                if (ev.code === 1000) {
                    ElMessage({
                        message: `断开连接`,
                        duration: 1000,
                    });
                } else {
                    ElMessage({
                        message: `断开连接(${ev.code}):${ev.reason}`,
                        type: 'error',
                        showClose: true
                    });
                }
                this.disconnect()
            }
            /**
             * @param ev {WebSocket.ErrorEvent}
             */
            ws.onerror = (ev) => {
                ElMessage({
                    message: `连接出错:${ev.message}`,
                    type: 'error',
                    showClose: true
                });
                this.disconnect()
            }
            ws.onmessage = (ev) => {
                const json = JSON.parse(ev.data);
                if (json.type === 'roles') {
                    this.room.roles = json["roles"];
                } else {
                    this.setMsg(json)
                }
            }
        },
        setMsg(json) {
            if (json.type === 'msgs') {
                try {
                    for (const msg of Array.from(json.msgs)) {
                        this.setMsg(msg)
                    }
                } catch (e) {
                    console.error(json, e)
                }
            } else {
                if (this.maxId < json.id) {
                    for (let i = this.maxId; i <= json.id; i++) {
                        this.chatLogs.appendChild(document.createElement("div"))
                    }
                    this.scroll()
                    this.maxId = json.id
                }
                if (this.minId == null || this.minId > json.id) {
                    this.minId = json.id
                }
                let element = this.chatLogs.children[json.id];
                this.setInnerMsg(element, json)
                this.msgs[json.id] = json
            }
        },
        /**
         *
         * @param element {HTMLDivElement}
         * @param msg
         */
        setInnerMsg(element, msg) {
            const role = this.room.roles[msg.role]
            let innerHTML = `&lt;${role.name}&gt;: &nbsp;`
            element.setAttribute("style", `--color: ${role.color};`)
            switch (msg.type) {
                case "text": {
                    if (msg.role === +this.role) {
                        /**
                         * @type {HTMLButtonElement}
                         */
                        let editBtn = this.editBtn.firstChild.cloneNode(true);
                        editBtn.setAttribute("class", "el-icon")
                        element.appendChild(editBtn)
                        element.setAttribute("class", "edit")
                        element.addEventListener("click", () => {
                            this.editMsg(msg.id)
                        })
                    }
                    innerHTML += msg.msg.replace(/\n/g, "<br/>")
                    break
                }
                case "pic": {
                    innerHTML += `<img alt="img" src="${msg.msg}"/>`
                    break
                }
                case "sys": {
                    innerHTML = `<i>${msg.msg}</i>`
                    break
                }
                default:
                    innerHTML += "未知消息类型: " + msg.type
                    break
            }
            element.innerHTML = innerHTML
        },
        scroll() {
            if (this.scrollDown) {
                this.main.scrollTop = this.main.scrollHeight
            }
        },
        disconnect() {
            if (this.ws == null) {
                return
            }
            this.ws.close()
            this.ws = null
            this.role = ''
        },
        clear() {
            this.id = null
            this.message = "";
            this.image = null
        },
        sendHistory() {
            this.send({
                type: "default",
                id: this.minId,
                role: this.role
            })
        },
        editMsg(id) {
            this.id = id
            this.message = this.msgs[id].msg
        },
        sendMessage() {
            let trim = this.message.trim();
            if (trim.length !== 0) {
                this.send({
                    id: this.id,
                    type: "text",
                    msg: trim,
                })
                this.message = ""
            }
            this.id = null
            this.textarea?.focus()
        },
        sendBase64Image() {
            if (this.image != null) {
                let reader = new FileReader();
                reader.onloadend = () => {
                    this.send({
                        type: "pic",
                        msg: reader.result,
                    })
                    this.image = null
                };
                reader.readAsDataURL(this.image.raw);
            } else {
                console.log(this.picture);
            }
        },
        send(json) {
            this.ws.send(JSON.stringify(json))
        },
        getRole(roleId) {
            let role = this.room.roles[roleId];
            return role ? [role] : []
        },
    },
    components: {
        Key,
        Edit,
        Plus,
        StarFilled
    }
}
</script>
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

#chatLogs > div:empty {
    display: none;
}

#chatLogs > div {
    color: var(--color);
    padding: 0.3em 0 0.5em 2em;
    text-indent: -2em;
}

#chatLogs > div > * {
    text-indent: 0;
}

#chatLogs > div.edit:hover {
    border: 1px solid #a0cfff;
    cursor: pointer;
}

#chatLogs > div.edit {
    border: 0;
    outline: 0;
}

#chatLogs > div:hover > .el-icon {
    display: inline;
    color: #a0cfff;
}

#chatLogs > div > .el-icon {
    display: none;
}

img {
    /*width: 10%;
    height: 10%;*/
    max-width: 70%;
    max-height: 70%;
    vertical-align: top;
}

.avatar-uploader .avatar {
    width: 170px;
    height: 85px;
    display: block;
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
    width: 170px;
    height: 85px;
    text-align: center;
}

.el-main {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 210px;
    overflow-y: scroll;
}

.el-footer {
    position: absolute;
    height: 210px;
    width: 100%;
    bottom: 0;
    overflow-y: scroll;
}
</style>
