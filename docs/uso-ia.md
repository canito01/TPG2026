# Registro de uso de inteligencia artificial

Proyecto: TPG 2026 — Software embarcado de una nave interestelar tripulada.

Este documento registra las consultas de IA que influyeron en el código o la documentación del proyecto, según lo establecido en las Reglas Operativas del TPG 2026, sección 8.

## Consultas registradas

### Consulta 1 — Estructura general del proyecto, división de tareas y plazos

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Definir la estructura general del proyecto, la división de tareas y subsistemas entre los estudiantes del equipo, y coordinar los plazos del plan de trabajo para la Entrega 1.
- **Componente afectado:** Arquitectura general del proyecto, paquetes de dominio (`modelo.nave`, `modelo.motor`, `modelo.tripulacion`, `modelo.asistente`, `modelo.misiones`, `modelo.universo`, `app`) y cronograma de integración.

**Resultado aprovechado:**

Se estableció la modularización del sistema asignando a cada estudiante un subsistema y patrón de diseño específico (Nave y Recursos con Factory, Motor Warp con State, Tripulación y Haberes con Decorator, Asistente y Bitácora como Fachada/Inversión de dependencias, Misiones con Template Method). Se definieron las interfaces y contratos previos para permitir el trabajo en paralelo en distintas ramas de Git y se fijó el cronograma de fechas límite para la fase de integración final.

**Revisión:**

El equipo revisó la propuesta para garantizar que no existan dependencias circulares entre subsistemas y que cada componente pueda probarse de forma aislada antes del merge.

**Verificación:**

Creación de la estructura de paquetes en el repositorio, configuración inicial de Maven (`pom.xml`) y acuerdo del plan de trabajo del equipo.

**Estado:** Incorporado

---

### Consulta 2 — Estructura inicial del contrato del asistente

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Definir la interfaz AsistenteDeComando y las clases de apoyo (Resultado, Evento, ResumenRecursos, Bitacora) para tener un contrato claro antes de implementar.
- **Componente afectado:** paquete `modelo.asistente`

**Resultado aprovechado:**

Se usó como base para armar la interfaz y las clases de datos. Se revisaron las firmas de los métodos y se ajustaron al diseño del grupo.

**Revisión:**

Se revisó que los métodos coincidan con lo que necesitan los demás subsistemas (recursos, motor, misiones). Se corrigieron nombres y se simplificaron comentarios.

**Verificación:** Se compiló con javac sin errores. Se ejecutó VerificacionBitacora y pasó todos los casos.

**Estado:** Incorporado

---

### Consulta 3 — Excepciones de la nave e invariantes de recursos

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Definir a partir del gráfico UML las excepciones que debería tener la nave y la lista de tripulantes, y saber cuáles eran las pre y postcondiciones para que la parte de nave y recursos quede bien conectada con las demás.
- **Componente afectado:** paquete `modelo.nave` (`Nave.java`, `Recursos.java`, `RecursoFueraDeRangoException.java`)

**Resultado aprovechado:**

Inclusión de las excepciones en el constructor de la clase Nave y definición de `RecursoFueraDeRangoException` para cuando una operación deja un recurso fuera de los límites [0, 100].

**Revisión:**

Se verificó que los recursos respeten el rango 0-100, que `consumir(combustible, energia)` sea atómico (se descuentan ambos o ninguno) y que los valores iniciales coincidan con cada tipo de nave (Exploradora 60/80/0, Carguero 100/60/0, Combate 80/100/0).

**Verificación:** Pruebas unitarias de recursos (`RecursosTest`).

**Estado:** Incorporado

---

### Consulta 4 — Rediseño del Template Method de las misiones

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Revisar el diseño de la clase Mision y proponer mejoras al ciclo preparar, ejecutar, evaluar y cerrar.
- **Componente afectado:** `Mision`, `InformeMision`, `Mision1`, `Mision2` y `Mision3`

**Resultado aprovechado:**

Se incorporó el registro de acciones del ciclo para el informe, el cálculo de los recursos consumidos (en lugar de los recursos finales de la nave), el uso de try/finally para restablecer el estado de la misión y métodos de apoyo protegidos en Mision.

