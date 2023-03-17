<template id="app">
  <component :is="currentView" :host="host"/>
</template>

<script>
import Index from "@/views/Index.vue";
import Admin from "@/views/Admin.vue";

const routes = {
    '/': Index,
    '/admin': Admin
}
export default {
    name: 'App',
    setup() {
        window.addEventListener('hashchange', () => {
            this.currentPath = window.location.hash
        })
    },
    data() {
        return {
            host: "127.0.0.1:80",
            currentPath: window.location.hash
        }
    },
    computed: {
        currentView() {
            return routes[this.currentPath.slice(1) || '/'] || Index
        }
    }
}
</script>
