# Informe de Diseño y Arquitectura — Entregas 1 y 2

**Proyecto:** TPG 2026 — Software embarcado de una nave interestelar tripulada  
**Alcance:** Entrega 1 (Modelo de dominio y patrones base) + Entrega 2 (Universo dinámico, concurrencia, persistencia e interfaz gráfica)

---

# Parte 1 — Modelo de dominio, patrones de diseño y escenarios base

---

## 1. Introducción General

Este proyecto implementa el software embarcado de una nave interestelar tripulada para la Entrega 1 del TPG. El sistema está estructurado en subsistemas desacoplados, donde cada uno resuelve una responsabilidad puntual del dominio mediante el uso de objetos, contratos claros y patrones de diseño reconocidos:

- **Nave, Recursos y Fábrica:** Modela la nave física, sus recursos (combustible, energía, desgaste) y su creación encapsulada con el patrón Factory.
- **Tripulación y Haberes:** Modela a los tripulantes y el cálculo flexible y acumulativo de sueldos con el patrón Decorator.
- **Motor Warp:** Modela la propulsión hiperespacial como una máquina de estados con el patrón State.
- **Asistente de Comando y Bitácora:** Actúa como intermediario/fachada entre las misiones y la nave, resguardando la bitácora y convirtiendo errores de dominio en resultados controlados.
- **Misiones:** Modela el ciclo de vida cerrado de las operaciones espaciales con el patrón Template Method y la generación del informe de misión.
- **Centro de Control y Escenarios:** Gestiona las naves activas y ejecuta las demostraciones sin mezclar la lógica de negocio con la consola.

Ninguna clase del modelo usa `Scanner`, `System.out` ni componentes visuales; toda la entrada y salida de demostración se encuentra aislada en el paquete `app`.

---

## 2. Nave, Recursos y Factory

### Qué hace

Modela la nave y sus recursos, y permite crear naves sin que el código cliente conozca las clases concretas.

| Clase | Responsabilidad |
|---|---|
| `Nave` (abstracta) | Identidad, tipo, recursos, motor y tripulación. Es la abstracción común de todas las naves. |
| `NaveExploradora`, `NaveCarguero`, `NaveCombate` | Cada una fija su configuración inicial de recursos. |
| `TipoNave` (enum) | Etiqueta del tipo: `EXPLORADORA`, `CARGUERO`, `COMBATE`. |
| `Recursos` | Combustible, energía y desgaste. Valida y protege sus propios límites. |
| `RecursoFueraDeRangoException` | Se lanza cuando una operación dejaría un recurso fuera de rango. |
| `NaveFactory` (`modelo.fabrica`) | El cliente pide un `TipoNave` y recibe una `Nave` lista para usar. |

### Reglas que cumple

- **Configuración inicial** (combustible / energía / desgaste): Exploradora 60/80/0, Carguero 100/60/0, Combate 80/100/0.
- **Invariantes de `Recursos`:** combustible, energía y desgaste siempre entre 0 y 100. Se requiere mantenimiento con desgaste de 80 o más.
- **Sin cambios parciales:** si una operación no puede completarse sin violar un límite, se rechaza y el estado anterior queda intacto. `consumir(combustible, energia)` es atómico: se descuentan ambos o ninguno.
- **Mantenimiento:** `realizarMantenimiento()` lleva el desgaste a 0 y no falla nunca.
- **Encapsulación:** los atributos son privados y la tripulación se entrega como lista de solo lectura (`Collections.unmodifiableList`).
- **Sin consola ni Swing:** ninguna clase usa `Scanner`, `System.out` ni componentes gráficos.

### Operaciones de `Recursos`

- **Modifican:** `cargarCombustible`, `cargarEnergia`, `consumir`, `desgastar`, `realizarMantenimiento`.
- **Consultan sin modificar:** `getCombustible`, `getEnergia`, `getDesgaste`, `requiereMantenimiento`, `copia`, y `puedeCargarCombustible`, `puedeCargarEnergia`, `puedeConsumir`, `puedeDesgastar`.

### Manejo de errores

- `RecursoFueraDeRangoException`: el argumento es válido pero no entra dado el estado actual (por ejemplo, cargar combustible con la nave en 95). Es una excepción no verificada: el asistente la captura, registra el motivo en la bitácora y devuelve un resultado de rechazo.
- `IllegalArgumentException`: argumento inválido por sí solo (cantidad cero o negativa, id vacío, valores nulos).

### Uso del patrón Factory

`NaveFactory.crear(TipoNave, id)` busca el constructor del tipo en un registro interno, sin condicionales por tipo, y verifica que la nave creada sea del tipo pedido. Para agregar un tipo nuevo se hacen tres cambios localizados:
1. Una constante nueva en `TipoNave`.
2. Una clase nueva que extienda `Nave`, siguiendo el esquema de `NaveExploradora`.
3. Una línea de registro en el constructor de `NaveFactory`.

### Conexiones y dependencias

- **Depende de:** `MotorWarp`, que la nave contiene, y `Tripulante`, que forma la tripulación.
- **Dependen de ella:** el asistente opera una nave y usa sus recursos; las misiones consumen recursos a través del asistente; el `Main` y los escenarios crean las naves con la fábrica.

---

## 3. Tripulación y Haberes

### Qué hace

Modela al personal a bordo de la nave (`Tripulante`, `Cargo`, `Origen`) y se encarga del cálculo y liquidación de haberes mensuales en Patrones Galácticos (PG).

Cada tripulante tiene un sueldo base determinado por su rango jerárquico. Sobre esa base se pueden aplicar distintos adicionales y bonificaciones según su antigüedad, su planeta de origen y las tareas especiales realizadas (como los consejos brindados por los consejeros).

Para calcular el haber total y generar el desglose explicativo de forma flexible, combinable y sin recurrir a una explosión de subclases, se implementó el **Patrón Decorator**.

