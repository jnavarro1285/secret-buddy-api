# -- Etapa de Compilación --
# Utiliza una imagen base de Maven para compilar el proyecto.
FROM maven:3.9-eclipse-temurin-17 AS build

# Establece el directorio de trabajo dentro del contenedor.
WORKDIR /secret-buddy-app

# Copia los archivos pom.xml del padre y los módulos para aprovechar el cache.
COPY ../pom.xml ./
COPY secret-buddy-core/pom.xml secret-buddy-core/
COPY secret-buddy-api/pom.xml secret-buddy-api/

# Descarga las dependencias del proyecto.
RUN mvn dependency:go-offline -B

# Copia el código fuente completo del proyecto.
COPY . .

# Compila y empaqueta la aplicación.
# -DskipTests para omitir los tests y acelerar la construcción.
RUN mvn clean package -DskipTests

# -- Etapa Final (Producción) --
# Utiliza una imagen JRE ligera, solo con el entorno de ejecución Java.
FROM eclipse-temurin:17-jre-alpine

# Expone el puerto por defecto de Spring Boot.
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

# Copia el archivo JAR ejecutable desde la etapa de construcción.
# La ruta es relativa al WORKDIR de la etapa de construcción.
COPY --from=build /secret-buddy-app/secret-buddy-api/target/secret-buddy-api-0.0.1-PROD.jar .

# Define el punto de entrada para ejecutar la aplicación.
ENTRYPOINT ["java", "-jar", "/secret-buddy-api-0.0.1-PROD.jar"]