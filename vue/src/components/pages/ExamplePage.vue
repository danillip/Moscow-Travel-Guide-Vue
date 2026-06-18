<template>
  <main class="c-example-page">
    <RouterLink class="c-example-page__link" :to="{ name: $routes.INDEX }">
      Вернуться на стартовую страницу
    </RouterLink>
  </main>
</template>

<script>
export default {
  name: 'ExamplePage'
}
</script>

<style lang="scss">
.c-example-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #080b0f;

  &__link {
    color: #ffffff;
  }
}
</style>