### Archivos y responsabilidades

- `Tripulante` — clase de dominio con los datos del tripulante (identidad, cargo, origen y antigüedad en años). Valida en su constructor que la antigüedad no sea negativa.
- `Cargo` — enum con los rangos y sus bases salariales (`CAPITAN`: 1000 PG, `CONSEJERO`: 600 PG, `TENIENTE`: 400 PG, `ALFEREZ`: 200 PG).
- `Origen` — enum con las procedencias planetarias (`TERRICOLA`: 20 PG, `VULCANO`: 30 PG, `MARCIANO`: 18 PG).
- `ConceptoHaber` — interfaz base que define el contrato del componente (`calcularTotal()` y `obtenerDetalle()`).
- `SueldoBase` — componente concreto que implementa `ConceptoHaber`. Retorna el haber inicial según el cargo del tripulante.
- `HaberDecorator` — clase abstracta base para los decoradores. Mantiene la referencia al `ConceptoHaber` envuelto y al `Tripulante`, delegando las llamadas hacia adentro.
- `BonoAntiguedad` — decorador concreto que suma un porcentaje por año de antigüedad calculado sobre la base del cargo (Capitán: 20%/año, Consejero: 5%/año, Teniente: 3%/año, Alférez: 0.5%/año).
- `BonoConsejero` — decorador concreto que suma 2 PG por cada consejo brindado. Valida que la cantidad de consejos no sea negativa y solo aplica si el tripulante tiene cargo de Consejero.
- `SubsidioOrigen` — decorador concreto que suma un monto fijo por procedencia (Terrícola: 20 PG, Vulcano: 30 PG, Marciano: 18 PG).

### Por qué usamos Decorator

Los adicionales salariales son acumulativos, opcionales y se combinan dinámicamente en tiempo de ejecución para cada tripulante:
1. **Evita la explosión de clases:** Si hubiéramos usado herencia tradicional, necesitaríamos crear clases para cada combinación posible (`CapitanConAntiguedad`, `CapitanConAntiguedadYSubsidioTerricola`, etc.), lo cual sería inmanejable.
2. **Estructura en capas (cebolla):** `SueldoBase` constituye el núcleo. Cada bono envuelve al concepto anterior. Al invocar `calcularTotal()`, la consulta se propaga recursivamente hacia el interior hasta llegar a la base, y las respuestas numéricas se van sumando hacia afuera.
3. **Desglose detallado:** De la misma manera que se calcula el total numérico, cada decorador concatena su línea en `obtenerDetalle()`, permitiendo emitir un recibo claro con la justificación de cada concepto liquidado.
4. **Principio Abierto/Cerrado (OCP):** Si en el futuro se incorporan nuevos adicionales (por ejemplo, "BonoPorRiesgo"), basta con crear un nuevo decorador que herede de `HaberDecorator`, sin tocar `Tripulante`, `SueldoBase` ni los decoradores existentes.

### Conexiones y dependencias

- El paquete `modelo.tripulacion` es completamente autónomo y no depende de la nave, del motor ni del asistente.
- `Tripulante` solo conoce sus enums de apoyo (`Cargo` y `Origen`).
- Los decoradores conocen la interfaz `ConceptoHaber` (a la que envuelven e implementan) y leen los datos necesarios de `Tripulante`, sin alterar su estado interno.

---

## 4. Motor Warp

### Qué hace

Modela el sistema de propulsión hiperespacial con el patrón State, controlando el ciclo operacional de salto y asegurando que las transiciones entre fases se efectúen únicamente en el orden permitido.

| Clase | Responsabilidad |
|---|---|
| `MotorWarp` (contexto) | Mantiene el estado actual y delega en él cada acción. No contiene `switch` ni `if` por estado. |
| `EstadoMotor` (interfaz) | Declara las acciones: preparar salto, saltar, enfriar y terminar enfriamiento. |
| `EstadoBase` (abstracta) | Implementación por defecto que rechaza cualquier acción no soportada. |
| `Disponible`, `PreparandoSalto`, `EnWarp`, `Enfriamiento` | Cada estado decide qué acciones acepta y a qué estado pasa. |
| `TransicionInvalidaException` | Se lanza cuando se intenta una transición no permitida desde el estado actual. |

### Transiciones del motor (patrón State)

Secuencia válida: Disponible → Preparando salto → En warp → Enfriamiento → Disponible.

| Estado actual | Preparar salto | Saltar | Enfriar | Terminar enfriamiento |
|---|---|---|---|---|
| Disponible | Preparando salto | rechazada | rechazada | rechazada |
| Preparando salto | rechazada | En warp | rechazada | rechazada |
| En warp | rechazada | rechazada | Enfriamiento | rechazada |
| Enfriamiento | rechazada | rechazada | rechazada | Disponible |

Una transición inválida **no modifica la nave** y avisa de forma explícita lanzando `TransicionInvalidaException` (no es silenciosa), para que el asistente la capture y la registre en la bitácora.

### Reglas de diseño

- El comportamiento vive dentro de cada estado, no en un condicional central (`switch` o `if`).
- Agregar un estado nuevo no obliga a modificar los existentes ni a llenar la nave de condicionales.
- Al terminar el salto la nave vuelve a Disponible. `Enfriamiento` existe, está probado y se puede alcanzar por su transición.

### Conexiones y dependencias

- **Depende de:** `Nave`, que contiene al motor.
- **Dependen de él:** El asistente usa el motor para ejecutar y registrar las órdenes de salto solicitadas por el centro de control o las misiones.

---

## 5. Asistente de Comando y Bitácora

### Qué hace

El asistente es básicamente el que está en el medio entre las misiones y la nave. Las misiones no tocan los recursos ni el motor directamente: le piden al asistente y él se fija si se puede o no.

