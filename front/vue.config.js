const {defineConfig} = require('@vue/cli-service')
const CompressionWebpackPlugin = require('compression-webpack-plugin');
const AutoImport = require('unplugin-auto-import/webpack')
const Components = require('unplugin-vue-components/webpack')
const {ElementPlusResolver} = require('unplugin-vue-components/resolvers')

module.exports = defineConfig({
    pages: {
        index: {
            entry: 'src/index.js',
            template: 'public/index.html',
            filename: 'index.html',
            title: '首页 - 云聊天室',
        },
        chat: {
            entry: 'src/views/chat.js',
            template: 'public/index.html',
            filename: 'chat.html',
            title: '聊天室 - 云聊天室',
        },
        admin: {
            entry: 'src/views/admin.js',
            template: 'public/index.html',
            filename: 'admin.html',
            title: '管理后台 - 云聊天室',
        },
        login: {
            entry: 'src/views/login.js',
            template: 'public/index.html',
            filename: 'login.html',
            title: '登录 - 云聊天室',
        },
    },
    transpileDependencies: true,
    productionSourceMap: process.env.NODE_ENV !== 'production',
    configureWebpack: {
        performance: {
            hints: false
        },
        plugins: [
            AutoImport({
                resolvers: [ElementPlusResolver()],
            }),
            Components({
                resolvers: [ElementPlusResolver()],
            }),
            new CompressionWebpackPlugin({
                algorithm: 'gzip',
                test: /\.js$|\.html$|\.css$/,
                minRatio: 1, // 压缩率小于1才会压缩
                threshold: 10240 >> 1, // 对超过5k的数据压缩
                deleteOriginalAssets: false,
            }),
        ],
    },

})
