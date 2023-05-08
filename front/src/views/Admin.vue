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
        <el-table-column prop="name" label="name" style="min-width: 100px">
            <template #default="{ row }">
                <span v-bind:style="{ color: row.color }">{{ row.name }}</span>
            </template>
        </el-table-column>
        <el-table-column label="color">
            <template #default="{ row }">
                <el-color-picker v-model="row.color" show-alpha></el-color-picker>
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
</template>

<script>
import axios from "axios";
import {ElNotification} from "element-plus";

export default {
    name: 'Admin-page',
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
                 * @type {Record<string, {id: string, name: string, color: string}>}
                 */
                roles: {},
            },
            roleDialog: {
                visible: false,
                id: "",
                name: "",
            },
            dialogVisible: false,
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
                color: "",
            }
            console.log(this.room.roles[id])
            roleDialog.visible = false
        },
        deleteRole(id) {
            delete this.room.roles[id]
        },
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
