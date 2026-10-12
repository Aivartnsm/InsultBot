<script setup>
import { nextTick, ref } from 'vue'
import { ApiError, fetchRandomGym, sendMessage } from '../api'
import { JOKE_TYPE_LABELS } from '../jokeTypes'

const MAX_LENGTH = 255 // sama piir mis RegexJailbreakAdvisor.MAX_MESSAGE_LENGTH

const emit = defineEmits(['joke-saved'])

// Iga 3-6 kasutaja sõnumi järel lisatakse vastusele "Prescription: gym" kaart
const GYM_EVERY_MIN = 3
const GYM_EVERY_MAX = 6

function randomGymInterval() {
  return GYM_EVERY_MIN + Math.floor(Math.random() * (GYM_EVERY_MAX - GYM_EVERY_MIN + 1))
}

let promptsSinceGym = 0
let nextGymAt = randomGymInterval()

const suggestions = ['Roast my code', 'Tell me a dark joke', 'I use tabs instead of spaces']

let nextId = 1
const messages = ref([
  {
    id: nextId++,
    from: 'bot',
    text: "Oh great, another one. Go on then, say something so I can make fun of it."
  }
])
const input = ref('')
const loading = ref(false)
const messageList = ref(null)

function addMessage(message) {
  messages.value.push({ id: nextId++, ...message })
  scrollToBottom()
}

async function scrollToBottom() {
  await nextTick()
  const el = messageList.value
  if (el) el.scrollTop = el.scrollHeight
}

function errorText(error) {
  if (error instanceof ApiError && error.guardrail) {
    const rule = error.guardrail.split(':').pop()
    // judge-error / judge-empty: turvakohtunik ise ei vastanud (nt vale API võti), kasutaja pole süüdi
    if (rule.startsWith('judge-')) {
      return "The safety judge fell asleep backstage, so I can't let anything through. Try again in a moment."
    }
    return `Nice try. The bouncer threw that message out (${rule}).`
  }
  if (error instanceof ApiError && error.status === 429) {
    return "Slow down, you're out of breath. Do a set of squats and try again in a minute."
  }
  if (error instanceof ApiError && error.status === 400) {
    return "I can't roast nothing. Type something first."
  }
  return 'The comedian tripped over the mic cable. Is the backend running? Try again.'
}

async function send(text = input.value) {
  const message = text.trim()
  if (!message || loading.value) return

  addMessage({ from: 'user', text: message })
  input.value = ''
  loading.value = true
  scrollToBottom()

  promptsSinceGym++

  try {
    const joke = await sendMessage(message)
    const gym = await gymPrescription(joke.type)
    addMessage({ from: 'bot', type: joke.type, text: joke.joke, gifUrl: joke.gifUrl, gym })
    emit('joke-saved', joke)
  } catch (error) {
    addMessage({ from: 'system', text: errorText(error) })
  } finally {
    loading.value = false
  }
}

// SUPPORT vastusele (päris mures kasutaja) jõusaali ei soovitata - kaart lükkub järgmisele vastusele
async function gymPrescription(type) {
  if (type === 'SUPPORT' || promptsSinceGym < nextGymAt) return null
  try {
    const gym = await fetchRandomGym()
    if (!gym) return null
    promptsSinceGym = 0
    nextGymAt = randomGymInterval()
    return gym
  } catch {
    return null // jõusaal on boonus, nali näidatakse ka ilma selleta
  }
}

function onKeydown(event) {
  // Enter saadab, Shift+Enter teeb uue rea
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    send()
  }
}
</script>