**Revisión:**

La IA también propuso unificar el método cerrar en la clase base. El equipo evaluó la opción y decidió mantener evaluar y cerrar abstractos, para que cada misión controle su evaluación y su informe.

**Verificación:** Compilación con Maven.

**Estado:** Incorporado

---

### Consulta 5 — Motor Warp con patrón State y Centro de Control

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Implementar, a partir del diagrama UML armado, el Motor Warp con el patrón State, el centro de control y el programa principal con los escenarios. Definir las pre y postcondiciones de cada acción del motor y los métodos que necesitan las demás partes (consultar si la nave está disponible y cómo registra el asistente un rechazo).
- **Componente afectado:** paquetes `modelo.motor` (`MotorWarp`, `EstadoMotor`, `Disponible`, `PreparandoSalto`, `EnWarp`, `Enfriamiento`) y `modelo.universo` (`CentroDeControl`)

**Resultado aprovechado:**

Estructura de estados polimórfica para el motor sin condicionales switch/if en el contexto, y registro de asistentes por su abstracción en el Centro de Control.

**Revisión:**

Se acordó que las transiciones inválidas no modifiquen la nave y avisen explícitamente lanzando `TransicionInvalidaException` para que el asistente la capture y la guarde en la bitácora.

**Verificación:** Pruebas unitarias de secuencia válida y transiciones inválidas en `MotorWarpTest`.

**Estado:** Incorporado

---

### Consulta 6 — Adaptación de las misiones al contrato del Asistente de Comando

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Resolver la incompatibilidad entre los métodos que usaba Mision (verificarViabilidad y consumirRecursos) y los definidos en AsistenteDeComando.
- **Componente afectado:** `Mision.java` (métodos preparar y ejecutar)

**Resultado aprovechado:**

Uso de `consumir()` con su `Resultado`, lanzando `IllegalStateException` si los recursos son insuficientes.

**Revisión:**

Se acordó la solución con el contrato del asistente y se verificaron los nombres reales de los métodos, asegurando que si falla no haya cambios parciales.

**Verificación:** Compilación con Maven y prueba de rechazo por recursos insuficientes.

**Estado:** Incorporado

---

### Consulta 7 — Pruebas unitarias para el cálculo de haberes y patrón Decorator

- **Herramienta:** Antigravity (Google DeepMind)
- **Propósito:** Diseñar la suite de pruebas unitarias con JUnit 5 para validar el cálculo de sueldos y adicionales sobre la estructura del patrón Decorator ya implementada.
- **Componente afectado:** `src/test/java/modelo/tripulacion/TripulacionTest.java`

**Resultado aprovechado:**

Casos de prueba con JUnit 5 que comprueban los sueldos base por cargo, los bonos por antigüedad, los bonos de consejero, los subsidios por planeta y la liquidación encadenada con múltiples decoradores juntos (caso de referencia Spock: Consejero Vulcano con 4 años y 3 consejos = 756 PG).

**Revisión:**

Se comprobó que las fórmulas de cálculo coincidan con la consigna y se validaron los casos de antigüedad cero y validación de consejos no negativos.

**Verificación:** Ejecución de `mvn test` con todos los casos de tripulación pasando en verde.

**Estado:** Incorporado

---

### Consulta 8 — Implementación de AsistenteBase

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Implementar la clase AsistenteBase que conecta la interfaz con los subsistemas ya existentes (Nave, Recursos, MotorWarp).
- **Componente afectado:** `AsistenteBase.java`, `TransicionInvalidaException.java`

**Resultado aprovechado:**

Se usó como guía para la delegación a los subsistemas y el manejo de excepciones de dominio (convertirlas en Resultado). Se revisó y se adaptó a los métodos reales de Recursos y MotorWarp.

**Revisión:**

Se verificó que las llamadas a Recursos.consumir(), desgastar(), cargarCombustible(), etc. coincidan con las firmas reales.

**Verificación:** Se compiló el paquete asistente + nave + motor sin errores ni warnings con javac --release 17.

