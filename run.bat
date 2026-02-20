cd backend
docker build -t spring-boot-docker .
cd ..
cd  frontend
npm install  & ^
ng build -c production & ^
docker build -t angular-ui .