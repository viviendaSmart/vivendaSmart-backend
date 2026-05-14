Feature: US05 Catálogo y gestión de inmuebles
  Como asesor
  quiero registrar las propiedades disponibles en mi portafolio
  para poder vincularlas posteriormente a una solicitud de crédito.

  Scenario Outline: Registro de una propiedad al portafolio
    Given el <asesor> desea añadir una propiedad disponible al catálogo.
    And se encuentra en el apartado de "Inmuebles".
    When ingrese la <tasacion>, la <ubicacion> y los <metros_cuadrados>.
    And haga clic en el botón "Añadir Inmueble".
    Then el sistema le mostrará la <confirmacion_registro>.

    Examples:
      | asesor | tasacion | ubicacion   | metros_cuadrados | confirmacion_registro         |
      | Rafael | 300000   | La Molina   | 120              | Propiedad listada en catálogo |

  Scenario Outline: Fallar en el registro por metros cuadrados negativos
    Given el <asesor> intenta registrar una nueva propiedad.
    When escriba los <metros_negativos> en el formulario.
    Then el sistema le mostrará un <mensaje_error>.

    Examples:
      | asesor | metros_negativos | mensaje_error             |
      | Rafael | -85              | El monto no es válido     |