**Estado:** Incorporado

---

### Consulta 9 — Detección y corrección de errores de compilación en Misiones

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Identificar errores de compilación en las clases del paquete modelo.misiones.
- **Componente afectado:** `Mision`, `InformeMision`, `Mision1`, `Mision2` y `Mision3`

**Resultado aprovechado:**

Corrección de paquetes inconsistentes y normalización de firmas.

**Revisión:**

El equipo aplicó las correcciones y revisó que cada clase siguiera la convención de nombres de Java.

**Verificación:** Compilación exitosa con Maven.

**Estado:** Incorporado

---

### Consulta 10 — Integración de escenarios de demostración y resolución de compatibilidad

- **Herramienta:** Antigravity (Google DeepMind)
- **Propósito:** Resolver errores de compatibilidad tras el merge entre ramas e implementar los escenarios de demostración A, B y D requeridos para la entrega.
- **Componente afectado:** `app.EscenarioA`, `app.EscenarioB`, `app.EscenarioD`, `app.Main`, `app.PruebasEscenarios`

**Resultado aprovechado:**

Estructura de código de los escenarios para simular la ejecución correcta (Escenario A), el rechazo atómico por falta de recursos (Escenario B) y la preservación de estado ante operaciones inválidas (Escenario D).

**Revisión:**

Se verificó que los escenarios respeten los contratos acordados sin romper nada y reproduzcan los casos pedidos por la cátedra.

**Verificación:** Ejecución de `Main.java` y `EjecutarPruebas` con todos los escenarios pasando sin errores.

**Estado:** Incorporado

---

### Consulta 11 — Actualización y validación del diagrama de clases UML

- **Herramienta:** Antigravity (Google DeepMind)
- **Propósito:** Actualizar el diagrama de clases en formato PlantUML modificando el bosquejo planteado inicialmente con la incorporación de las clases ya creadas y los ajustes reales de la implementación.
- **Componente afectado:** carpeta `docs/` (`diagrama_uml_tpg_e1.puml`, `.png`, `.svg`)

**Resultado aprovechado:**

Se modificó el diagrama planteado inicialmente con la incorporación de las clases ya creadas en el proyecto, organizándolo de forma completa por paquetes (`modelo.tripulacion`, `modelo.motor`, `modelo.asistente`, `modelo.misiones`, `modelo.fabrica`, `app`), detallando atributos, métodos y relaciones entre clases (herencia, interfaces, decoradores y máquina de estados).

**Revisión:**

Se contrastaron las flechas y firmas contra el código real de la rama develop, asegurando que el diagrama refleje con exactitud la estructura definitiva implementada para la Entrega 1.

**Verificación:** Renderizado exitoso del diagrama a PNG y SVG mediante la herramienta PlantUML local sin errores de sintaxis.

**Estado:** Incorporado

---

### Consulta 12 — Unificación y estructuración del informe técnico de entrega

- **Herramienta:** Antigravity (Google DeepMind)
- **Propósito:** Tomar los resúmenes y notas aportadas por cada integrante en relación a su parte trabajada, darles un formato homogéneo y unificarlos en un solo informe técnico completo para la Entrega 1.
- **Componente afectado:** `informe/informe.md`

**Resultado aprovechado:**

Se integraron en un único documento los resúmenes de todos los subsistemas (Nave/Recursos/Fábrica, Tripulación/Haberes, Motor Warp, Asistente de Comando/Bitácora, Misiones, Centro de Control y Escenarios), estructurándolos bajo un formato común (Qué hace, Archivos y responsabilidades, Decisiones de diseño y patrones, Conexiones y dependencias), manteniendo la redacción técnica original de cada parte y removiendo nombres individuales para reflejar la autoría conjunta del equipo.

**Revisión:**

El equipo revisó que el informe integrado reflejara con exactitud el estado del código en la rama develop, conservando las tablas de responsabilidades, matrices de transición del motor y los contratos de cada subsistema.

**Verificación:**

Revisión y lectura completa del archivo Markdown generado (`informe/informe.md`).

**Estado:** Incorporado
