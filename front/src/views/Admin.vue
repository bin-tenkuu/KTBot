<template id="app">
  <el-form>
    <el-form-item label="房间Id:">
      <el-select v-model="room.id"
          allow-create
          default-first-option
          filterable
          clearable
      >
        <el-option v-for="it in rooms" :key="it.id" :label="it.id" :value="it.id" @click="getRoom(it.id)">
          <span style="float: left">{{ it.id }}</span>
          <span style="float: right;color: var(--el-text-color-secondary);font-size: 13px;">
            {{ it.name }}
          </span>
        </el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="房间名:" style="width: 25%;">
      <el-input v-model="room.name"></el-input>
    </el-form-item>
    <span class="dialog-footer">
      <el-button type="primary" @click="setRoom">保存房间</el-button>
      <el-button type="info" @click="downloadLog">导出日志</el-button>
      <el-button type="danger" @click="deleteRoom">删除房间</el-button>
    </span>
  </el-form>
  <el-table
      :data="Object.values(room.roles)"
      style="width: 100%;"
      table-layout="auto"
      border stripe>
    <el-table-column fixed prop="id" label="id" style="min-width: 50px"/>
    <el-table-column prop="name" label="name" style="min-width: 100px"/>
    <el-table-column label="tags">
      <template #default="{ row }">
        <template v-for="(tag,index) in row.tags" :key="index">
          <el-popconfirm
              title="修改还是删除"
              confirm-button-text="修改"
              cancel-button-text="删除"
              :icon="InfoFilled"
              icon-color="#626AEF"
              @confirm="setTag(row.id,index)"
              @cancel="deleteTag(row.id,index)"
          >
            <template #reference>
              <el-tag
                  :type="tag.type??''"
                  :color="tag.color??''"
                  effect="light"
                  size="large"
              >
                {{ tag.name }}
              </el-tag>
            </template>
          </el-popconfirm>
        </template>
        <el-button @click="tagDialog.id=row.id;tagDialog.visible=true">+ New Tag</el-button>
      </template>
    </el-table-column>
    <el-table-column fixed="right" label="操作" width="120">
      <template #default="{row}">
        <el-button
            type="danger"
            @click="deleteRole(row.id)">
          删除
        </el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-button style="width: 100%" @click="roleDialog.visible=true">
    Add Item
  </el-button>
  <el-dialog v-model="roleDialog.visible">
    <el-form>
      <el-form-item label="id">
        <el-input v-model="roleDialog.id"></el-input>
      </el-form-item>
      <el-form-item label="name">
        <el-input v-model="roleDialog.name"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="roleDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="addRole">确认</el-button>
      </span>
    </template>
  </el-dialog>
  <el-dialog v-model="tagDialog.visible">
    <el-form>
      <el-form-item label="预览">
        <el-tag
            :type="tagDialog.type"
            :color="tagDialog.color"
            effect="light"
            size="large"
        >
          {{ tagDialog.name }}
        </el-tag>
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="tagDialog.name"></el-input>
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="tagDialog.type">
          <el-option label="" value=""></el-option>
          <el-option label="success" value="success"></el-option>
          <el-option label="info" value="info"></el-option>
          <el-option label="warning" value="warning"></el-option>
          <el-option label="danger" value="danger"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="背景色">
        <el-color-picker v-model="tagDialog.color" show-alpha></el-color-picker>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="tagDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="addTag">确认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import axios from "axios";
import {ElNotification} from "element-plus";
import {InfoFilled} from "@element-plus/icons-vue";

export default {
    name: 'Admin-page',
    computed: {
        InfoFilled() {
            return InfoFilled
        }
    },
    props: {
        host: String
    },
    data() {
        axios.get(`http://${this.host}/api/rooms`).then(res => {
            this.rooms = res.data
        })
        return {
            /**
             * @type {Array<{id: string, name: string}>}
             */
            rooms: [],
            loading: true,
            room: {
                id: "default",
                name: "",
                /**
                 * @type {Record<string, {id: string, name: string, tags: Array<{name: string, type: string, color: string}>}>}
                 */
                roles: {},
            },
            roleDialog: {
                visible: false,
                id: "",
                name: "",
            },
            dialogVisible: false,
            tagDialog: {
                visible: false,
                id: "",
                index: null,
                name: "",
                type: "",
                color: "",
            }
        }
    },
    methods: {
        getRoom(id) {
            this.dialogVisible = false
            this.room.id = id
            axios.get(`http://${this.host}/api/room`, {
                params: {
                    id: id,
                }
            }).then(res => {
                this.room = res.data
            })
        },
        setRoom() {
            axios.post(`http://${this.host}/api/room`, this.room).then(() => {
                ElNotification({
                    title: '成功',
                    message: '保存成功',
                    type: 'success',
                    position: 'top-left',
                });
            })
        },
        downloadLog() {
            window.open(
                `http://${this.host}/api/room/logs?id=${this.room.id}`,
                '_self'
            );
        },
        deleteRoom() {
            axios.get(`http://${this.host}/api/room/del`, {
                params: {
                    id: this.room.id,
                },
            }).then(() => {
                ElNotification({
                    title: '成功',
                    message: '删除成功',
                    type: 'success',
                    position: 'top-left',
                });
            })
        },
        addRole() {
            let roleDialog = this.roleDialog;
            const {id, name} = roleDialog;
            this.room.roles[id] = {
                id: id,
                name: name,
                tags: [],
            }
            console.log(this.room.roles[id])
            roleDialog.visible = false
        },
        deleteRole(id) {
            delete this.room.roles[id]
        },
        setTag(id, index) {
            const tagDialog = this.tagDialog;
            const {name, type, color} = this.room.roles[id].tags[index];
            tagDialog.id = id;
            tagDialog.index = index;
            tagDialog.name = name;
            tagDialog.type = type;
            tagDialog.color = color;
            tagDialog.visible = true
            return true
        },
        addTag() {
            const tagDialog = this.tagDialog;
            const {type, color, name, id, index} = tagDialog;
            if (index) {
                this.room.roles[id].tags[index] = {
                    name: name,
                    type: type,
                    color: color,
                }
            } else {
                this.room.roles[id].tags.push({
                    name: name,
                    type: type,
                    color: color,
                })
            }
            tagDialog.visible = false
            tagDialog.index = null
        },
        deleteTag(id, index) {
            this.room.roles[id].tags.splice(index, 1)
            return true
        }
    }
}
</script>
<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: left;
  color: #2c3e50;
  margin: 5px;
}

</style>
