Feature: US08 Definición de plantillas de costos
  Como asesor
  quiero establecer los costos periódicos e iniciales
  para garantizar que los cálculos mensuales sean exactos.

  Scenario Outline: Creación de costo periódico
    Given el <asesor> quiere incluir seguros obligatorios en la simulación.
    When defina un <tipo_seguro> como un <porcentaje_mensual> y lo guarde.
    Then el sistema mostrará la <confirmacion_costo>.

    Examples:
      | asesor | tipo_seguro | porcentaje_mensual | confirmacion_costo        |
      | Rafael | Desgravamen | 0.05               | Seguro agregado a cuotas  |

  Scenario Outline: Creación de costo inicial
    Given el <asesor> desea reflejar los gastos de desembolso.
    When defina un <gasto_fijo> como un <monto_inicial>.
    Then el sistema lo registrará únicamente en la <cuota_aplicada>.

    Examples:
      | asesor | gasto_fijo      | monto_inicial | cuota_aplicada            |
      | Rafael | Gasto Notarial  | 150           | Registrado en Cuota 0     |