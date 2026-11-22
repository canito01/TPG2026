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

---

## Consultas registradas — Entrega 2

### Consulta 13 — Recursos y bitácora observables con patrón Observer

- **Herramienta:** Codex (OpenAI)
- **Propósito:** Incorporar el patrón Observer propio para que el panel de recursos y bitácora se actualice automáticamente ante cambios en los recursos o nuevos eventos de la nave seleccionada.
- **Componente afectado:** `util.observer` (`Sujeto`, `Observador`), `modelo.asistente.Bitacora`, `modelo.nave.Recursos`, `vista.RecursosBitacoraPanel` y `controlador.RecursosBitacoraController`.

**Resultado aprovechado:**

Se implementaron `Sujeto` y `Observador` con la firma `actualizar(String mensaje)` y se suscribió la vista a través del asistente de la nave elegida, actualizando Swing en el EDT. Además se corrigió el consumo de recursos para que, si el desgaste supera el límite, se conserven intactos combustible, energía y desgaste.

**Revisión:**

Se revisó que los avisos lleguen solo al observador de la nave activa, que al cambiar de nave se actualicen las suscripciones sin fugas y que un consumo rechazado no produzca cambios parciales.

**Verificación:**

Ejecución de `modelo.asistente.VerificacionBitacoraObservable` pasando todos los casos.

**Estado:** Incorporado

---

### Consulta 14 — Plataforma de carga concurrente y Visor del Universo Paralelo

- **Herramienta:** Codex (OpenAI)
- **Propósito:** Diseñar la plataforma compartida entre generadoras de energía y naves consumidoras con monitores en Java (`synchronized`, `while`, `wait()`, `notifyAll()`) y su panel de visualización en vivo.
- **Componente afectado:** `modelo.concurrencia` (`PlataformaDeCarga`, `GeneradoraDeEnergia`, `NaveConsumidora`, `UniversoParalelo`), `vista.VisorPanel`, `controlador.VisorController` y `app.MainVisor`.

**Resultado aprovechado:**

Se armó la sincronización de la plataforma de capacidad limitada con condiciones de guarda dentro de bucles `while`, espera cuando se llena o vacía y notificación con `notifyAll()`. Se conectó el visor con dos botones para agregar entidades dinámicamente en tiempo real y cierre ordenado de hilos.

**Revisión:**

Se revisaron las condiciones de espera y despertar para descartar deadlocks o pérdida de baterías, así como la desconexión e interrupción limpia de hilos al cerrar la ventana.

**Verificación:**

Ejecución de `modelo.concurrencia.VerificacionPlataformaDeCarga` (intercambio de 300 baterías entre 6 hilos y ambas esperas) y `controlador.VerificacionVisor` en modo headless.

**Estado:** Incorporado

---

### Consulta 15 — Operaciones de navegación en el Asistente de Comando

- **Herramienta:** Codex (OpenAI)
- **Propósito:** Conectar el asistente con el motor, los radares y el tablero para ofrecer a las misiones operaciones de consulta de posición, escaneo, salto normal y salto Warp.
- **Componente afectado:** `modelo.asistente` (`AsistenteDeComando`, `AsistenteBase`, `NavegacionDeMision`, `NavegacionConTablero`, `PosicionNave`, `ResultadoEscaneo`, `ResultadoSalto`).

**Resultado aprovechado:**

Se prepararon las operaciones de navegación y su registro automático en bitácora, devolviendo estados claros que distinguen si la nave avanzó, si el motor está preparando o enfriando, o si la maniobra fue rechazada.

**Revisión:**

Se corroboró que el asistente siga actuando como único intermediario entre las misiones y la nave, delegando la lógica a cada subsistema.

**Verificación:**

Ejecución de `modelo.asistente.VerificacionNavegacionAsistente` con todos los casos verificados.

**Estado:** Incorporado

---

### Consulta 16 — Recursos ampliados, destrucción de nave y fábrica de naves enemigas

