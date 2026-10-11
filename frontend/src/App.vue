<script setup>
import { ref } from 'vue'
import AppHeader from './components/AppHeader.vue'
import HeroSection from './components/HeroSection.vue'
import ChatWindow from './components/ChatWindow.vue'
import HallOfShame from './components/HallOfShame.vue'
import AppFooter from './components/AppFooter.vue'

const hallOfShame = ref(null)

// Uus nali salvestati backendis -> värskenda Hall of Shame nimekirja
function onJokeSaved() {
  hallOfShame.value?.load()
}
</script>

<template>
  <AppHeader />
  <main>
    <HeroSection />
    <ChatWindow @joke-saved="onJokeSaved" />
    <HallOfShame ref="hallOfShame" />
  </main>
  <AppFooter />
</template>
