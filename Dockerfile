# Используйте образ Arch Linux с Kotlin Compiler в качестве базового образа
FROM archlinux:latest

# Установка пакетов
RUN pacman -Syu --noconfirm && pacman -S --noconfirm --needed jdk8-openjdk kotlin

# # Используйте образ с предустановленными JDK и Kotlin
# FROM adoptopenjdk/openjdk8:latest

ENV DOMAIN=$DOMAIN

# Установка рабочей директории внутри контейнера
WORKDIR /app

# Копирование всех файлов проекта внутрь контейнера
COPY . /app

# Сборка исходного кода сервера
RUN kotlinc server/src/main/java/com/example/server/Server.kt -include-runtime -d server.jar

# Определение команды для запуска сервера при запуске контейнера
CMD java -jar server.jar