- **Herramienta:** Antigravity (Google DeepMind)
- **Propósito:** Ampliar `Recursos`, `Nave` y `NaveFactory` para cubrir gemas, recargas con tope y total, entrega atómica de suministros, estado de destrucción y creación de naves enemigas manteniendo los contratos de la Entrega 1.
- **Componente afectado:** `modelo.nave` (`Recursos`, `Nave`) y `modelo.fabrica.NaveFactory`.

**Resultado aprovechado:**

Se incorporaron las gemas para `M-02`, `entregarRecursos` atómico para `M-01`, `recargarHastaElTope` para estaciones, `recargaTotal` con mantenimiento para `M-03`, `estaDestruida()` (desgaste 100 o energía/combustible en 0, provocando la muerte de la tripulación) y `NaveFactory.crearNaveEnemiga`.

**Revisión:**

Se comprobó que las operaciones estrictas de la Entrega 1 sigan rechazando excesos sin cambios parciales y que al destruirse la nave toda la tripulación pase a estado fallecido.

**Verificación:**

Ejecución de `RecursosTest`, `NaveTest` y `NaveFactoryTest` pasando en verde.

**Estado:** Incorporado

---

### Consulta 17 — Elementos del universo y resolución de batallas con Double Dispatch

- **Herramienta:** Antigravity (Google DeepMind)
- **Propósito:** Implementar `ElementoDelUniverso`, `Asteroide`, `EstacionDeCarga` y `NaveEnemiga`, y resolver las batallas entre naves según la tabla 3×3 aplicando Double Dispatch sin condicionales de tipo.
- **Componente afectado:** `modelo.elementos` (`ElementoDelUniverso`, `ResultadoEncuentro`, `NaveEnemiga`, `Asteroide`, `EstacionDeCarga`) y `modelo.nave` (`Nave`, `NaveExploradora`, `NaveCarguero`, `NaveCombate`).

**Resultado aprovechado:**

Se distribuyó la tabla de encuentros 3×3 en los métodos polimórficos `lucharContraExploradora`, `lucharContraCarguero` y `lucharContraCombate` sin usar `if`, `switch` ni `instanceof`. Se implementó el daño fijo de energía hasta 0 en asteroides y la recarga por monto fijo o porcentaje en estaciones.

**Revisión:**

Se contrastaron las 9 combinaciones de combate contra la tabla del enunciado y se verificó que perder un encuentro destruye la nave y su tripulación.

**Verificación:**

Ejecución de `modelo.elementos.VerificacionElementosYCreacion` verificando las 9 combinaciones, asteroides y ambos modos de estación.

**Estado:** Incorporado

---

### Consulta 18 — Sección de Creación bajo arquitectura MVC

- **Herramienta:** Antigravity (Google DeepMind)
- **Propósito:** Armar `CreacionPanel` y `CreacionController` separando validaciones de formato en la vista de las reglas de dominio en el controlador.
- **Componente afectado:** `vista.CreacionPanel` y `controlador.CreacionController`.

**Resultado aprovechado:**

Se estructuró la pantalla para instanciar naves mediante `NaveFactory`, agregar tripulantes con validación numérica en vista y verificar en el controlador la regla de tripulación mínima (al menos 1 capitán y 4 tripulantes adicionales vivos).

**Revisión:**

Se revisó que la vista no conozca la fábrica ni las reglas de negocio y que las actualizaciones de tabla se ejecuten en el EDT.

**Verificación:**

Pruebas en modo headless en `modelo.elementos.VerificacionElementosYCreacion`.

**Estado:** Incorporado

---

### Consulta 19 — Reescritura del Motor Warp con patrón State según el Anexo

- **Herramienta:** Claude (Anthropic) y Antigravity (Google DeepMind)
- **Propósito:** Reescribir `MotorWarp` y sus cuatro estados para cumplir las reglas del Anexo exponiendo únicamente `saltar()` y `saltarWarp()` hacia el exterior.
- **Componente afectado:** `modelo.motor` (`MotorWarp`, `EstadoMotor`, `EstadoBase`, `Disponible`, `PreparandoSalto`, `EnWarp`, `Enfriamiento`, `SistemasDeLaNave`, `ResultadoDeTurno`, `ParametrosDelMotor`).

