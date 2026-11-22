# Diagramas de Arquitectura y Patrones — Entrega 2

Estos diagramas complementan el diagrama general de la Entrega 1 ([`diagrama_uml_tpg_e1.png`](diagrama_uml_tpg_e1.png)) y el diagrama PlantUML actualizado de la Entrega 2 ([`diagrama_uml_tpg_e2.puml`](diagrama_uml_tpg_e2.puml)), aislando los patrones y flujos clave solicitados para la segunda etapa y la exposición.

---

## 1. Arquitectura MVC + Observer (Interfaz Gráfica y Capas)

```mermaid
flowchart LR
    subgraph VISTA["Vista (Swing — EDT)"]
        VP["VentanaPrincipal (CardLayout + JTabbedPane)"]
        LP["LoginPanel / RegistroPanel"]
        CP["CreacionPanel"]
        TP["TripulantesPanel"]
        TBP["TableroPanel"]
        RBP["RecursosBitacoraPanel"]
        MP["MisionesPanel"]
        VISP["VisorPanel"]
    end

    subgraph CTRL["Controladores (MVC)"]
        AC["AuthController"]
        CC["CreacionController"]
        TC["TableroController"]
        RBC["RecursosBitacoraController"]
        MC["MisionController (Thread propio)"]
        VC["VisorController"]
    end

    subgraph MOD["Modelo y Servicio (sin Swing ni SQL)"]
        AS["AutenticacionService"]
        NF["NaveFactory / Nave / Tripulante / Elementos"]
        AD["AdaptadorTableroDestino -> destinoytablero"]
        MW["MotorWarp (State) / Radares"]
        MIS["Mision M-01..M-03 (Template Method) / HistorialDeInformes"]
        ASI["AsistenteBase / Bitacora / Recursos (Sujeto)"]
        CONC["UniversoParalelo / PlataformaDeCarga (Monitor)"]
    end

    subgraph PERS["Persistencia"]
        DAO["UsuarioDAO / UsuarioDAOJdbc"]
        H2[("H2 Embebida (usuarios)")]
    end

    LP -- "ActionListener" --> AC --> AS -- "UsuarioDTO" --> DAO --> H2
    CP -- "ActionListener" --> CC --> NF
    TBP --- TC --> AD
    MP -- "ActionListener" --> MC --> MIS --> ASI
    ASI --> MW
    ASI --> AD
    ASI -. "Observer + invokeLater" .-> RBC --> RBP
    AD -. "Observer + invokeLater" .-> TBP
    VISP -- "ActionListener" --> VC --> CONC
    CONC -. "Observer + invokeLater" .-> VISP
```

---

## 2. Secuencia — Double Dispatch en Encuentros entre Naves

```mermaid
sequenceDiagram
    participant Mis as Mision (ejecutar)
    participant Asi as AsistenteBase
    participant Ene as NaveEnemiga
    participant Propia as NaveExploradora (propia)
    participant Riv as NaveCombate (enemiga)

    Mis->>Asi: interactuarConElementoActual()
    Asi->>Ene: interactuarCon(navePropia)
    Ene->>Propia: enfrentarseCon(naveEnemiga)
    Note over Propia: 1er despacho polimorfico (tipo de nave propia)
    Propia->>Riv: lucharContraExploradora(this)
    Note over Riv: 2do despacho polimorfico (tipo de nave enemiga)
    Riv-->>Propia: ResultadoEncuentro.PIERDE
    Propia->>Propia: destruir() (mata a toda la tripulacion)
    Propia-->>Ene: PIERDE
    Ene-->>Asi: descripcion del encuentro
    Asi-->>Mis: evento registrado en bitacora (mision falla)
```

---

## 3. Secuencia — Concurrencia Productor–Consumidor (`wait()` / `notifyAll()`)

