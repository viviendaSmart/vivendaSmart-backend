Feature: US09 Autenticación segura y control de acceso
  Como asesor
  quiero iniciar sesión en el sistema de forma segura
  para proteger la información financiera de mis clientes.

  Scenario Outline: Acceso exitoso al sistema
    Given el <asesor> se encuentra en la pantalla de Login.
    When ingrese su <correo> y su <contraseña> válida.
    And haga clic en "Ingresar".
    Then el sistema le redirigirá al <dashboard>.

    Examples:
      | asesor | correo            | contraseña | dashboard      |
      | Rafael | rafael@gmail.com  | pass123    | Home Dashboard |

  Scenario Outline: Acceso denegado por credenciales incorrectas
    Given el <asesor> se encuentra en la pantalla de Login.
    When ingrese un <correo> o <contraseña_erronea>.
    And haga clic en "Ingresar".
    Then el sistema le mostrará un <mensaje_error>.

    Examples:
      | asesor | correo            | contraseña_erronea | mensaje_error                     |
      | Rafael | rafael@gmail.com  | wrong_pass         | Usuario o contraseña incorrectos  |