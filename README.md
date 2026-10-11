# TPG-2026 - Sistema de Naves, Asistentes y Misiones

Trabajo Práctico Grupal de Programación (Entrega 1). Sistema de gestión y control de naves espaciales, simulación de subsistemas de recursos, motor warp, asignación de tripulación con cálculo de haberes y ejecución coordinada de misiones.

---

## 1. Requisitos de Ejecución

Para compilar y ejecutar el proyecto se requieren las siguientes herramientas instaladas en el entorno local:

* **Java Development Kit (JDK):** Versión **17** o superior (por ejemplo, Eclipse Adoptium Temurin 17, OpenJDK 17 u Oracle JDK 17).
  * Verificar en terminal con: `java -version` y `javac -version`.
* **Apache Maven:** Versión **3.8** o superior (o utilizar el Maven embebido provisto por entornos de desarrollo como IntelliJ IDEA, Eclipse o VS Code).
  * Verificar en terminal con: `mvn -version`.
* **Git:** Versión **2.30** o superior para la clonación del repositorio y manejo de ramas/etiquetas.
* **Sistema Operativo:** Multiplataforma (probado y compatible con Windows 10/11, GNU/Linux y macOS).
* **Recursos de Hardware:** Mínimo 512 MB de memoria RAM disponible para la JVM y 100 MB de espacio en disco.

---

## 2. Comandos Maven Necesarios

El ciclo de vida del proyecto se gestiona mediante Maven a través del archivo [`pom.xml`]. Los comandos principales son:

* **Limpiar artefactos generados:**
  ```bash
  mvn clean
  ```
  Elimina el directorio `target/` y cualquier compilación anterior.

* **Compilar el código fuente principal:**
  ```bash
  mvn compile
  ```
  Compila las clases ubicadas en `src/main/java/` y genera los binarios `.class` en `target/classes/`.

* **Compilar clases y pruebas:**
  ```bash
  mvn test-compile
  ```
  Compila tanto las clases principales como las clases de prueba de `src/test/java/`.

* **Ejecutar la suite de pruebas automatizadas:**
  ```bash
  mvn test
  ```
  Ejecuta todos los tests unitarios con JUnit 5 mediante el plugin Maven Surefire, reportando resultados y cobertura en consola.

* **Empaquetar la aplicación:**
  ```bash
  mvn clean package
  ```
  Compila, ejecuta las pruebas y genera el archivo JAR en `target/tpg2026-1.0-SNAPSHOT.jar`.

---

## 3. Forma de Iniciar el Sistema

El punto de entrada principal del sistema es la clase [`app.Main`], la cual ejecuta de manera automatizada y secuencial la demostración de los escenarios requeridos por la **Ficha de Inicio**:

* **Universo / Centro de Control:** Alta de naves, unicidad de nave activa y control de rechazos.
* **Escenario A (Ejecución correcta):** Creación de naves vía `NaveFactory`, asignación de tripulantes y cálculo de haberes (`Decorator`), ejecución de las misiones `M-01`, `M-02` y `M-03`, e impresión de bitácora y recursos.
* **Escenario B (Recursos insuficientes):** Demostración de atomicidad (sin cambios parciales) y registro del motivo de rechazo en la bitácora cuando no alcanzan los recursos.
* **Escenario C (Motor Warp):** Recorrido completo del ciclo de estados del motor (`Disponible -> Preparando salto -> En warp -> Enfriamiento -> Disponible`) y registro de transiciones rechazadas.
* **Escenario D (Contrato inválido):** Demostración de robustez y preservación de invariantes ante cargas excesivas, consumos fuera de rango o argumentos negativos.

### Opción A: Ejecución desde Terminal (Línea de comandos)

1. Compilar el proyecto:
   ```bash
   mvn test-compile
   ```
