# Contrato inicial del asistente

Este contrato corresponde a la parte de Agustín. Es una propuesta para conectar
Nave, Motor Warp, Recursos y Misiones; debe acordarse con los responsables de esos
componentes antes de implementar `AsistenteBase`.

## Qué representa cada tipo

- `AsistenteDeComando`: interfaz que declara las operaciones disponibles. El centro
  de control y las misiones dependen de este tipo.
- `ResumenRecursos`: copia inmutable de los datos de recursos al consultar. La
  indicación de mantenimiento se obtiene del subsistema de recursos.
- `Resultado`: datos inmutables de éxito o rechazo, con un mensaje explicativo.
- `Evento`: fecha y descripción inmutables de un acontecimiento.
- `Bitacora`: conserva los eventos en orden temporal y devuelve copias de solo lectura.

La interfaz declara los métodos; todavía no hay una clase que implemente sus
operaciones sobre una nave. `Nave`, `Recursos` y `MotorWarp` tampoco están presentes
en el proyecto al crear este contrato. Los tipos de apoyo permiten compilar la
interfaz y desarrollar la bitácora de forma independiente.

## Operaciones y responsabilidades

| Método | Datos o resultado | Responsable del comportamiento |
|---|---|---|
| `getNaveId()` | Identidad de la nave | Nave |
| `consultarEstado()` | Nombre del estado del motor | Motor Warp |
| `consultarRecursos()` | `ResumenRecursos` | Recursos; el asistente copia sus datos |
| `consumir(combustible, energia, desgaste)` | `Resultado` | Recursos; consumo y desgaste sin cambios parciales |
| `cargarCombustible(cantidad)` | `Resultado` | Recursos |
| `cargarEnergia(cantidad)` | `Resultado` | Recursos |
| `realizarMantenimiento()` | `Resultado` | Nave/Recursos, según el contrato que publique Eneas |
| `prepararSalto()` | `Resultado` | Motor Warp |
| `saltar()` | `Resultado` | Motor Warp |
| `enfriar()` | `Resultado` | Motor Warp |
| `terminarEnfriamiento()` | `Resultado` | Motor Warp |
| `registrarEvento(descripcion)` | Agrega un evento | Bitácora |
| `consultarBitacora()` | Copia de solo lectura de `List<Evento>` | Bitácora |

Los parámetros inválidos se rechazan con `IllegalArgumentException`. Las órdenes
con parámetros válidos que un subsistema no pueda realizar deben producir un
`Resultado` de rechazo y registrar el motivo. La futura implementación del
asistente capturará las excepciones de dominio de los subsistemas; esta primera
etapa no implementa todavía esa captura ni la jerarquía de excepciones.

La interfaz y las clases de datos no imprimen ni leen de consola.

## Relación con el diagrama UML del grupo

Se tomó como referencia `diagrama_uml_tpg_e1.svg`. El contrato inicial propone
estos ajustes, que deben reflejarse en el diagrama cuando el grupo los acuerde:

- La consulta devuelve `ResumenRecursos`, en lugar del objeto mutable `Recursos`.
- El consumo incluye el desgaste para que el costo de una operación se aplique
  completo o se rechace sin cambios parciales. Dos llamadas independientes a
  `consumir` y `desgastar` podrían dejar un cambio parcial.
- La consulta de bitácora devuelve una lista de solo lectura, en lugar de exponer
  la bitácora interna con capacidad de registrar eventos.
- Se usa `registrarEvento` como nombre explícito de la operación del asistente.
- Se incluyen carga, mantenimiento y enfriamiento para poder operar los subsistemas
  a través del asistente.

## Acuerdos pendientes para la implementación

1. Eneas: firmas de Nave y Recursos, y una operación que aplique combustible,
   energía y desgaste juntos sin cambios parciales.
2. Joaquín: excepciones de transición inválida y momento de finalización del salto.
   La aclaración pide volver a Disponible cuando termina el salto; también debe
   poder demostrarse la secuencia por Enfriamiento. El motor conserva esa decisión.
3. Tomás: uso de los resultados para detener la misión ante un rechazo, registrar
   sus etapas y resultados, y generar el informe.
4. Grupo: aprobar las firmas propuestas y actualizar el diagrama UML.

## Verificación reproducible

`VerificacionBitacora` comprueba eventos inválidos, conservación de registros ante
rechazos, orden temporal con fechas iguales, consultas de solo lectura, copias
independientes de registros posteriores y conservación de la fecha del evento.

Es una demostración con `main` y comprobaciones explícitas. Para ejecutarla con
Maven instalado, desde la raíz del proyecto:

```sh
mvn test-compile
java -cp target/classes:target/test-classes modelo.asistente.VerificacionBitacora
```

`mvn test` por sí solo no ejecuta esta demostración. No se agregaron bibliotecas
de pruebas ni se modificó el `pom.xml`.

### Verificación realizada

Se compilaron los 15 archivos Java presentes (incluidas las clases de Tripulación
y la demostración) con `javac --release 17 -Xlint:all -encoding UTF-8`, usando el
JDK 26 disponible. La compilación terminó sin errores ni advertencias. Se ejecutó
`modelo.asistente.VerificacionBitacora` y todos sus casos pasaron.

Maven no está disponible en esta terminal, por lo que todavía no se verificaron
los comandos `mvn`. Esta comprobación tampoco demuestra operaciones sobre una
nave: `AsistenteBase` y los subsistemas necesarios aún no están implementados.
