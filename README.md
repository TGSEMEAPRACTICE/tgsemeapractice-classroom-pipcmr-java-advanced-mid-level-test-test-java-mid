[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/hxxXWQLA)

🚀 Java Technical Challenge: Multi-Source Data Processor
Objetivo: Implementar un módulo de procesamiento de datos que combine múltiples fuentes de forma eficiente.

Requerimientos Técnicos:

Modelo de Datos: Crea una clase Transaction con id, amount, currency y status.

Java Streams: Dada una lista de transacciones:

Filtra las que tengan un amount superior a 50.

Transforma el status a "PROCESSED" usando .map().

Obtén la suma total de los montos filtrados.

Asincronía (CompletableFuture): Simula una llamada a una API externa para validar cada transacción. Debe ejecutarse de forma no bloqueante (asíncrona) y devolver un mensaje de confirmación al terminar todas.

Patrón de Diseño (Strategy): Implementa el patrón Strategy para calcular diferentes comisiones según la moneda (USD tiene 2%, EUR tiene 1%, otras 5%).

Clean Code: El código debe ser legible, usar nombres de variables claros y manejar posibles excepciones (ej. montos negativos).

Entrega: Realiza los "commits" y "push" necesarios en este repositorio de GitHub Classroom antes de la hora acordada.