Si algo falla (no hay combustible, el motor no está en el estado correcto, etc.), el asistente atrapa el error y devuelve un `Resultado` diciendo que no se pudo, en vez de romper todo con una excepción. Además va guardando todo lo que pasa en la bitácora.

### Archivos y responsabilidades

- `AsistenteDeComando` — interfaz, el contrato.
- `AsistenteBase` — la implementación, delega a Nave, Recursos y MotorWarp.
- `Resultado` — dice si salió bien o mal, con un mensaje explicativo.
- `ResumenRecursos` — copia de los recursos para consultar sin tocar nada.
- `Evento` — un registro con fecha y descripción.
- `Bitacora` — guarda los eventos y te da copias.
- `TransicionInvalidaException` — la tira el motor cuando le pedís algo que no puede hacer.

### Por qué lo hicimos así

- **Separación de interfaz y clase:** Se separó la interfaz de la clase porque así las misiones y el centro de control dependen del contrato nada más. Si después se necesita otro tipo de asistente no hay que cambiar nada de lo que ya está.
- **Inmutabilidad:** Los objetos como `Resultado`, `Evento` y `ResumenRecursos` son inmutables, tienen todo `final` y no tienen setters. La idea es que una vez que se crean no se puedan tocar.
- **Copias defensivas:** La bitácora cuando la consultás te da una copia de la lista. Si después se agregan más eventos, la lista que ya tenías no cambia.
- **Patrón:** Actúa como una Fachada (Facade) y aplica Inversión de Dependencias (DIP) sobre la nave y sus subsistemas.

### Conexiones y dependencias

El asistente conoce a Nave, Recursos y MotorWarp. Ellos no conocen al asistente. Las misiones usan la interfaz `AsistenteDeComando`, no la clase `AsistenteBase`.

---

## 6. Misiones

### Qué hace y alcance

El paquete `modelo.misiones` contiene las tres misiones obligatorias de la Entrega 1:
- **M-01:** Intercepción y asistencia.
- **M-02:** Recolección.
- **M-03:** Retorno seguro.

Y el informe que cada una genera al terminar (`InformeMision`). Son versiones simplificadas que no dependen del universo dinámico. Para consumir recursos no tocan la nave directamente: le piden al Asistente de Comando que lo haga.

### Por qué usamos Template Method

Las tres misiones hacen lo mismo en el mismo orden: preparar, ejecutar, evaluar y cerrar (E1-06). Lo único que cambia de una a otra es el objetivo. Si cada misión tuviera su propio ciclo, el mismo código estaría repetido tres veces, y cambiar el orden de un paso implicaría tocar las tres clases.

Por eso el ciclo está escrito una sola vez, en el método `ejecutarCiclo` de `Mision`, que es `final`. Así ninguna subclase puede modificar el orden de los pasos y cada una se ocupa solamente de lo suyo: evaluar, cerrar y la energía adicional que pide su objetivo.

### Contratos que cumple

Los contratos están documentados y el código los hace cumplir:
- **Preparación previa:** `ejecutar` revisa `estaPreparada` y, si la misión no fue preparada, lanza `IllegalStateException`.
- **Recursos insuficientes:** El consumo lo hace `AsistenteDeComando.consumir()`: verifica todos los recursos antes de modificar alguno. Si falla, `ejecutar` lanza `IllegalStateException` y la nave queda como estaba.
- **Resultado e informe obligatorios:** `cerrar` siempre devuelve un `InformeMision`, así que una misión no puede terminar sin informe.
- **Misión reutilizable:** El bloque `finally` de `ejecutarCiclo` deja `estaPreparada` en `false` aunque algún paso falle. De esa forma, una misión rechazada no queda marcada como preparada.

### Informe de misión

`InformeMision` guarda el nombre de la misión, el resultado, las acciones realizadas, los recursos consumidos, el estado final de la nave y las observaciones. Las acciones se van anotando en `Mision` a lo largo del ciclo (preparación, ejecución, evaluación y cierre) y se pasan al informe cuando la misión se cierra. En cuanto a los recursos, el informe muestra lo que gastó la misión, no el estado en que quedó la nave.

---

## 7. Centro de Control y Escenarios de Demostración

### Qué hace

El `CentroDeControl` (`modelo.universo`) registra asistentes por su abstracción (`AsistenteDeComando`), devuelve uno por id y mantiene una única nave activa a la vez. Una variante nueva de asistente se registra sin tocar el centro.

En el paquete `app`, el programa principal `Main` ejecuta cuatro escenarios reproducibles de demostración:

- **Escenario A (ejecución correcta):** Crear una nave de cada tipo con la fábrica, asignar tripulación y completar M-01, M-02 y M-03 con recursos finales, bitácora e informe.
- **Escenario B (recursos insuficientes):** La misión se rechaza por falta de recursos, sin cambios parciales y con el motivo registrado en la bitácora.
- **Escenario C (Motor Warp):** Recorrer la secuencia válida completa de estados y probar transiciones inválidas, comprobando que se rechazan, no alteran el estado y quedan registradas.
- **Escenario D (contrato inválido):** Una carga que excede la capacidad máxima [0, 100] conserva el estado anterior intacto.
- **Demostración de Centro de Control:** Alta de naves, selección de nave activa y control de identificadores duplicados o nulos.

El modelo no usa `Scanner` ni `System.out`. La consola solo existe en `Main`, que puede reemplazarse en futuras entregas por interfaces visuales sin tocar el modelo de dominio.

---

## 8. Verificación y Pruebas

El sistema cuenta con una batería completa de pruebas unitarias desarrolladas en JUnit 5:

