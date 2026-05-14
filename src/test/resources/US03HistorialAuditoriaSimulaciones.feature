Feature: US03 Historial y auditoría de simulaciones
  Como asesor
  quiero consultar un registro histórico de las simulaciones realizadas
  para recuperar y comparar cotizaciones pasadas para mis clientes.

  Scenario Outline: Explorar el registro histórico
    Given el <asesor> desea revisar las cotizaciones pasadas.
    And se encuentra en el dashboard principal.
    When seleccione el apartado de "Historial" en el SideBar.
    Then el sistema mostrará una tabla con la <fecha>, el <cliente> y el <monto_financiado>.

    Examples:
      | asesor | fecha      | cliente      | monto_financiado |
      | Rafael | 10/05/2026 | Juan Perez   | 250000           |
      | Rafael | 12/05/2026 | Maria Garcia | 180000           |

  Scenario Outline: Recuperar el detalle de una simulación guardada
    Given el <asesor> se encuentra en el apartado de "Historial".
    When seleccione el icono de "Ver Detalle" de una <simulacion_id>.
    Then el sistema cargará en modo de solo lectura el <cronograma> y los <indicadores>.

    Examples:
      | asesor | simulacion_id | cronograma     | indicadores     |
      | Rafael | SIM-001       | Cronograma_001 | TCEA 12%, VAN.. |