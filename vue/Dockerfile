FROM node:22

WORKDIR /vue

COPY package*.json ./

COPY ./ ./

EXPOSE 5173

ENTRYPOINT ["sh", "-c", "test -x node_modules/.bin/vite || npm install --no-audit --no-fund --fetch-retries=5 --fetch-retry-maxtimeout=120000 || npm install --registry=https://registry.npmmirror.com --no-audit --no-fund --fetch-retries=5 --fetch-retry-maxtimeout=120000; npm run devhost"]
