Feature: US01 Generación del cronograma de pagos
  Como asesor
  quiero ejecutar la simulación cruzando los datos del cliente, inmueble, configuración y bonos
  para generar un cronograma de amortización detallado cuota por cuota.

  Scenario Outline: Generación exitosa del cronograma
    Given el <asesor> quiere calcular el préstamo de su prospecto.
    And ha seleccionado previamente a un <cliente>, un <inmueble> y la <configuracion_base>.
    When seleccione el botón "Generar Simulación".
    Then el sistema procesará los datos y generará un <cronograma_detallado> mostrando <capital>, <intereses> y <cuota_total>.

    Examples:
      | asesor | cliente    | inmueble | configuracion_base | cronograma_detallado | capital | intereses | cuota_total |
      | Rafael | Juan Perez | Depa SJL | TEA 10%            | Cronograma_001       | 250000  | 1250.50   | 3200.00     |

  Scenario Outline: Intento de simulación con datos incompletos
    Given el <asesor> se encuentra en el módulo de Simulación.
    And aún no ha vinculado un <elemento_faltante> a la solicitud actual.
    When seleccione el botón "Generar Simulación".
    Then el sistema le mostrará un <mensaje_error>.

    Examples:
      | asesor | elemento_faltante | mensaje_error                                         |
      | Rafael | Inmueble          | Debe seleccionar un inmueble antes de continuar       |
      | Rafael | Cliente           | Debe seleccionar un cliente antes de continuar        |