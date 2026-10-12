<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchJokes } from '../api'
import { JOKE_TYPE_LABELS } from '../jokeTypes'

const PAGE_SIZE = 9

const jokes = ref([])
const loading = ref(true)
const error = ref(false)
const showAll = ref(false)

// SUPPORT vastused on päris murega inimestele - neid ei näita avalikus Hall of Shame'is
const roasts = computed(() => jokes.value.filter((joke) => joke.type !== 'SUPPORT'))
const visible = computed(() => (showAll.value ? roasts.value : roasts.value.slice(0, PAGE_SIZE)))

async function load() {
  error.value = false
  try {
    jokes.value = await fetchJokes()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const relativeTime = new Intl.RelativeTimeFormat('en', { numeric: 'auto' })

function timeAgo(createdAt) {
  const seconds = Math.round((new Date(createdAt) - Date.now()) / 1000)
  const units = [
    ['day', 86400],
    ['hour', 3600],
    ['minute', 60]
  ]
  for (const [unit, size] of units) {
    if (Math.abs(seconds) >= size) return relativeTime.format(Math.round(seconds / size), unit)
  }
  return 'just now'
}

onMounted(load)

defineExpose({ load })
</script>

<template>
  <section id="hall-of-shame" class="section hall">
    <div class="container">
      <h2 class="section-title">Hall of Shame</h2>
      <p class="section-lead">Everyone who stepped on stage. Nobody walked off with their dignity.</p>

      <p v-if="loading" class="state">Loading the evidence…</p>
      <p v-else-if="error" class="state">
        Couldn't load the Hall of Shame. Is the backend running?
        <button type="button" class="btn btn-ghost retry" @click="load">Try again</button>
      </p>
      <p v-else-if="roasts.length === 0" class="state">Empty. Be the first victim.</p>

      <template v-else>
        <ul class="grid">
          <li v-for="joke in visible" :key="joke.id" class="card">
            <div class="card-top">
              <span class="badge" :class="`badge-${joke.type}`">{{ JOKE_TYPE_LABELS[joke.type] ?? joke.type }}</span>
              <time :datetime="joke.createdAt">{{ timeAgo(joke.createdAt) }}</time>
            </div>
            <p class="victim">“{{ joke.userInput }}”</p>
            <p class="roast">{{ joke.joke }}</p>
            <img v-if="joke.gifUrl" :src="joke.gifUrl" class="gif" alt="Personal Advisor's reaction GIF" loading="lazy" />
          </li>
        </ul>
        <p class="giphy">GIFs powered by GIPHY</p>
        <div v-if="roasts.length > PAGE_SIZE" class="more">
          <button type="button" class="btn btn-ghost" @click="showAll = !showAll">
            {{ showAll ? 'Show less' : `Show all ${roasts.length}` }}
          </button>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.hall {
  border-top: 1px solid var(--border);
  background: linear-gradient(180deg, var(--surface), var(--bg) 320px);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 22px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  transition: transform 0.15s, border-color 0.15s;
}

.card:hover {
  transform: translateY(-3px);
  border-color: var(--muted);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

time {
  color: var(--muted);
  font-size: 0.8rem;
}

.victim {
  margin: 0;
  color: var(--muted);
  font-style: italic;
  font-size: 0.9rem;
  overflow-wrap: anywhere;
}

.roast {
  margin: 0;
  overflow-wrap: anywhere;
}

.gif {
  width: 100%;
  height: 180px;
  margin-top: auto;
  border-radius: 10px;
  object-fit: cover;
  background: var(--bg);
}

.giphy {
  margin: 16px 0 0;
  text-align: right;
  font-size: 0.75rem;
  color: var(--muted);
  letter-spacing: 0.04em;
}

.state {
  color: var(--muted);
  text-align: center;
  padding: 40px 0;
}

.retry {
  margin-left: 12px;
  padding: 8px 18px;
}

.more {
  text-align: center;
  margin-top: 32px;
}
</style>