**Resultado aprovechado:**

Se modelaron los 2 turnos de preparación verificando 5 de energía, el aborto con reinicio de preparación si se pierde la energía al saltar, el turno de enfriamiento con 3 de desgaste y un constructor por defecto que conserva compatibilidad con la Entrega 1.

**Revisión:**

Se comprobó por reflexión que la API pública del motor exponga únicamente `saltar()` y `saltarWarp()` y que no haya condicionales por estado en el contexto.

**Verificación:**

Ejecución de `pruebas.PruebasMotorWarp` y `app.EscenarioC` pasando sin fallos.

**Estado:** Incorporado

---

### Consulta 20 — Interfaz Tablero, TableroStub, radares y planificación de ruta BFS

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Diseñar la abstracción del tablero de 20×20 desacoplada de la clase de la cátedra, junto con un stub reproducible y los radares de largo y corto alcance.
- **Componente afectado:** `modelo.tablero` (`Tablero`, `TableroStub`, `InstantaneaDelTablero`, `ElementoDePrueba`) y `modelo.navegacion` (`Coordenada`, `Ruta`, `PlanificadorDeRuta`, `RadarLargoAlcance`, `RadarCortoAlcance`).

**Resultado aprovechado:**

Se diseñó la interfaz `Tablero`, `TableroStub` con `Random` con semilla fija, `Ruta` inmutable con validación de adyacencia ortogonal y `PlanificadorDeRuta` mediante BFS.

**Revisión:**

Se verificó el cálculo de rutas mínimas (distancia Manhattan), los bordes del tablero y la reproducibilidad exacta de un viaje completo con la misma semilla.

**Verificación:**

Ejecución de `pruebas.PruebasTablero`, `pruebas.PruebasNavegacion` y `app.EscenarioNavegacion`.

**Estado:** Incorporado

---

### Consulta 21 — Vista del tablero en tiempo real segura para hilos

- **Herramienta:** Claude (Anthropic) y Antigravity (Google DeepMind)
- **Propósito:** Conectar `TableroPanel` y `TableroController` con Observer sin congelar Swing ni producir carreras entre el hilo de la misión y el EDT.
- **Componente afectado:** `vista.TableroPanel`, `controlador.TableroController`, `modelo.tablero.InstantaneaDelTablero` y `app.MainTableroSwing`.

**Resultado aprovechado:**

Se diseñó `InstantaneaDelTablero` como copia profunda inmutable para que `TableroPanel` dibuje siempre una foto segura dentro de `SwingUtilities.invokeLater`, adaptando la suscripción a `Observador.actualizar(String mensaje)`.

**Revisión:**

Se comprobó la desconexión del observador al cerrar la ventana y la seguridad concurrente entre hilos moviendo elementos y tomando copias.

**Verificación:**

Ejecución de `vista.PruebasVista` y `pruebas.EjecutarPruebas` en verde.

**Estado:** Incorporado

---

### Consulta 22 — Refactorización de Misiones con métodos de enganche (hooks) en Template Method

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Evitar que las tres misiones dupliquen el ciclo de vida y el bucle de viaje por turnos sobre el tablero.
- **Componente afectado:** `modelo.misiones` (`Mision`, `Mision1`, `Mision2`, `Mision3`).

**Resultado aprovechado:**

Los pasos del ciclo y el bucle de viaje quedaron escritos una sola vez en `Mision`, mientras las subclases redefinen únicamente los hooks propios de cada misión (destino, energía de despegue, verificación en destino, recompensa final y textos del informe).

**Revisión:**

Respecto de la Parte 1 (donde `evaluar` y `cerrar` eran abstractos), el equipo evaluó el diseño y adoptó hooks en la clase base para evitar triplicar el bucle de travesía y centralizar correcciones.

**Verificación:**

Compilación limpia y pruebas de las tres misiones.

