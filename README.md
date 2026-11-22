# TPG 2026 — Sistema de Naves, Asistentes, Misiones y Universo Dinámico

Trabajo Práctico Grupal de Programación (Entregas 1 y 2). Software embarcado de una nave interestelar tripulada con universo dinámico, persistencia de operadores (DAO/DTO + JDBC sobre H2), concurrencia (Productor–Consumidor con monitores) e interfaz gráfica Swing en tiempo real (MVC + Observer).

---

## 1. Requisitos de Ejecución

* **Java Development Kit (JDK):** Versión **17** o superior (`java -version`, `javac -version`).
* **Apache Maven:** Versión **3.8** o superior (`mvn -version`) o Maven embebido del IDE (IntelliJ IDEA, Eclipse, VS Code).
* **Base de datos:** **H2 Database** (`com.h2database:h2:2.2.224`), declarada en [`pom.xml`](pom.xml). Al ser embebida en Java, no requiere instalar ningún servidor externo.
* **Sistema Operativo:** Windows 10/11, GNU/Linux o macOS.

---

## 2. Comandos Maven Necesarios

* **Compilar el proyecto:**
  ```bash
  mvn compile
  ```
* **Compilar código fuente y pruebas:**
  ```bash
  mvn test-compile
  ```
* **Ejecutar la suite JUnit 5 (incluye persistencia H2, nave, fábrica, recursos y haberes):**
  ```bash
  mvn test
  ```
* **Empaquetar el proyecto:**
  ```bash
  mvn clean package
  ```

---

## 3. Forma de Iniciar la Aplicación (Entrega 2 — Interfaz Gráfica)

El punto de entrada principal de la Entrega 2 es [`app.MainApp`](src/main/java/app/MainApp.java).

### Ejecución desde IDE
1. Abrir el proyecto como proyecto Maven (`pom.xml`).
2. Ejecutar `src/main/java/app/MainApp.java` (**Run 'MainApp.main()'**).

### Ejecución desde Terminal
1. Compilar el proyecto con Maven (`mvn compile`).
2. Ejecutar incluyendo el driver H2 en el classpath:
   * **Windows (PowerShell):**
     ```powershell
     java -cp "target/classes;$env:USERPROFILE\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar" app.MainApp
     ```
   * **Linux / macOS:**
     ```bash
     java -cp "target/classes:$HOME/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar" app.MainApp
     ```

### Base de datos y Acceso (Login / Registro)
* Al iniciar `MainApp`, `ConexionBD` crea automáticamente la base local H2 en `./data/tpg2026` (excluida de Git en `.gitignore`) y aplica el esquema idempotente de [`database/schema.sql`](database/schema.sql).
* **Alcance de la persistencia:** únicamente se persisten las credenciales de usuario (`nombre_usuario` único y `password_hash` derivado con PBKDF2 + sal aleatoria). Ninguna otra entidad del sistema se persiste.
* **Flujo de uso en la ventana principal:**
  1. **Acceso:** registrar un operador nuevo (mín. 3 caracteres de usuario, mín. 4 de contraseña) e iniciar sesión.
  2. **Pestaña Creación:** primero instanciar la nave (Exploradora, Carguero o Combate) y luego asignar al menos 5 tripulantes incluyendo 1 Capitán.
  3. **Pestaña Tripulantes:** consultar los tripulantes, su estado (`Vivo` / `Fallecido`), salario total y desglose detallado del Decorator.
  4. **Pestaña Control de la Nave:** visualizar en tiempo real el tablero 20×20 oficial de la cátedra (`destinoytablero` integrado vía `AdaptadorTableroDestino`), los recursos, la bitácora, lanzar las misiones `M-01`, `M-02` y `M-03` y consultar el historial de informes finales.
  5. **Pestaña Visor Recóndito:** agregar en vivo `GeneradoraDeEnergia` y `NaveConsumidora` operando sobre la `PlataformaDeCarga` (monitor Productor–Consumidor). Al cerrar la ventana se detienen todos los hilos ordenadamente.

