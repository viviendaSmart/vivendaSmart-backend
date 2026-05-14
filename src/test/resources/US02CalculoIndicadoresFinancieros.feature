Feature: US02 Cálculo de indicadores financieros
  Como asesor
  quiero que el simulador calcule automáticamente la TCEA, TIR y VAN
  para demostrarle al cliente el costo real y la viabilidad del crédito.

  Scenario Outline: Visualización automática de indicadores
    Given el <asesor> ha ejecutado una simulación exitosa.
    When el sistema termine de cargar el cronograma del <cliente>.
    Then el sistema mostrará un panel resumen con los valores de <tcea>, <tir> y <van>.

    Examples:
      | asesor | cliente    | tcea   | tir    | van      |
      | Rafael | Juan Perez | 12.5%  | 1.05%  | 15400.20 |

  Scenario Outline: Recálculo de indicadores por modificación de plazo
    Given el <asesor> está visualizando una simulación con un <plazo_original>.
    When modifique el plazo a un <nuevo_plazo> y vuelva a simular.
    Then el sistema actualizará en tiempo real los valores de <tcea_nueva> y <van_nuevo>.

    Examples:
      | asesor | plazo_original | nuevo_plazo | tcea_nueva | van_nuevo |
      | Rafael | 240 meses      | 180 meses   | 11.8%      | 12000.50  |