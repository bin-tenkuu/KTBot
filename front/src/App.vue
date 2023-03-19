<template id="app">
  <component :is="currentView" :host="host"/>
</template>

<script>
import Index from "@/views/Index.vue";
import Admin from "@/views/Admin.vue";

export default {
    name: 'App',
    setup() {
        const routes = {
            '/': Index,
            '/admin': Admin
        }
        window.addEventListener('hashchange', () => {
            this.currentPath = window.location.hash
        })
        return {
            routes
        }
    },
    data() {
        return {
            // host: "127.0.0.1:8088",
            host: location.host,
            currentPath: window.location.hash
        }
    },
    computed: {
        currentView() {
            return this.routes[this.currentPath.slice(1) || '/'] || this.routes["/"]
        }
    }
}
</script>