- **Recursos y Nave (`RecursosTest`, `NaveTest`, `NaveFactoryTest`):** Cobertura de límites [0, 100], atomicidad en `consumir`, umbral de mantenimiento en 80 y creación por fábrica.
- **Tripulación (`TripulacionTest`):** Cálculo de haberes por cargo, bonos por antigüedad, bonos de consejero, subsidios por planeta y composición encadenada del Decorator (caso de referencia Spock: 756 PG).
- **Motor Warp (`MotorWarpTest`):** Recorrido completo de la secuencia válida y captura de transiciones inválidas.
- **Misiones y Asistente (`MisionTest`, `AsistenteBaseTest`, `BitacoraTest`):** Cumplimiento del ciclo Template Method, inmutabilidad de eventos y generación de informes.
- **Escenarios (`PruebasEscenarios` / `EjecutarPruebas`):** Ejecución automática de las aserciones de los escenarios A, B, C, D y Centro de Control.

Todas las pruebas pasan limpiamente (20/20 aserciones de escenarios y 46 pruebas JUnit).

---

# Parte 2 — Universo Dinámico, Concurrencia, Persistencia e Interfaz Gráfica

## 9. Introducción a la Parte 2

La segunda parte continúa directamente sobre el modelo construido en la Entrega 1, sin reescribir el núcleo ni romper sus contratos. A la arquitectura en capas se incorporan:

- **Nave, recursos ampliados, elementos del universo y Creación MVC (`modelo.nave`, `modelo.elementos`, `modelo.fabrica`, `vista.CreacionPanel`, `controlador.CreacionController`):** gemas, recargas con tope y total, destrucción de la nave y su tripulación, obstáculos, estaciones, encuentros entre naves con **Double Dispatch** y pantalla de creación.
- **Extensión de tripulación y haberes (`modelo.tripulacion`, `vista.TripulantesPanel`):** estado vital (`estaVivo()` / `morir()`) y desglose estructurado de haberes (`ItemHaber`) apto para tablas Swing.
- **Motor Warp reescrito, tablero/Destino y radares (`modelo.motor`, `modelo.tablero`, `destinoytablero`, `modelo.navegacion`, `vista.TableroPanel`, `controlador.TableroController`):** máquina de estados según las reglas del Anexo, planificación BFS, radares de largo y corto alcance, integración del módulo oficial de la cátedra mediante **Adapter** y visualización del tablero en tiempo real.
- **Misiones en el universo dinámico, historial y panel (`modelo.misiones`, `vista.MisionesPanel`, `controlador.MisionController`):** bucle por turnos dentro del **Template Method**, informe con balance completo, historial en memoria y ejecución en hilo propio.
- **Asistente observable y concurrencia (`util.observer`, `modelo.asistente`, `modelo.concurrencia`, `vista.RecursosBitacoraPanel`, `vista.VisorPanel`):** notificaciones automáticas vía **Observer** y simulación **Productor–Consumidor con monitores** (`synchronized`, `while`, `wait()`, `notifyAll()`).
- **Control de acceso, persistencia e interfaz principal (`database/schema.sql`, `persistencia.*`, `servicio.*`, `vista.LoginPanel`, `vista.RegistroPanel`, `vista.VentanaPrincipal`, `controlador.AuthController`, `app.MainApp`):** registro/login con **DAO/DTO** sobre **JDBC + H2** y navegación modular con **MVC**.

El modelo de dominio continúa completamente desacoplado de Swing y de SQL.

---

## 10. Nave, Recursos Ampliados, Elementos del Universo y Creación

### Qué hace

Se ampliaron `Nave`, `Recursos` y `NaveFactory` para cubrir la travesía espacial y las nuevas condiciones de las tres misiones. En `Recursos` se agregaron las gemas recolectadas (`M-02`), la entrega atómica de combustible y energía para asistir a una nave aliada (`M-01`), la recarga con tope en 100 para estaciones de carga, la recarga total con desgaste a 0 al regresar a la base (`M-03`) y la condición de destrucción cuando el desgaste llega a 100 o el combustible o la energía quedan en 0. En `Nave` se incorporaron la posición actual, la base de origen, la verificación de tripulación mínima (1 capitán y 4 tripulantes adicionales vivos) y el estado de destrucción que provoca la muerte de toda la tripulación.

También se implementaron los elementos espaciales del tablero (`ElementoDelUniverso`, `NaveEnemiga`, `Asteroide` y `EstacionDeCarga`) con resolución de batallas mediante **Double Dispatch**, y la pantalla de Creación bajo **MVC** (`CreacionPanel` y `CreacionController`) para elegir el tipo de nave, instanciarla con la fábrica y asignarle los tripulantes.

### Archivos y responsabilidades

| Clase / Archivo | Responsabilidad |
|---|---|
| `Recursos`, `Nave`, `NaveExploradora`, `NaveCarguero`, `NaveCombate` | Amplían los suministros, la posición y el estado operativo de la nave, y reparten la tabla de batallas de 3×3 mediante Double Dispatch sin condicionales de tipo. |
| `NaveFactory` | Mantiene la creación desacoplada de los tres tipos de nave y suma `crearNaveEnemiga` para instanciar naves enemigas del tablero sin usar `new` de clases concretas. |
| `ElementoDelUniverso`, `ResultadoEncuentro`, `NaveEnemiga`, `Asteroide`, `EstacionDeCarga` | Modelan los encuentros durante la travesía: batallas entre naves, pérdida fija de energía por colisión con asteroides y recarga por monto fijo o porcentaje en estaciones. |
| `CreacionPanel`, `CreacionController` | Implementan la Sección de Creación con MVC, separando las validaciones de formato en la vista de las reglas del dominio en el controlador. |

### Reglas de encuentros (Double Dispatch 3×3)

| Nave Propia \ Nave Enemiga | Exploradora | Carguero | Combate |
|---|---|---|---|
| **Exploradora** | `PACTO` | `GANA` | `PIERDE` |
| **Carguero** | `PIERDE` | `PACTO` | `GANA` |
| **Combate** | `GANA` | `PIERDE` | `PACTO` |

