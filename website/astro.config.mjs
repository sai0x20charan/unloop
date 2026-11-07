import { defineConfig } from 'astro/config';

export default defineConfig({
  site: 'https://unloop.nsaicharan.com',
  server: { host: true, port: 4322 },
  devToolbar: { enabled: false },
});
