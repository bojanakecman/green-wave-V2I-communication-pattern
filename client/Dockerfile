FROM node:current-slim

WORKDIR /client

COPY . /client

RUN npm install -g @angular/cli

RUN npm install

CMD ["ng", "serve", "--host", "0.0.0.0"]

EXPOSE 4200
