Feature: US10 Flujo explicado en el Home Dashboard
  Como asesor
  quiero ver un panel principal que me guíe a través del flujo de la aplicación
  para entender rápidamente cómo operar y validar bonos.

  Scenario Outline: Visualización del flujo de pasos
    Given el <asesor> inicia sesión en ViviendaSmart.
    When visualice el panel principal en el <home_dashboard>.
    Then el sistema le mostrará <tarjetas_ordenadas> con los pasos secuenciales.

    Examples:
      | asesor | home_dashboard | tarjetas_ordenadas   |
      | Rafael | Vista Home     | 4 pasos secuenciales |

  Scenario Outline: Consulta rápida de límites de ingresos por bono
    Given el <asesor> necesita verificar la calificación de un prospecto.
    When revise la sección de requisitos en la <tarjeta_bono>.
    Then el sistema mostrará el <tope_ingreso> correspondiente.

    Examples:
      | asesor | tarjeta_bono | tope_ingreso |
      | Rafael | Bono AVN     | S/ 3,715     |
      | Rafael | Bono CSP     | S/ 2,706     |