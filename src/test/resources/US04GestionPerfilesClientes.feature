Feature: US04 Gestión de perfiles de clientes
  Como asesor
  quiero registrar, editar y consultar perfiles de clientes
  para evaluar su perfil de riesgo y capacidad de endeudamiento.

  Scenario Outline: Registro de un nuevo prospecto válido
    Given el <asesor> desea registrar un nuevo cliente en el sistema.
    And se encuentra en el apartado de "Clientes".
    When ingrese el <dni>, <ingresos> y <estado_civil> del cliente.
    And haga clic en "Guardar".
    Then el sistema le mostrará una <confirmacion>.

    Examples:
      | asesor | dni      | ingresos | estado_civil | confirmacion                 |
      | Rafael | 76543210 | 3500     | Soltero      | Cliente registrado con éxito |

  Scenario Outline: Fallar en el registro por ingresos inválidos
    Given el <asesor> intenta registrar un perfil financiero.
    When ingrese un valor de <ingresos_negativos> en el formulario.
    Then el sistema le mostrará un <mensaje_error>.

    Examples:
      | asesor | ingresos_negativos | mensaje_error                |
      | Rafael | -1500              | El monto no es válido        |