2. Ejecutar la clase principal:
   * **En Windows (PowerShell / CMD):**
     ```powershell
     java -cp "target/classes;target/test-classes" app.Main
     ```
   * **En Linux / macOS (Bash / Zsh):**
     ```bash
     java -cp "target/classes:target/test-classes" app.Main
     ```

### Opción B: Ejecución desde el IDE (IntelliJ IDEA / Eclipse / VS Code)

1. Abrir el proyecto como proyecto Maven (`pom.xml`).
2. Navegar en el explorador de archivos hasta [`src/main/java/app/Main.java`].
3. Hacer clic derecho sobre el archivo o el método `main` y seleccionar **Run 'Main.main()'** (o presionar `Shift + F10` en IntelliJ).

**Resultado esperado:** Todos los escenarios deben ejecutarse e imprimir `VERIFICADO`, concluyendo con:
```text
Escenarios ejecutados sin fallos.
```

---

## 4. Forma de Ejecutar las Verificaciones

El proyecto cuenta con múltiples capas de verificación y pruebas automatizadas:

### 1. Pruebas Unitarias Estándar (JUnit 5 vía Maven)
Ejecuta la totalidad de los tests unitarios formales:
```bash
mvn test
```
* **Qué comprueba:** 46 pruebas automatizadas:
  * [`NaveFactoryTest`]: Creación por tipo y configuraciones iniciales de la ficha.
  * [`NaveTest`]: Invariantes de nave, asignación de tripulación e inmutabilidad de listas expuestas.
  * [`RecursosTest`]: Cargas, consumos atómicos, límites en rango [0, 100], umbral de mantenimiento y precondiciones.
  * [`HaberesTest`]: Cálculo acumulativo y detalle de sueldos con el patrón Decorator.

### 2. Verificador Integral de Escenarios y Componentes
Ejecuta la suite de verificación global que valida la máquina de estados, el centro de control y la salida de los escenarios:
* **En Windows:**
  ```powershell
  java -cp "target/classes;target/test-classes" pruebas.EjecutarPruebas
  ```
* **En Linux / macOS:**
  ```bash
  java -cp "target/classes:target/test-classes" pruebas.EjecutarPruebas
  ```
* **Resultado esperado:** `Resultado: 20 pasaron, 0 fallaron`.

### 3. Verificación de Inmutabilidad de la Bitácora
Comprueba la consistencia temporal, inmutabilidad y copias independientes de la bitácora:
* **En Windows:**
  ```powershell
  java -cp "target/classes;target/test-classes" modelo.asistente.VerificacionBitacora
  ```
* **En Linux / macOS:**
  ```bash
  java -cp "target/classes:target/test-classes" modelo.asistente.VerificacionBitacora
  ```
* **Resultado esperado:** `Verificación de bitácora: todos los casos pasaron.`

---

## 5. Configuración Local Necesaria

Para asegurar una correcta ejecución sin conflictos de compatibilidad:

1. **Configuración de variables de entorno:**
   * Definir la variable `JAVA_HOME` apuntando al directorio raíz del JDK 17 (ejemplo en Windows: `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot` o en Linux: `/usr/lib/jvm/java-17-openjdk`).
   * Asegurarse de que `%JAVA_HOME%\bin` (o `$JAVA_HOME/bin`) esté presente en la variable de entorno `PATH`.
   * Si se utiliza Maven desde consola independiente, agregar el directorio `bin` de Apache Maven al `PATH`. (En IntelliJ IDEA no es necesario ya que el IDE utiliza su propio Maven empaquetado).
2. **Codificación de Archivos (Encoding):**
   * El proyecto utiliza **UTF-8** como codificación estándar, configurada en el `pom.xml` mediante `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>`.
   * Asegurar que el IDE tenga configurado UTF-8 para evitar inconsistencias en caracteres especiales o tildes.
3. **Nivel de Lenguaje en el IDE:**
   * Configurar el proyecto en el IDE con **Language Level 17** y **Project SDK 17**.

---