### Por qué lo hicimos así

- **Double Dispatch sin condicionales:** cuando `NaveEnemiga` interactúa con la nave propia, invoca `navePropia.enfrentarseCon(naveEnemiga)`. En el primer despacho, la nave propia llama en la enemiga al método de su propio tipo (`lucharContraExploradora`, `lucharContraCarguero` o `lucharContraCombate`), y en el segundo despacho la clase concreta de la enemiga devuelve `GANA`, `PIERDE` o `PACTO`. Así la tabla de 3×3 queda distribuida polimórficamente sin ningún `if`, `switch` ni `instanceof`. Si el resultado es `PIERDE`, la nave propia se destruye inmediatamente y toda su tripulación muere.
- **Compatibilidad de `Recursos`:** se conservaron intactas las operaciones estrictas de la Entrega 1 (`cargarCombustible` y `cargarEnergia` que rechazan excesos sin cambios parciales) y se agregaron métodos específicos para la Entrega 2 (`recargarHastaElTope`, `recargaTotal` y `perderEnergiaHastaCero`) que saturan en los límites `[0, 100]` y notifican a los observadores.
- **Separación MVC en Creación:** `CreacionPanel` captura los eventos de los botones y valida el formato (ID y nombre no vacíos; antigüedad y consejos enteros válidos) antes de delegar en `CreacionController`. El controlador utiliza `NaveFactory` para crear la nave, asigna los `Tripulante` y verifica la regla de negocio de tripulación mínima (al menos 1 capitán y 4 tripulantes adicionales vivos) antes de habilitar misiones.

### Conexiones y dependencias

- `Nave` utiliza `Coordenada` (`modelo.navegacion`), `MotorWarp` (`modelo.motor`) y `Tripulante` (`modelo.tripulacion`).
- `NaveEnemiga`, `Asteroide` y `EstacionDeCarga` implementan `ElementoDelUniverso` y operan sobre `Nave` y `Recursos`.
- `CreacionController` coordina `CreacionPanel` con `NaveFactory`, `Nave` y `Tripulante`.

---

## 11. Extensión de Tripulación y Haberes

### Qué cambia respecto de la Entrega 1

Se conservaron intactas todas las reglas salariales y la estructura del patrón **Decorator** de la Entrega 1, agregando dos capacidades requeridas por la Parte 2:

1. **Estado vital del tripulante (`estaVivo()` / `morir()`):** todo `Tripulante` se instancia vivo (`vivo = true`) y pasa a fallecido mediante `morir()` cuando la nave es destruida durante una travesía o combate.
2. **Desglose estructurado para la vista (`ItemHaber` y `obtenerDesglose()`):** en la Entrega 1 el detalle salarial se exponía solo como un `String` multilínea (`obtenerDetalle()`). Para evitar que la interfaz gráfica tenga que parsear texto, se incorporó el objeto de valor inmutable `ItemHaber` (concepto y aporte en PG) y el método `List<ItemHaber> obtenerDesglose()` en `ConceptoHaber`.

### Archivos modificados y agregados (`modelo.tripulacion`)

| Clase | Responsabilidad |
|---|---|
| `ItemHaber` *(nueva)* | Value Object inmutable (`final`) que representa un renglón del recibo: `concepto` y `aporte` en PG. |
| `ConceptoHaber` | Suma `obtenerDesglose()` manteniendo `calcularTotal()` y `obtenerDetalle()`. |
| `HaberDecorator`, `SueldoBase`, `BonoAntiguedad`, `BonoConsejero`, `SubsidioOrigen` | Acumulan cada renglón de `ItemHaber` a través de la cadena Decorator y devuelven listas inmutables (`Collections.unmodifiableList`). |
| `Tripulante` | Agrega `estaVivo()`, `morir()`, soporte opcional de `cantidadConsejos` (constructor sobrecargado compatible con E1) y los métodos `obtenerHaber()`, `calcularHaberTotal()` y `obtenerDesgloseHaber()`. |

---

## 12. Motor Warp (State), Tablero/Destino (Adapter), Radares y Vista del Tablero

### Qué hace

El Motor Warp quedó reescrito según las reglas del Anexo: hacia afuera solo expone `saltar()` y `saltarWarp()`, mientras que las fases internas (`Disponible`, `PreparandoSalto`, `EnWarp` y `Enfriamiento`) resuelven qué pasa en cada turno aplicando el patrón **State**. El salto simple consume 2 de combustible y avanza una casilla. El salto Warp requiere 2 turnos de preparación verificando que haya 5 de energía (si no hay, sigue preparando sin avanzar), al saltar consume los 5 de energía y avanza dos casillas (o aborta y reinicia la preparación si perdió la energía), y luego cumple 1 turno de enfriamiento que suma 3 de desgaste antes de volver a `Disponible`.

También se modeló el tablero de 20×20, los radares de largo y corto alcance para calcular la ruta y escanear la siguiente casilla, el adaptador hacia el módulo oficial `destinoytablero` provisto por la cátedra, la vista en tiempo real del tablero con Swing y la ampliación de `CentroDeControl` para listar las naves registradas.

### Archivos y responsabilidades

