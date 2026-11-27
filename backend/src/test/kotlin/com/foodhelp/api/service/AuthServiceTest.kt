package com.foodhelp.api.service

import com.foodhelp.api.dto.LoginRequest
import com.foodhelp.api.dto.RegistroRequest
import com.foodhelp.api.exception.CredencialesInvalidasException
import com.foodhelp.api.exception.UsuarioYaExisteException
import com.foodhelp.api.models.Usuario
import com.foodhelp.api.repository.UsuarioRepository
import com.foodhelp.api.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {
    @Mock
    private lateinit var usuarioRepository: UsuarioRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtService: JwtService

    private val limitadorLogin =
        com.foodhelp.api.security.LimitadorIntentos(
            maxIntentos = 10,
            ventana = java.time.Duration.ofMinutes(5),
        )

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        limitadorLogin.limpiarTodo()
        authService = AuthService(usuarioRepository, passwordEncoder, jwtService, limitadorLogin)
    }

    @Test
    fun `comprobar que la contrasena se encripta antes de guardarse en el repositorio`() {
        val request =
            RegistroRequest(
                nombre = "Carlos Perez",
                correo = "carlos@example.com",
                contrasena = "clavePlana123",
            )
        val contrasenaHasheada = "\$2a\$10\$hashEncriptadoSimuladoSeguro"
        val jwtEsperado = "token.jwt.firmado"

        `when`(usuarioRepository.existsByCorreo(request.correo)).thenReturn(false)
        `when`(passwordEncoder.encode(request.contrasena)).thenReturn(contrasenaHasheada)
        `when`(usuarioRepository.save(any(Usuario::class.java))).thenAnswer { it.arguments[0] as Usuario }
        `when`(jwtService.generateToken(request.correo, mapOf("tv" to 0))).thenReturn(jwtEsperado)

        val response = authService.registrar(request)

        verify(passwordEncoder).encode("clavePlana123")

        val usuarioCaptor = ArgumentCaptor.forClass(Usuario::class.java)
        verify(usuarioRepository).save(usuarioCaptor.capture())

        val usuarioGuardado = usuarioCaptor.value
        assertEquals("Carlos Perez", usuarioGuardado.nombre)
        assertEquals("carlos@example.com", usuarioGuardado.correo)
        assertEquals(contrasenaHasheada, usuarioGuardado.contrasena)

        assertNotNull(response.token)
        assertEquals(jwtEsperado, response.token)
    }

    @Test
    fun `comprobar que se lanza excepcion de conflicto si el correo ya esta registrado`() {
        val request =
            RegistroRequest(
                nombre = "Laura Gomez",
                correo = "laura@example.com",
                contrasena = "claveSecreta456",
            )

        `when`(usuarioRepository.existsByCorreo(request.correo)).thenReturn(true)

        assertThrows<UsuarioYaExisteException> {
            authService.registrar(request)
        }

        verify(usuarioRepository, never()).save(any())
        verify(passwordEncoder, never()).encode(anyString())
        org.mockito.Mockito.verifyNoInteractions(jwtService)
    }

    @Test
    fun `login exitoso con credenciales validas retorna token JWT`() {
        val request = LoginRequest(correo = "carlos@example.com", contrasena = "clave123")
        val usuario =
            Usuario(
                nombre = "Carlos",
                contrasena = "hash-valido",
                correo = "carlos@example.com",
            )
        `when`(usuarioRepository.findByCorreo("carlos@example.com")).thenReturn(usuario)
        `when`(passwordEncoder.matches("clave123", "hash-valido")).thenReturn(true)
        `when`(jwtService.generateToken("carlos@example.com", mapOf("tv" to 0))).thenReturn("jwt-token-login")

        val response = authService.login(request)
        assertEquals("jwt-token-login", response.token)
    }

    @Test
    fun `login con correo inexistente lanza CredencialesInvalidasException`() {
        val request = LoginRequest(correo = "desconocido@example.com", contrasena = "clave123")
        `when`(usuarioRepository.findByCorreo("desconocido@example.com")).thenReturn(null)
        `when`(passwordEncoder.encode(anyString())).thenReturn("hash-ficticio")
        `when`(passwordEncoder.matches(anyString(), anyString())).thenReturn(false)

        assertThrows<CredencialesInvalidasException> {
            authService.login(request)
        }
    }

    @Test
    fun `login con contrasena incorrecta lanza CredencialesInvalidasException`() {
        val request = LoginRequest(correo = "carlos@example.com", contrasena = "clave-erronea")
        val usuario =
            Usuario(
                nombre = "Carlos",
                contrasena = "hash-valido",
                correo = "carlos@example.com",
            )
        `when`(usuarioRepository.findByCorreo("carlos@example.com")).thenReturn(usuario)
        `when`(passwordEncoder.matches("clave-erronea", "hash-valido")).thenReturn(false)

        assertThrows<CredencialesInvalidasException> {
            authService.login(request)
        }
    }
}