<template>
  <section id="chat" class="section">
    <div class="container">
      <h2 class="section-title">Step on stage</h2>
      <p class="section-lead">Say anything. Regret it immediately.</p>

      <div class="chat">
        <div class="chat-header">
          <div class="avatar" aria-hidden="true">
            <svg viewBox="0 0 64 64" width="22" height="22">
              <rect x="24" y="10" width="16" height="28" rx="8" fill="var(--accent)" />
              <path d="M18 30a14 14 0 0 0 28 0" fill="none" stroke="var(--text)" stroke-width="4" stroke-linecap="round" />
              <path d="M32 44v10M24 54h16" stroke="var(--text)" stroke-width="4" stroke-linecap="round" />
            </svg>
          </div>
          <div>
            <div class="chat-name">Personal Advisor</div>
            <div class="chat-status">
              <span class="status-dot" aria-hidden="true"></span>
              {{ loading ? 'thinking of something mean…' : 'live on stage' }}
            </div>
          </div>
        </div>

        <div ref="messageList" class="messages" aria-live="polite">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message"
            :class="[message.from, message.type && `type-${message.type}`]"
          >
            <span v-if="message.type" class="badge" :class="`badge-${message.type}`">
              {{ JOKE_TYPE_LABELS[message.type] ?? message.type }}
            </span>
            <p>{{ message.text }}</p>
            <img
              v-if="message.gifUrl"
              :src="message.gifUrl"
              class="gif"
              alt="Personal Advisor's reaction GIF"
              @load="scrollToBottom"
            />
            <a
              v-if="message.gym"
              :href="message.gym.mapsUrl"
              class="gym-card"
              target="_blank"
              rel="noopener noreferrer"
            >
              <span class="gym-label">🏋️ Prescription: gym</span>
              <strong>{{ message.gym.name }}</strong>
              <span v-if="message.gym.address" class="gym-address">{{ message.gym.address }}</span>
            </a>
          </div>

          <div v-if="loading" class="message bot typing" aria-label="Personal Advisor is typing">
            <span></span><span></span><span></span>
          </div>
        </div>

        <div class="suggestions">
          <button
            v-for="suggestion in suggestions"
            :key="suggestion"
            type="button"
            class="chip"
            :disabled="loading"
            @click="send(suggestion)"
          >
            {{ suggestion }}
          </button>
        </div>

        <form class="composer" @submit.prevent="send()">
          <label for="chat-input" class="visually-hidden">Your message</label>
          <textarea
            id="chat-input"
            v-model="input"
            rows="1"
            :maxlength="MAX_LENGTH"
            placeholder="Say something you'll regret…"
            :disabled="loading"
            @keydown="onKeydown"
          ></textarea>
          <button type="submit" class="btn btn-primary send" :disabled="loading || !input.trim()">
            Roast me
          </button>
        </form>
        <div class="chat-footer">
          <span class="giphy">Powered by GIPHY</span>
          <span class="counter" :class="{ warn: input.length > MAX_LENGTH * 0.9 }">
            {{ input.length }} / {{ MAX_LENGTH }}
          </span>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.chat {
  max-width: 760px;
  margin: 0 auto;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}

.avatar {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: var(--surface-2);
}

.chat-name {
  font-weight: 600;
}

.chat-status {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--muted);
  font-size: 0.85rem;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--support);
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 460px;
  overflow-y: auto;
  padding: 20px;
}

.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 14px;
  animation: pop-in 0.2s ease-out;
}

.message p {
  margin: 0;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.message .badge {
  margin-bottom: 8px;
}

.message.user {
  align-self: flex-end;
  background: var(--accent);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message.bot {
  align-self: flex-start;
  background: var(--surface-2);
  border-bottom-left-radius: 4px;
}

.message.bot.type-SUPPORT {
  border: 1px solid var(--support);
}

.message.system {
  align-self: center;
  max-width: 90%;
  background: transparent;
  border: 1px dashed var(--border);
  color: var(--muted);
  font-size: 0.9rem;
  text-align: center;
}

.typing {
  display: flex;
  gap: 5px;
  padding: 16px 18px;
}

.typing span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--muted);
  animation: bounce 1.2s infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing span:nth-child(3) {
  animation-delay: 0.3s;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 20px 12px;
}

.chip {
  background: transparent;
  color: var(--muted);
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 0.85rem;
}

.chip:hover:not(:disabled) {
  color: var(--text);
  border-color: var(--muted);
}

.composer {
  display: flex;
  gap: 10px;
  padding: 12px 20px 4px;
  border-top: 1px solid var(--border);
}

textarea {
  flex: 1;
  min-height: 48px;
  max-height: 140px;
  resize: vertical;
  padding: 13px 16px;
  background: var(--bg);
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: 12px;
  font: inherit;
}

textarea:focus {
  outline: none;
  border-color: var(--spotlight);
}

.send {
  align-self: flex-end;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.gif {
  display: block;
  max-width: 100%;
  height: 160px;
  margin-top: 10px;
  border-radius: 10px;
  object-fit: cover;
  background: var(--bg);
}

.gym-card {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 10px;
  padding: 10px 14px;
  background: var(--bg);
  border: 1px dashed var(--accent);
  border-radius: 10px;
  color: var(--text);
  text-decoration: none;
}

.gym-card:hover {
  border-style: solid;
}

.gym-label {
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--accent);
}

.gym-address {
  font-size: 0.85rem;
  color: var(--muted);
}

.chat-footer {
  display: flex;
  justify-content: space-between;
  padding: 0 20px 12px;
  font-size: 0.75rem;
  color: var(--muted);
}

.giphy {
  letter-spacing: 0.04em;
}

.counter.warn {
  color: var(--accent);
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

@keyframes pop-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  40% {
    transform: translateY(-5px);
    opacity: 1;
  }
}

@media (max-width: 560px) {
  .messages {
    height: 60vh;
    padding: 16px;
  }

  .message {
    max-width: 90%;
  }

  .composer {
    flex-direction: column;
  }

  .send {
    align-self: stretch;
    justify-content: center;
  }
}
</style>
