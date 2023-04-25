<template id="app">
    <div class="el-main">
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
        <el-table :data="msgs" table-layout="auto" stripe>
            <el-table-column label="角色">
                <template #default="{row}">
                    {{ role.name }}
                    <template v-for="(role,index) in getRole(row.role)" :key="index">
                        <template v-for="(tag,index) in role.tags" :key="index">
                            <el-tag
                                    :type="tag.type??''"
                                    :color="tag.color??''"
                                    size="large"
                                    effect="light">
                                {{ tag.name }}
                            </el-tag>
                            <br>
                        </template>
                    </template>
                </template>
            </el-table-column>
            <el-table-column prop="msg" label="消息">
                <template #default="{row}">
                    <template v-if="row.type==='text'">
                        <span v-html="row.msg"></span>
                    </template>
                    <template v-else-if="row.type==='pic'">
                        <img alt="img" :src="row.msg"/>
                    </template>
                </template>
            </el-table-column>
            <el-table-column label="操作">
                <template #default="{row}">
                    <el-button v-if="row.type==='text'||row.role===role" @click="editMsg(row.id)">
                        <el-icon>
                            <Edit/>
                        </el-icon>
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
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
                    发送 <kbd>⌘/Ctrl</kbd>+<kbd>S</kbd>
                </el-button>
                <el-button type="primary" @click="sendBase64Image">发送图片</el-button>
            </el-space>
            <el-input ref="textarea" type="textarea" class="el-textarea" placeholder="请输入内容"
                      v-model="message"
                      :autosize="{ minRows: 2, maxRows: 10 }"
            />
            <br>
            <el-button type="info" @click="sendHistory" :disabled="minId<=1">20条历史消息</el-button>
            <el-button type="info" @click="clear">清空</el-button>
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
    props: {
        host: String
    },
    setup() {
        let textarea = ref()
        let picture = ref()
        return {
            textarea,
            picture
        }
    },
    data() {
        document.addEventListener("keyup", (e) => {
            if (e.key === "Enter") {
                console.log(e)
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
            edit: {
                inputVisible: false,
                inputValue: "",
            },
            room: {
                id: "default",
                name: "default",
                /**
                 * @type {Record<string, {id: string, name: string, tags: Array<{name: string, type: string, color: string}>}>}
                 */
                roles: {},
            },
            /**
             * @type {WebSocket}
             */
            ws: null,
            role: "a",
            minId: null,
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
                this.msgs = []
                this.send({
                    type: "default",
                    id: this.minId,
                    role: this.role
                })
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
                const setMsg = (json) => {
                    if (json.type === 'msgs') {
                        for (const msg of Array.from(json.msgs)) {
                            setMsg(msg)
                        }
                    } else {
                        if (this.minId > json.id) {
                            this.minId = json.id
                        }
                        json.msg = json.msg.replace(/\n/g, "<br/>")
                        this.msgs[json.id] = json
                    }
                }
                if (json.type === 'roles') {
                    this.room.roles = json["roles"];
                } else {
                    setMsg(json)
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
<style scoped>
.avatar-uploader .avatar {
    width: 170px;
    height: 85px;
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