| Clase / Paquete | Responsabilidad |
|---|---|
| `MotorWarp`, `EstadoMotor`, `EstadoBase`, `Disponible`, `PreparandoSalto`, `EnWarp`, `Enfriamiento` | Implementan el patrón State del motor con las nuevas reglas de turnos, energía, combustible y desgaste. |
| `SistemasDeLaNave`, `ResultadoDeTurno`, `ParametrosDelMotor` | Desacoplan al motor de la nave concreta, devuelven lo ocurrido en cada turno para la bitácora y centralizan costos y tiempos del Anexo. |
| `Tablero`, `TableroStub`, `InstantaneaDelTablero`, `ElementoDePrueba` | Definen la interfaz de grilla 20×20, una implementación de prueba reproducible con semilla fija y la copia inmutable para dibujar. |
| `destinoytablero.*` (`Tablero`, `Destino`, `Posicion`, `Observable`, `Observador`, excepciones) | Módulo oficial provisto por la cátedra integrado sin modificar su lógica. |
| `AdaptadorTableroDestino` (`modelo.tablero`) | Implementa el patrón **Adapter** entre `modelo.tablero.Tablero` y las clases `destinoytablero.Tablero` + `destinoytablero.Destino`, traduciendo coordenadas, turnos de `Destino`, consulta de alrededores 3×3 y notificaciones de `Observador` de forma segura para hilos. |
| `Coordenada`, `Ruta`, `PlanificadorDeRuta`, `RadarLargoAlcance`, `RadarCortoAlcance` | Calculan el camino más corto al destino mediante BFS ortogonal, guardan la ruta activa y escanean si la casilla siguiente está libre u ocupada. |
| `TableroPanel`, `TableroController`, `MainTableroSwing` | Dibujan el tablero en vivo suscribiéndose como `Observador` y permiten probar el viaje en una ventana independiente. |
| `CentroDeControl`, `MainDemo`, `EscenarioC`, `EscenarioNavegacion` | Agregan la consulta de asistentes registrados para los controladores y actualizan las demostraciones por consola al nuevo motor y a la navegación en el tablero. |

### Por qué lo hicimos así

- **Patrón State en el motor:** cada fase decide qué hacer cuando le piden `saltar()` o `saltarWarp()`, sin usar `if` ni `switch` por estado. Como ambas operaciones pueden invocarse en cualquier turno, cada estado devuelve un `ResultadoDeTurno` indicando el estado anterior, el estado nuevo, cuántas casillas avanzó y el mensaje para que el asistente lo registre en bitácora. El motor opera contra `SistemasDeLaNave` y conserva un constructor sin argumentos por defecto para mantener compatibilidad con la Entrega 1.
- **Interfaz `Tablero`, `TableroStub` y Patrón Adapter con `Destino`:** el dominio y la vista dependen siempre de `modelo.tablero.Tablero`, nunca de las clases concretas de la cátedra. `TableroStub` usa un `Random` con semilla fija para pruebas reproducibles, mientras que en ejecución real (`MainApp`) se conecta `AdaptadorTableroDestino`, que envuelve `destinoytablero.Tablero` y `destinoytablero.Destino` poblados con elementos reales (`Asteroide`, `EstacionDeCarga`, `NaveEnemiga`) sin alterar el código de la cátedra.
- **Radares:** `RadarLargoAlcance` calcula la ruta mínima con BFS ortogonal sobre la grilla y la almacena; turno a turno, `RadarCortoAlcance` inspecciona solo la casilla siguiente para decidir entre salto normal o Warp.
- **Concurrencia en la vista del tablero:** como el tablero cambia desde el hilo de la misión y Swing dibuja en el EDT, `TableroPanel` recibe el aviso del `Observador` y usa `SwingUtilities.invokeLater` para pedir una `InstantaneaDelTablero` (copia profunda inmutable de etiquetas y coordenadas) y repintar sin leer nunca la matriz viva del motor.

### Conexiones y dependencias

- `MotorWarp` depende solo de `SistemasDeLaNave`.
- Los radares, el planificador y `TableroPanel` dependen de la interfaz `modelo.tablero.Tablero`. Tanto `TableroStub` como `AdaptadorTableroDestino` extienden `util.observer.Sujeto`.
- `CentroDeControl` guarda instancias de `AsistenteDeComando` por su abstracción.

---

## 13. Misiones en el Universo Dinámico, Historial y Panel de Misiones

### Qué hace y alcance

El paquete `modelo.misiones` contiene las tres misiones obligatorias (`M-01` Intercepción y asistencia, `M-02` Recolección y `M-03` Retorno a punto seguro), el informe final (`InformeMision`) y el historial en memoria (`HistorialDeInformes`). En esta entrega las misiones operan dentro del universo dinámico: la nave viaja hacia un destino sobre el tablero, turno a turno, y reacciona a lo que encuentra. Las misiones no acceden directamente a la nave ni al tablero: todo lo piden a `AsistenteDeComando` (consumir recursos, calcular la ruta, escanear, saltar y avanzar el turno del tablero).

Para operar las misiones desde la interfaz gráfica se sumaron `MisionesPanel` y `MisionController`, que permiten elegir y lanzar misiones con destino aleatorio del tablero oficial o manual, correrlas en un hilo propio sin congelar Swing y consultar el historial completo de informes.

### Por qué usamos Template Method con hooks

Las tres misiones comparten un ciclo de vida de cuatro fases: `preparar` (verificar recursos mínimos y calcular la ruta), `ejecutar` (despegue y bucle de turnos con pausa `Thread.sleep` hasta llegar al destino), `evaluar` (armar el informe) y `cerrar` (aplicar la recompensa o entrega). Ese ciclo está escrito una sola vez en el método `ejecutarCiclo` de `Mision`, que es `final`, de modo que ninguna subclase puede alterar el orden.

Lo que cambia entre misiones se resuelve con métodos de enganche (*hooks*) que cada subclase completa: el destino, la energía de despegue (`5`, `10` y `0`, todos con 4 de combustible y 4 de desgaste según el Anexo), la verificación en destino (solo `M-01`), la recompensa final (transferencia de auxilio, gemas entre 20 y 100, o recarga total con desgaste en 0) y los textos del informe. Así el bucle de viaje vive en un único lugar y no se duplica en las tres clases.

