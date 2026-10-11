<template>
  <header class="header">
    <div class="container header-inner">
      <a href="#top" class="logo">
        <span class="on-air" aria-hidden="true"></span>
        Insultbot
      </a>
      <nav class="nav">
        <a href="#chat">Chat</a>
        <a href="#hall-of-shame">Hall of Shame</a>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(20, 17, 15, 0.85);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-display);
  font-size: 1.7rem;
  letter-spacing: 0.04em;
  text-decoration: none;
}

.on-air {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 12px var(--accent);
  animation: pulse 2s ease-in-out infinite;
}

.nav {
  display: flex;
  gap: 24px;
}

.nav a {
  color: var(--muted);
  text-decoration: none;
  font-weight: 500;
  font-size: 0.95rem;
}

.nav a:hover {
  color: var(--text);
}

@keyframes pulse {
  50% {
    opacity: 0.35;
  }
}

@media (max-width: 480px) {
  .nav {
    gap: 16px;
  }
}
</style>
