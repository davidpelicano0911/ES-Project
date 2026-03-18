FROM node:20-slim AS build
WORKDIR /app

# Accept build arguments for Vite environment variables
ARG VITE_BACKEND_HOST
ARG VITE_BACKEND_PORT
ARG VITE_KEYCLOAK_URL
ARG VITE_FLAGSMITH_ENV_ID
ARG VITE_API_VERSION
ARG VITE_MACHINE_URL

# Set them as environment variables for the build
ENV VITE_BACKEND_HOST=$VITE_BACKEND_HOST
ENV VITE_BACKEND_PORT=$VITE_BACKEND_PORT
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
ENV VITE_FLAGSMITH_ENV_ID=$VITE_FLAGSMITH_ENV_ID
ENV VITE_API_VERSION=$VITE_API_VERSION
ENV VITE_MACHINE_URL=$VITE_MACHINE_URL

RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]