### Contratos que cumple

- **Preparación previa:** `ejecutar` lanza `IllegalStateException` si la misión no fue preparada.
- **Cierre con informe:** `cerrar` lanza `IllegalStateException` si la misión no fue evaluada y no tiene informe.
- **Recursos insuficientes:** `preparar` verifica los recursos mínimos antes de calcular la ruta. Si no alcanzan, la misión no despega, deja la nave intacta y termina con un informe de fracaso que explica el motivo.
- **Destrucción de la nave:** si el desgaste llega a 100, o la energía o el combustible a 0, la misión falla de inmediato.
- **Tope de turnos:** una misión que no llega al destino en 200 turnos falla, evitando bucles infinitos (por ejemplo, si el Warp queda preparando sin energía).
- **Datos de entrada e invariantes:** los constructores rechazan valores fuera de rango (auxilio de 0 a 50, gemas de 20 a 100) y datos nulos; `verificarInvariantes()` valida el estado al finalizar cada fase. Los fallos normales del dominio quedan en el informe, reservando las excepciones para errores de programación.

### Informe de misión, historial y panel MVC

- **`InformeMision`:** guarda nombre, objetivo, resultado (éxito o fracaso), acciones realizadas, ruta realmente recorrida, balance de recursos (comparando inicio y fin: consumido, perdido y ganado), estado final de la nave y observaciones. Se crea en `evaluar` y se completa en `cerrar`.
- **`HistorialDeInformes`:** conserva los informes en memoria (no se persisten en base de datos), sincronizado para acceso concurrente, con copias inmutables y notificación vía `Sujeto`.
- **`MisionesPanel` y `MisionController`:** validan antes de despegar que la nave esté operativa con tripulación válida (`tieneTripulacionValida()`), garantizan una sola misión activa por vez (`AtomicBoolean`), ejecutan el viaje en un `Thread` dedicado (interrumpible al cancelar o cerrar la ventana) y refrescan la interfaz mediante `SwingUtilities.invokeLater`.

---

## 14. Asistente Observable, Panel de Recursos/Bitácora y Concurrencia (Visor del Universo Paralelo)

### Qué hace

El asistente quedó ampliado para que las misiones puedan consultar la posición de la nave, escanear una casilla y pedir un salto normal o Warp. El asistente pasa la orden al módulo de navegación (`NavegacionDeMision` / `NavegacionConTablero`), devuelve el resultado y registra lo ocurrido en la bitácora. Así la misión sabe si la nave avanzó, si el motor sigue preparando, si está enfriando o si el salto fue rechazado.

También se implementaron el panel de recursos y bitácora de la nave seleccionada, y el **Visor del Universo Paralelo**, donde en torno a una `PlataformaDeCarga` compartida de capacidad limitada (3 lugares) las generadoras depositan baterías y las naves consumidoras las retiran en tiempo real.

### Archivos y responsabilidades

| Clase / Archivo | Responsabilidad |
|---|---|
| `AsistenteDeComando`, `AsistenteBase`, `NavegacionDeMision`, `NavegacionConTablero` | Definen las operaciones de travesía y delegan órdenes de navegación, escaneo, salto e interacción con elementos. |
| `PosicionNave`, `ResultadoEscaneo`, `ResultadoSalto` | Representan la posición y los resultados estructurados que recibe la misión. |
| `util.observer.Sujeto`, `util.observer.Observador`, `Bitacora`, `Recursos` | Implementan el patrón Observer propio (`java.util.Observable` está obsoleto), permitiendo suscribirse a eventos de bitácora y cambios de recursos. |
| `RecursosBitacoraPanel`, `RecursosBitacoraController` | Muestran los datos en vivo de la nave seleccionada y actualizan las suscripciones al cambiar de nave. |
| `PlataformaDeCarga`, `GeneradoraDeEnergia`, `NaveConsumidora`, `UniversoParalelo` | Modelan el monitor compartido con capacidad limitada, los hilos productores/consumidores creados en vivo y el cierre ordenado de hilos. |
| `VisorPanel`, `VisorController`, `MainVisor` | Conectan los dos botones de alta en vivo y la consola de eventos del visor (`MainVisor` permite abrirlo por separado). |

### Por qué lo hicimos así

- **Patrón Observer hacia Swing:** cuando cambia un recurso o se agrega un evento en la bitácora, el modelo notifica a los observadores registrados a través del asistente sin conocer nada de Swing. El controlador suscrito consulta los valores actualizados y actualiza el panel en el EDT mediante `SwingUtilities.invokeLater`.
- **Productor–Consumidor con Monitores:** cada `GeneradoraDeEnergia` y cada `NaveConsumidora` corre en su propio `Thread`. `PlataformaDeCarga` protege el acceso exclusivo con métodos `synchronized`, evalúa las condiciones de guarda (plataforma llena / plataforma vacía) dentro de bucles `while` llamando a `wait()` y, tras cada depósito o retiro, invoca `notifyAll()` para despertar a los hilos en espera y que vuelvan a comprobar la condición.

### Conexiones y dependencias

- Las misiones llaman a `AsistenteDeComando`; el asistente delega movimiento y escaneo a `NavegacionDeMision`.
- El visor corre de forma completamente independiente de las misiones y detiene ordenadamente sus hilos al cerrar la ventana.

---

## 15. Control de Acceso, Persistencia (DAO / DTO + JDBC) e Interfaz Principal (MVC)

### Qué hace

Implementa el registro e inicio de sesión de operadores antes de acceder a los paneles de control, la ventana principal con navegación por pestañas y la sección de Tripulantes con consulta del desglose salarial en vivo. Cumpliendo estrictamente el alcance del enunciado, **únicamente se persisten las credenciales de usuario** (nombre de usuario y contraseña hasheada); ninguna otra entidad del dominio se persiste en base de datos.

