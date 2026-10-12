# Informe de Diseño y Arquitectura — Entrega 1

**Proyecto:** TPG 2026 — Software embarcado de una nave interestelar tripulada  
**Entrega:** Entrega 1 (Modelo de dominio, patrones de diseño y escenarios de demostración)

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
