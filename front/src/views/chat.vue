<template id="app">
  <div v-if="ws==null">
    <el-input v-model="room.id" style="width: 20em" clearable>
      <template #prepend>房间：</template>
    </el-input>
    <br>
    <el-input v-model="role.id" style="width: 20em;" clearable>
      <template #prepend>角色：</template>
    </el-input>
    <br>
    <el-button type="primary" @click="connect">进入房间</el-button>
  </div>
  <div ref="chatLogs" id="chatLogs"></div>
  <el-divider>
    <el-icon>
      <StarFilled/>
    </el-icon>
  </el-divider>
  <div v-if="ws">
    <el-space>
            <span>

            </span>
      {{ this.role.name }}
      <br>
      <label>输入框：</label>
      <el-button
          type="primary"
          :disabled="hasmessage"
          @click="sendMessage"
      >
        {{ id ? "修改" : "发送" }}
      </el-button>
      <el-switch
          v-model="scrollDown"
          size="large"
          inline-prompt
          active-text="保持底部"
          inactive-text="自由滚动"
          @change="scroll"
      />
    </el-space>
    <quill-editor
        v-model:content="message"
        content-type="html"
        :options="editorOptions"
        placeholder="请输入内容"
        class="editor"
        @ready="editorReady"
        @update:content="updateContent"
    >
    </quill-editor>
    <br>
    <el-button type="info" @click="sendHistory" :disabled="minId<=1">20条历史消息</el-button>
    <el-button type="info" @click="clear">{{ id ? "取消" : "清空" }}</el-button>
    <el-button type="danger" @click="disconnect">离开房间</el-button>
  </div>
</template>

<script>
import {Edit, Key, Plus, StarFilled} from '@element-plus/icons-vue'
import axios from "axios";
import {ElMessage} from "element-plus";
import {ref} from "vue";
import {Quill, QuillEditor} from '@vueup/vue-quill';
import htmlEditButton from "quill-html-edit-button";
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import {UsrTiktok} from "@/formats/UsrTiktok";
import {UsrShake} from "@/formats/UsrShake";
import {Ruby} from "@/formats/ruby";

Quill.register({
  "modules/htmlEditButton": htmlEditButton,
  'blots/tiktok': UsrTiktok,
  'blots/shake': UsrShake,
  'blots/ruby': Ruby,
});
let whitelist = ['0.8em', false, '2em', '4em', '8em', '16em', '32em'];
Quill.imports['attributors/style/size'].whitelist = whitelist;