```mermaid
sequenceDiagram
    participant Gen as GeneradoraDeEnergia (Thread)
    participant Plat as PlataformaDeCarga (Monitor synchronized)
    participant Nav as NaveConsumidora (Thread)
    participant Vis as VisorPanel (Observador / EDT)

    Gen->>Plat: depositar(generadora)
    Note over Plat: synchronized — capacidad llena (3/3)
    Plat-.->Vis: actualizar("Generadora espera espacio") via invokeLater
    Gen->>Plat: while (llena) wait() [libera monitor]

    Nav->>Plat: retirar(nave)
    Note over Plat: synchronized — toma monitor, retira bateria (2/3)
    Plat->>Plat: notifyAll() [despierta hilos en espera]
    Plat-.->Vis: actualizar("Nave retiro bateria 2/3") via invokeLater
    Nav-->>Nav: consume bateria

    Note over Gen,Plat: Generadora despierta, reevalua while (!llena) y deposita (3/3)
    Gen->>Plat: deposita bateria + notifyAll()
    Plat-.->Vis: actualizar("Generadora deposito 3/3") via invokeLater
```

---

## 4. Secuencia — Control de Acceso MVC + Persistencia DAO/DTO

```mermaid
sequenceDiagram
    actor Operador
    participant Vista as LoginPanel (Vista)
    participant Ctrl as AuthController (Controlador)
    participant Serv as AutenticacionService (Servicio)
    participant DAO as UsuarioDAOJdbc (DAO)
    participant Win as VentanaPrincipal

    Operador->>Vista: Ingresa usuario y contraseña y pulsa Iniciar sesion
    Vista->>Ctrl: ActionListener -> manejarLogin()
    Ctrl->>Vista: validarFormato()
    Vista-->>Ctrl: true
    Ctrl->>Serv: iniciarSesion(usuario, password)
    Serv->>DAO: buscarPorNombre(usuario)
    DAO-->>Serv: Optional de UsuarioDTO

    alt Hash PBKDF2 coincide
        Serv-->>Ctrl: UsuarioDTO
        Ctrl->>Win: mostrarSistema(usuarioDTO)
        Win-->>Operador: Abre pestañas del sistema
    else Usuario inexistente o clave invalida
        Serv--xCtrl: CredencialesInvalidasException
        Ctrl->>Vista: mostrarError("Usuario o contraseña incorrectos.")
    end
```

---

## 5. UML Enfocado — Tablero Oficial (Adapter), Motor Warp (State) y Misiones (Template Method)

```mermaid
classDiagram
    class Tablero {
        <<interface>>
        +DIMENSION = 20
        +esValida(c) boolean
        +getElementoEn(c) ElementoDePrueba
        +moverElementos() void
        +moverNave(c) void
        +copiaParaDibujar() InstantaneaDelTablero
    }

    class AdaptadorTableroDestino {
        -destinoytablero.Tablero tableroCatedra
        -destinoytablero.Destino destinoCatedra
        +generarObjetivoAleatorio() Coordenada
        +moverElementos() void
        +moverNave(c) void
        +copiaParaDibujar() InstantaneaDelTablero
    }

    class DestinoCatedra["destinoytablero.Destino"] {
        +turnoDestino() void
    }

    class TableroCatedra["destinoytablero.Tablero"] {
        +moverNave(p) void
        +inspeccionarPosicion() ElementoDelUniverso
        +observarAlrededores() ElementoDelUniverso[][]
        +obtenerObjetivo() Posicion
        +verTablero() ElementoDelUniverso[][]
    }

    class MotorWarp {
        -EstadoMotor estado
        +saltar() ResultadoDeTurno
        +saltarWarp() ResultadoDeTurno
    }

    class EstadoMotor {
        <<interface>>
        +saltar(motor) ResultadoDeTurno
        +saltarWarp(motor) ResultadoDeTurno
    }

    class Mision {
        <<abstract>>
        +ejecutarCiclo(a) InformeMision
        #preparar(a) void
        #ejecutar(a) void
        #evaluar(a) void
        #cerrar(a) void
        #determinarDestino(a)* PosicionNave
        #aplicarRecompensa(a)* void
    }

    Tablero <|.. AdaptadorTableroDestino
    AdaptadorTableroDestino --> TableroCatedra : adapta
    AdaptadorTableroDestino --> DestinoCatedra : delega turnos
    DestinoCatedra --> TableroCatedra
    MotorWarp --> EstadoMotor : delega (State)
    Mision <|-- Mision1
    Mision <|-- Mision2
    Mision <|-- Mision3
```
