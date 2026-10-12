<script setup>
const steps = [
  { title: 'Say something', text: 'Tell it about your code, your hobbies or your life choices.' },
  { title: 'Get roasted', text: 'A grumpy, gym-obsessed AI comedian answers with an insult or a dark joke - and prescribes more squats.' },
  { title: 'Earn your shame', text: 'Every roast lands in the Hall of Shame for everyone to see.' }
]
</script>

<template>
  <section id="top" class="hero">
    <div class="spotlight" aria-hidden="true"></div>
    <div class="container hero-inner">
      <div class="hero-text">
        <p class="eyebrow">Live on stage · Tonight only · Every night</p>
        <h1>Get roasted by a <span class="accent">grumpy</span> gym-junkie AI comedian.</h1>
        <p class="lead">
          Personal Advisor has a dark sense of humour, zero patience and one answer to every problem: more bench presses and squats. It roasts what you
          <em>say</em> and <em>do</em> - never who you <em>are</em>.
        </p>
        <div class="actions">
          <a href="#chat" class="btn btn-primary">Step on stage</a>
          <a href="#hall-of-shame" class="btn btn-ghost">See the Hall of Shame</a>
        </div>
      </div>

      <div class="preview" aria-hidden="true">
        <div class="preview-bubble user">I write all my code without tests.</div>
        <div class="preview-bubble bot">
          <span class="badge badge-INSULT">Insult</span>
          <p>Bold strategy. Your code is like a horror movie: nobody knows what's going to happen,
            but everyone knows it ends badly.</p>
        </div>
      </div>
    </div>

    <div class="container">
      <ol class="steps">
        <li v-for="(step, i) in steps" :key="step.title" class="step">
          <span class="step-number">0{{ i + 1 }}</span>
          <h3>{{ step.title }}</h3>
          <p>{{ step.text }}</p>
        </li>
      </ol>
    </div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  overflow: hidden;
  padding: 96px 0 40px;
}

.spotlight {
  position: absolute;
  top: -200px;
  left: 50%;
  width: 900px;
  height: 700px;
  transform: translateX(-30%);
  background: radial-gradient(ellipse at center, rgba(255, 194, 61, 0.16), transparent 65%);
  pointer-events: none;
}

.hero-inner {
  position: relative;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 56px;
  align-items: center;
}

.eyebrow {
  color: var(--spotlight);
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  margin: 0 0 16px;
}

h1 {
  font-size: clamp(3rem, 8vw, 5.2rem);
}

.accent {
  color: var(--accent);
}

.lead {
  color: var(--muted);
  font-size: 1.1rem;
  max-width: 34rem;
  margin: 24px 0 32px;
}

.lead em {
  color: var(--text);
  font-style: normal;
  font-weight: 600;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.preview {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 24px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  transform: rotate(1.5deg);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
}

.preview-bubble {
  max-width: 85%;
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 0.95rem;
}

.preview-bubble p {
  margin: 8px 0 0;
}

.preview-bubble.user {
  align-self: flex-end;
  background: var(--accent);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.preview-bubble.bot {
  align-self: flex-start;
  background: var(--surface-2);
  border-bottom-left-radius: 4px;
}

.steps {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  list-style: none;
  padding: 0;
  margin: 80px 0 0;
}

.step {
  padding: 24px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: linear-gradient(180deg, var(--surface), transparent);
}

.step-number {
  color: var(--spotlight);
  font-family: var(--font-display);
  font-size: 1.4rem;
}

.step h3 {
  font-size: 1.8rem;
  margin: 8px 0;
}

.step p {
  color: var(--muted);
  margin: 0;
  font-size: 0.95rem;
}

@media (max-width: 820px) {
  .hero {
    padding-top: 56px;
  }

  .hero-inner,
  .steps {
    grid-template-columns: 1fr;
  }

  .preview {
    transform: none;
  }

  .steps {
    margin-top: 48px;
  }
}
</style>
