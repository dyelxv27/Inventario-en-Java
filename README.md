Sistema de Gestión de Inventarios con Análisis de Rendimiento.
Este proyecto es una aplicación integral desarrollada en Java para la administración eficiente de productos.Destaca por el uso de estructuras de datos personalizadas y un módulo de análisis comparativo de algoritmos.
Funcionalidad Destacada: Carga Masiva de Datos
El sistema permite la importación masiva de inventarios mediante archivos de texto o CSV. Esta función facilita la migración de datos y el testeo del sistema con grandes volúmenes de información.
Cómo cargar tu inventario:
Prepara un archivo (ej. inventario.txt) con el siguiente formato separado por comas:ID,Nombre,Categoría,Precio
Dentro de la aplicación, utiliza el botón "Cargar Archivo".
Selecciona tu archivo y el sistema procesará automáticamente cada entrada utilizando la Tabla Hash interna para asegurar búsquedas de tiempo constante O(1).