**Estado:** Incorporado

---

### Consulta 23 — Revisión de casos límite y tope de turnos en Misiones

- **Herramienta:** Claude (Anthropic)
- **Propósito:** Revisar las clases de misión en busca de casos límite antes de integrarlas con el asistente y el tablero oficial.
- **Componente afectado:** `modelo.misiones` (`Mision`, `InformeMision`, `Mision1`, `Mision2`, `Mision3`).

**Resultado aprovechado:**

Se identificó el riesgo de bucle infinito cuando el Motor Warp queda en preparación por falta de energía, agregando un tope de 200 turnos que hace fallar la misión con motivo claro, y se ajustó la verificación de recursos mínimos para que el despegue no destruya la nave.

**Revisión:**

El equipo revisó cada hallazgo y validó los invariantes al final de cada fase mediante `verificarInvariantes()`.

**Verificación:**

Compilación con Java 17 y pruebas de casos límite en `VerificacionMisionesPanel`.

**Estado:** Incorporado

---

### Consulta 24 — Pruebas de persistencia H2 y validación de credenciales (Escenario V1)

- **Herramienta:** Antigravity (Google DeepMind)
- **Propósito:** Verificar la configuración de H2 en Maven (`pom.xml` y `database/schema.sql`), el almacenamiento de contraseñas con PBKDF2 y sal usando `javax.crypto`, y diseñar las pruebas unitarias del escenario V1 en memoria y en disco.
- **Componente afectado:** `pom.xml`, `database/schema.sql`, `servicio.HashContrasena`, `src/test/java/persistencia/AutenticacionTest.java` y `src/test/java/modelo/tripulacion/HaberesTest.java`.

**Resultado aprovechado:**

Se definieron los casos de prueba de alta, usuario duplicado, credenciales inválidas, validaciones de formato y persistencia entre ejecuciones sobre H2, junto con los tests del desglose inmutable de haberes (`ItemHaber`) y estado vital en `Tripulante`.

**Revisión:**

Se comprobó que solo se persistan credenciales según la consigna, que todas las consultas JDBC usen `PreparedStatement` con `try-with-resources` y que las reglas de haberes de la Entrega 1 permanezcan sin cambios.

**Verificación:**

Ejecución de `AutenticacionTest` (5/5 en verde) y `HaberesTest` (5/5 en verde).

**Estado:** Incorporado

---

### Consulta 25 — Integración final de módulos, adaptador de Destino y unificación de documentación

- **Herramienta:** Antigravity (Google DeepMind)
- **Propósito:** Apoyar la integración final en `MainApp` (adaptador del módulo `destinoytablero` provisto por la cátedra y panel/controlador de misiones), generar los diagramas actualizados de la Parte 2 y unificar los resúmenes del equipo en el informe técnico.
- **Componente afectado:** `modelo.tablero.AdaptadorTableroDestino`, `vista.MisionesPanel`, `controlador.MisionController`, `modelo.misiones.HistorialDeInformes`, `app.MainApp`, `docs/informe.md`, `docs/diagramas_e2.md`, `docs/diagrama_uml_tpg_e2.puml` y `README.md`.

**Resultado aprovechado:**

Se integró el módulo oficial `destinoytablero` sin modificar su lógica mediante el patrón Adapter, se conectaron todas las secciones en `MainApp` y se unificaron los resúmenes aportados por el equipo en `docs/informe.md` junto con los diagramas actualizados de clases, MVC y secuencias (Double Dispatch y `wait()`/`notifyAll()`).

**Revisión:**

Se validó que ninguna clase de `destinoytablero` alterara su comportamiento original, que el informe refleje con exactitud las firmas y clases reales de la rama `develop` y que todas las verificaciones V1 a V12 pasen en conjunto.

**Verificación:**

Compilación total, ejecución de `EjecutarPruebas` (51/51), `VerificacionMisionesPanel` (V2–V6), pruebas JUnit 5 (63/63) y prueba manual punta a punta de `app.MainApp`.

**Estado:** Incorporado
