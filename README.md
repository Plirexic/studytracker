# StudyTracker

## Overview

**Version:** 0.1

Development of a Spring + Vue Full-Stack Application, tailored for students, who want to manage their tasks and have an insight into statistical evaluation and progress visualization.
Backend technology: Java, Spring Boot, Spring Data JPA, Hibernate, Microsoft SQL Server, Maven
Frontend technology: Vue.js, Vite, Pinia, Axios, HTML, CSS, Javascript

## Installation

### Requirements

**JDK Version:** 17 oder higher
**Apache Maven:** Version 3.6 oder higher
**Node.js:** Version 18.x oder higher
**npm:** Version 9.x or higher OR **yarn:** Version 1.x
**SQL Server and Database:** Microsoft SQL Server provided by Esslingen University has been used for development, you can use a server of your choice

### Setup

**Database:** If you are using your own SQL Server you will have to create tables according to the Entity classes in the backend, and include database logic. Templates for database logic and functionality can be found in `studybackend/src/main/resources/db`.

**Backend:** For building and starting the backend you will need the following commands

```bash
mvn clean install
mvn spring-boot:run
```

**Frontend:** Make sure to install all required packages. The Base URL should be `localhost:8080/api.`

```bash
npm install
OR
yarn install
npm run dev
```

### **Test the application**

Starting the application, you should be redirected to the login page. As there is no registration function for now, you will have to insert users into the database. After successfully logging in, you should now see the main dashboard and are free to test the application.