### Archivos y responsabilidades

| Clase / Archivo | Paquete / Ruta | Responsabilidad |
|---|---|---|
| `schema.sql` | `database/` | Script DDL idempotente (`CREATE TABLE IF NOT EXISTS usuarios`) con clave primaria autoincremental y restricción `UNIQUE` sobre `nombre_usuario`. |
| `UsuarioDTO` | `persistencia.dto` | Data Transfer Object inmutable para transportar `id`, `nombreUsuario` y `passwordHash` entre capas. Su `toString()` omite el hash para no exponerlo en logs. |
| `UsuarioDAO`, `UsuarioDAOJdbc`, `ConexionBD`, `PersistenciaException` | `persistencia.dao` | Contrato DAO y su implementación JDBC sobre H2 con `PreparedStatement` y `try-with-resources`. Encapsula `SQLException` en `PersistenciaException` y no contiene reglas de negocio. |
| `AutenticacionService`, `HashContrasena` y excepciones propias | `servicio` | Aplica reglas de negocio (unicidad, longitudes mínimas, validación de credenciales), deriva contraseñas con `PBKDF2WithHmacSHA256` y sal aleatoria de 16 bytes usando `javax.crypto` del JDK y lanza `UsuarioDuplicadoException` / `CredencialesInvalidasException`. |
| `LoginPanel`, `RegistroPanel`, `AuthController` | `vista`, `controlador` | Flujo MVC de acceso: las vistas validan formato básico y delegan `ActionListener` en `AuthController`, que invoca al servicio y coordina errores o ingreso al sistema. |
| `TripulantesPanel` | `vista` | Tabla no editable con datos, estado (`Vivo` / `Fallecido`) y salario total de cada tripulante, más panel de desglose reutilizando `obtenerDesgloseHaber()` del Decorator de E1. |
| `VentanaPrincipal`, `MainApp` | `vista`, `app` | `JFrame` con `CardLayout` (Login / Registro / Sistema) y `JTabbedPane` modular (`setPanelCreacion`, `setPanelControlNave`, `setPanelVisor`), más el punto de entrada gráfico que ensambla toda la aplicación y detiene los hilos ordenadamente al cerrar. |

### Decisiones de diseño

- **Patrones DAO y DTO + Inversión de Dependencias:** `AutenticacionService` depende solo de la interfaz `UsuarioDAO`. Cambiar el motor de BD o usar H2 en memoria en pruebas no impacta ni al servicio ni a la interfaz gráfica.
- **Elección de H2 embebida:** declarada en `pom.xml` (`com.h2database:h2`), persiste en archivo local (`./data/tpg2026`, excluido en `.gitignore`) sin exigir instalación de un servidor externo, y soporta modo en memoria (`jdbc:h2:mem:...`) para pruebas aisladas.
- **Seguridad:** consultas parametrizadas con `PreparedStatement` (prevención de SQL Injection), hash PBKDF2 + sal comparado en tiempo constante (`MessageDigest.isEqual`) y mensaje genérico ante fallo de login.
- **MVC y modularidad:** cada pestaña es un `JPanel` independiente con su controlador, sin que el modelo importe Swing, y toda actualización desde hilos secundarios pasa por `SwingUtilities.invokeLater`.

---

## 16. Verificación Integral de la Entrega 2 (Escenarios V1 a V12)

Todas las pruebas unitarias, escenarios de regresión y verificaciones de integración se ejecutaron y pasaron en su totalidad:

| ID | Escenario verificado | Clase de verificación | Resultado |
|---|---|---|---|
| **V1** | Registro, duplicado rechazado, login correcto/incorrecto y persistencia real en disco al reiniciar | `persistencia.AutenticacionTest` (JUnit 5) | 5/5 OK |
| **V2–V4** | `M-01` (transferencia en cierre), `M-02` (gemas 20–100) y `M-03` (recarga total y desgaste 0) con historial | `controlador.VerificacionMisionesPanel` | OK |
| **V5–V6** | Falla en `preparar` por recursos insuficientes sin cambios parciales y falla al llegar sin auxilio | `controlador.VerificacionMisionesPanel` | OK |
| **V7** | Ciclo completo Motor Warp (preparación, sin energía, aborto, enfriamiento) y API pública de 2 métodos | `pruebas.PruebasMotorWarp` / `EscenarioC` | OK |
| **V8–V9** | Las 9 combinaciones de Double Dispatch (3×3), asteroide hasta energía 0, estación con tope y destrucción | `modelo.elementos.VerificacionElementosYCreacion`, `NaveTest`, `RecursosTest` | OK |
| **V10** | Plataforma de carga con monitores (`while`, `wait`, `notifyAll`), espera llena/vacía, 300 baterías y alta en vivo | `modelo.concurrencia.VerificacionPlataformaDeCarga`, `controlador.VerificacionVisor` | OK |
| **V11** | Observer y GUI en vivo sin congelar la ventana (bitácora, recursos, tablero oficial adaptado y visor) | `VerificacionBitacoraObservable`, `PruebasVista`, `PruebasAdaptadorDestino` | OK |
| **V12** | Regresión de Entrega 1 adaptada a la nueva API (Escenarios A–D, Centro de Control y Haberes Decorator) | `app.MainDemo`, `pruebas.EjecutarPruebas`, `HaberesTest`, `NaveFactoryTest` | OK |

**Resumen numérico de pruebas:** 63/63 pruebas JUnit 5 (`NaveFactoryTest`, `NaveTest`, `RecursosTest`, `HaberesTest`, `AutenticacionTest`), 51/51 aserciones en `pruebas.EjecutarPruebas` (incluyendo las 5 pruebas de `PruebasAdaptadorDestino` sobre el módulo oficial de la cátedra) y 6/6 suites de verificación pasando sin errores.
