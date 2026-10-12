<template>
  <footer class="footer">
    <div class="container footer-inner">
      <p class="help">
        Feeling down for real? You're not alone - call <strong>116 123</strong>,
        or <strong>112</strong> in an emergency.
      </p>
      <p class="credits">Personal Advisor · a school project built with Spring Boot, Spring AI &amp; Vue</p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  border-top: 1px solid var(--border);
  padding: 32px 0;
  color: var(--muted);
  font-size: 0.9rem;
}

.footer-inner {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px;
}

.footer p {
  margin: 0;
}

.help strong {
  color: var(--support);
}
</style>