### Puntos de entrada auxiliares
* **Regresión por consola (Entrega 1 adaptada):** `app.MainDemo` (ejecuta escenarios A, B, C, D, navegación y Centro de Control).
* **Visor del Universo Paralelo aislado:** `app.MainVisor`.
* **Tablero en tiempo real aislado:** `app.MainTableroSwing`.

---

## 4. Forma de Ejecutar las Verificaciones

### 1. Pruebas Unitarias JUnit 5 (63 tests)
```bash
mvn test
```
Ejecuta `NaveFactoryTest`, `NaveTest`, `RecursosTest`, `HaberesTest` y `AutenticacionTest` (Escenario **V1**: H2 en memoria y persistencia en disco entre ejecuciones).

### 2. Suite Integral de Motor, Navegación, Tablero, Adaptador Destino, Vista y Escenarios (51 aserciones)
* **Windows:**
  ```powershell
  java -cp "target/classes;target/test-classes" pruebas.EjecutarPruebas
  ```
* **Linux / macOS:**
  ```bash
  java -cp "target/classes:target/test-classes" pruebas.EjecutarPruebas
  ```
**Resultado esperado:** `Resultado: 51 pasaron, 0 fallaron`.

### 3. Verificaciones de Módulos de Entrega 2
*(En Linux/macOS reemplazar `;` por `:` en `-cp`)*
```powershell
# MisionesPanel, MisionController e Historial (V2-V6)
java "-Djava.awt.headless=true" -cp "target/classes;target/test-classes" controlador.VerificacionMisionesPanel

# Elementos del universo, Double Dispatch 3x3 y Creacion MVC (V8-V9)
java "-Djava.awt.headless=true" -cp "target/classes;target/test-classes" modelo.elementos.VerificacionElementosYCreacion

# PlataformaDeCarga (monitores wait/notifyAll) y Visor (V10-V11)
java -cp "target/classes;target/test-classes" modelo.concurrencia.VerificacionPlataformaDeCarga
java "-Djava.awt.headless=true" -cp "target/classes;target/test-classes" controlador.VerificacionVisor

# Bitacora observable y navegacion del asistente (V11)
java -cp "target/classes;target/test-classes" modelo.asistente.VerificacionBitacoraObservable
java -cp "target/classes;target/test-classes" modelo.asistente.VerificacionNavegacionAsistente

# Regresion por consola de Entrega 1 (V12)
java -cp "target/classes;target/test-classes" app.MainDemo
```

---

## 5. Etiquetas de Entrega

* **Entrega 1:** `entrega-1`
* **Entrega 2:** `entrega-e2`

```bash
git checkout entrega-e2
git tag -l "entrega-*"
```

---

## 6. Documentación (`docs/`)

* **Informe Técnico Unificado (Entregas 1 y 2):** [`docs/informe.md`](docs/informe.md)
* **Registro de Uso de Inteligencia Artificial (Entregas 1 y 2):** [`docs/uso-ia.md`](docs/uso-ia.md)
* **Esquema de Base de Datos (H2):** [`database/schema.sql`](database/schema.sql)
* **Diagramas de Entrega 2 (MVC + Observer, Double Dispatch, `wait()/notifyAll()`, Login y Adapter/State/Template):** [`docs/diagramas_e2.md`](docs/diagramas_e2.md)
* **Diagrama de Clases UML actualizado (PlantUML):** [`docs/diagrama_uml_tpg_e2.puml`](docs/diagrama_uml_tpg_e2.puml)
* **Diagrama de Clases Entrega 1 (referencia histórica):** [`docs/diagrama_uml_tpg_e1.puml`](docs/diagrama_uml_tpg_e1.puml), [`docs/diagrama_uml_tpg_e1.png`](docs/diagrama_uml_tpg_e1.png), [`docs/diagrama_uml_tpg_e1.svg`](docs/diagrama_uml_tpg_e1.svg)
