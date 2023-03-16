<template id="app">
  <component :is="currentView"/>
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
