# Caudal  Gestión de Finanzas Personales para Android

Caudal es una aplicación nativa para Android desarrollada bajo un enfoque offline-first para el registro ágil de ingresos y gastos diarios, control de presupuestos mensuales y visualización analítica del comportamiento financiero personal.

---

## Características Principales

- Registro Ágil en 3 Toques: Modal inferior con teclado numérico personalizado, selector rápido de fecha, nota opcional y carrusel de categorías.
- Actividad y Filtros Inteligentes: Cálculo automático del balance mensual neto, búsqueda instantánea por nota o categoría, filtros por tipo (Todos / Gastos / Ingresos) y agrupación diaria.
- Gestión de Categorías Personalizadas: Creación, edición y eliminación protegida (integridad referencial frente a movimientos existentes) con selección entre 24 íconos vectoriales, paleta de colores y tope presupuestal individual.
- Resumen Visual (Gráfica de Dona): Distribución porcentual interactiva por categoría construida de forma nativa con Compose Canvas.
- Control de Presupuestos: Anillo principal para el límite mensual global y cuadrícula de mini-anillos individuales con alertas visuales preventivas (80% y 100%).
- Análisis Comparativo y Calendario Interactivo: Gráfica de barras de evolución de los últimos 6 meses y calendario mensual de gastos diarios con hoja de detalle al tocar cualquier fecha.
- Portabilidad y Personalización: Alternancia entre tema oscuro y claro, soporte multimoneda (COP $, USD US$, EUR €) y exportación del historial completo a formato CSV compatible con Excel (UTF-8 BOM + FileProvider).
- Privacidad, Seguridad y Recordatorios: Almacenamiento 100% local sin dependencia de servidores externos, bloqueo de acceso mediante huella digital o PIN del dispositivo (BiometricPrompt) y recordatorio diario nocturno programado con WorkManager.

---

## Arquitectura y Stack Tecnológico

El proyecto sigue la arquitectura recomendada por Google (MVVM + Clean Layering) con flujo de datos reactivo unidireccional:

- Lenguaje: Kotlin
- Interfaz de Usuario: Jetpack Compose + Material Design 3 + Navigation Compose
- Persistencia Local: Room Database (SQLite) con relaciones Embedded / Relation y Jetpack DataStore Preferences
- Asincronía y Reactividad: Kotlin Coroutines y StateFlow / Flow
- Tareas en Segundo Plano: WorkManager (CoroutineWorker)
- Seguridad: AndroidX Biometric (BiometricPrompt) y FileProvider para compartición segura de archivos
- Compilación: Gradle Kotlin DSL, KSP (Kotlin Symbol Processing) y Min SDK 26 (Android 8.0+)

### Estructura de Paquetes (com.empresa.caudal)

```text
com.empresa.caudal
├── data
│   ├── local          # Base de datos Room, DAOs, Entidades y DataStore
│   └── repository     # CaudalRepository (fuente única de verdad)
├── domain
│   └── model          # Modelos de dominio (TransactionType)
├── notifications      # DailyReminderWorker y ReminderScheduler
├── security           # BiometricAuthHelper
├── ui
│   ├── components     # NumericKeypad, DonutChart, BudgetRing, AppLockScreen
│   ├── navigation     # Screen, CaudalBottomBar y CaudalScaffold
│   ├── screens        # activity, summary, budget, analytics, categories, settings, transaction
│   ├── theme          # Paleta de colores, tipografía y tema Material 3
│   └── CaudalViewModel.kt
└── util               # CurrencyFormatter, DateUtils, IconMapper y CsvExporter