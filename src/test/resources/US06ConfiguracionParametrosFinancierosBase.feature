Feature: US06 Configuración de parámetros financieros base
  Como asesor
  quiero configurar los parámetros financieros globales
  para que estos se apliquen por defecto en mis futuras simulaciones.

  Scenario Outline: Guardar configuración inicial
    Given el <asesor> desea estandarizar sus simulaciones futuras.
    And se encuentra en el apartado de "Configuración".
    When defina la <tasa_base> y el <tipo_moneda>.
    And presione el botón "Guardar Preferencias".
    Then el sistema almacenará los <datos_por_defecto>.

    Examples:
      | asesor | tasa_base | tipo_moneda | datos_por_defecto      |
      | Rafael | 10.5      | Soles       | Preferencias guardadas |

  Scenario Outline: Modificación de parámetros globales
    Given el <asesor> necesita actualizar las tasas del mercado.
    When sobrescriba la <tasa_actual> por una <tasa_nueva> y guarde los cambios.
    Then el sistema aplicará la nueva configuración a las <nuevas_simulaciones>.

    Examples:
      | asesor | tasa_actual | tasa_nueva | nuevas_simulaciones     |
      | Rafael | 10.5        | 11.2       | Aplicado a nuevas simul |