export default {
  name: 'Index-page',
  components: {Key, Edit, Plus, StarFilled, QuillEditor},
  setup() {
    return {
      /**
       * @type {HTMLDivElement}
       */
      chatLogs: ref(),
      // 使用文档碎片节点优化性能
      // chatLogs2: document.createDocumentFragment(),
    }
  },
  data() {
    let host = process.env.NODE_ENV === 'development' ? "127.0.0.1:8088" : location.host
    document.addEventListener("keyup", (e) => {
      if (e.key === "Enter") {
        if (e.ctrlKey) {
          this.sendMessage()
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
      role: {
        id: 0,
        name: "",
        color: "",
      },
      /**
       * @type {WebSocket}
       */
      ws: null,
      minId: null,
      maxId: null,
      scrollDown: true,
      /**
       * @type {[{type:string,msg:string,role:string}]}
       */
      msgs: [],
      id: null,
      message: "<p><br></p>",
      hasmessage: true,
      editorOptions: {
        modules: {
          toolbar: {
            container: [
              // 加粗 斜体 下划线 删除线
              ['bold', 'italic', 'underline', 'strike'],
              // 引用 代码块 有序、无序列表
              ['blockquote', 'code-block', {list: 'ordered'}, {list: 'bullet'}],
              // 字体大小 字体种类
              [{'size': whitelist}, {'font': []}],
              // 字体颜色、字体背景颜色
              [{color: []}, {background: []}],
              // 上标/下标
              [{script: 'sub'}, {script: 'super'}],
              // 缩进 对齐方式
              [{indent: '-1'}, {indent: '+1'}, {align: []}],
              // 清除文本格式
              ['clean'],
              // 链接、图片、视频
              ['image'],
              ['tiktok', 'shake', 'ruby'],
            ],
            handlers: {
              shake() {
                let index = this.quill.getSelection().index || 0;
                this.quill.insertText(index, " ", "api");
                this.quill.insertEmbed(index + 1, 'shake', prompt("输入 shake 的字"), "api")
                this.quill.insertText(index + 2, " ", "api");
              },
              ruby() {
                let index = this.quill.getSelection().index || 0;
                this.quill.insertText(index, " ", "api");
                this.quill.insertEmbed(index + 1, 'ruby', [
                  {
                    k: prompt("输入 本体 ", "本体"),
                    v: prompt("输入 注解 ", "注解")
                  }
                ], "api")
                this.quill.insertText(index + 2, " ", "api");
              }
            }
          },
          htmlEditButton: {
            // debug: true,
            buttonHTML: "&lt;&gt;",
            buttonTitle: "以HTML编辑",
            msg: "在此处编辑HTML，当您单击“确定”时，编辑器的内容将被替换",
            okText: "确定",
            cancelText: "取消"
          }
        }
      }
    }
  },
  methods: {
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
        let role = this.room.roles[this.role.id];
        if (role != null) {
          this.role.name = role.name
          this.role.color = role.color
        } else {
          this.role.name = `unknown-${this.role.id}`
          this.role.color = "black"
        }
        document.title = `${this.room.name} - ${this.role.id}`
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
        this.chatLogs.textContent = ""
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
      let innerHTML = `<span>&lt;${role.name}&gt;:</span>`
      element.setAttribute("style", `--color: ${role.color};`)
      switch (msg.type) {
        case "text": {
          if (msg.role === +this.role.id) {
            element.setAttribute("class", "edit")
            element.addEventListener("click", () => {
              this.editMsg(msg.id)
            })
          }
          innerHTML += "<span>" + msg.msg.replace(/\n/g, "<br/>") + "</span>"
          break
        }
        case "pic": {
          innerHTML += `<img alt='img' src='${msg.msg}'/>`
          break
        }
        case "sys": {
          innerHTML = `<i>${msg.msg}</i>`
          break
        }
        default:
          innerHTML += "<span>未知消息类型: " + msg.type + "</span>"
          break
      }
      element.innerHTML = innerHTML
    },
    scroll() {
      if (this.scrollDown) {
        window.scrollTo(0, document.documentElement.scrollHeight)
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
      this.id = null
      this.quill.setText("", 'api')
    },
    sendHistory() {
      this.send({
        type: "default",
        id: this.minId,
        role: this.role.id
      })
      this.clear()
    },
    editMsg(id) {
      this.id = id
      this.message = this.msgs[id].msg
    },
    sendMessage() {
      /**
       * @type {string}
       */
      let text = this.quill.getText(0, 3);
      if (text.startsWith(".")) {
        text = this.quill.getText();
        if (text.length > 1) {
          this.send({
            id: this.id,
            type: "text",
            msg: text,
          })
        }
      } else if (text.startsWith("/me")) {
        text = this.quill.getText(3);
        if (text.length > 1) {
          this.send({
            id: this.id,
            type: "sys",
            msg: `*${this.role.name} ${text}`,
          })
        }
      } else {
        let trim = this.message;
        if (trim.length !== 0) {
          this.send({
            id: this.id,
            type: "text",
            msg: trim,
          })
        }
      }
      this.clear()
    },
    send(json) {
      this.ws.send(JSON.stringify(json))
    },
    editorReady(quill) {
      this.quill = quill
      document.querySelector('.ql-tiktok').innerText = "tiktok";
      document.querySelector('.ql-shake').innerText = "shake";
      document.querySelector('.ql-ruby').innerText = "ruby";
    },
    updateContent() {
      this.hasmessage = this.quill.getLength() < 2
    },
  }
}
</script>
<!--suppress CssUnusedSymbol -->
<style>
html, body {
  --color: black;
  height: 99%;
  width: 100%;
  margin: 0;
  padding: 0;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
}

p {
  margin: 0;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: left;
  color: #2c3e50;
  width: 100%;
}

#chatLogs > div:empty {
  display: none;
}

#chatLogs > div {
  color: var(--color);
  padding: 0;
  display: flex;
  margin: 5px;
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

#chatLogs > div > :nth-child(1) {
  font-weight: bold;
  margin-right: 10px;
}

#chatLogs > div > :nth-child(2) {
  white-space: normal;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

img {
  max-width: 70%;
  max-height: 70%;
  vertical-align: top;
}

.ql-snow button.ql-tiktok {
  min-width: 50px;
  border: 1px solid #ccc !important;
  border-radius: 5px;
}

.ql-snow button.ql-shake {
  min-width: 50px;
  border: 1px solid #ccc !important;
  border-radius: 5px;
}

.ql-snow button.ql-ruby {
  min-width: 50px;
  border: 1px solid #ccc !important;
  border-radius: 5px;
}

.ql-container {
  font-size: 1em;
}

.ql-snow button.ql-picker.ql-size {
  width: 70px;
}

.ql-snow .ql-picker.ql-size .ql-picker-label[data-value='0.8em']::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value='0.8em']::before {
  content: '0.8em';
}

.ql-snow .ql-picker.ql-size .ql-picker-label[data-value='2em']::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value='2em']::before {
  content: '2em';
}

.ql-snow .ql-picker.ql-size .ql-picker-label[data-value='4em']::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value='4em']::before {
  content: '4em';
}

.ql-snow .ql-picker.ql-size .ql-picker-label[data-value='8em']::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value='8em']::before {
  content: '8em';
}

.ql-snow .ql-picker.ql-size .ql-picker-label[data-value='16em']::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value='16em']::before {
  content: '16em';
}

.ql-snow .ql-picker.ql-size .ql-picker-label[data-value='32em']::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value='32em']::before {
  content: '32em';
}

</style>
