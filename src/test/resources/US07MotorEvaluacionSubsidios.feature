Feature: US07 Motor de evaluación de subsidios
  Como asesor
  quiero ver la elegibilidad del cliente para los subsidios del estado
  para aplicar el descuento correspondiente al capital financiado.

  Scenario Outline: Aplicación del Bono del Buen Pagador (BBP)
    Given el <asesor> está procesando una simulación para un inmueble de <precio_vivienda>.
    When el motor de simulación valide que califica para el <tipo_bono>.
    Then el sistema descontará el <monto_subsidio> del capital total.

    Examples:
      | asesor | precio_vivienda | tipo_bono | monto_subsidio |
      | Rafael | 250000          | BBP       | 19600          |

  Scenario Outline: Inmueble fuera del rango de subsidios
    Given el <asesor> selecciona un inmueble con <precio_alto>.
    When se ejecute el cálculo del crédito.
    Then el sistema indicará un <estado_subsidio> y financiará el 100%.

    Examples:
      | asesor | precio_alto | estado_subsidio        |
      | Rafael | 500000      | No aplica a subsidio   |