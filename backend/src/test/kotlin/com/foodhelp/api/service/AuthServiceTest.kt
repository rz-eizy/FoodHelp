package com.foodhelp.api.service

import com.foodhelp.api.dto.RegistroRequest
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

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService(usuarioRepository, passwordEncoder, jwtService)
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
        `when`(jwtService.generateToken(request.correo)).thenReturn(jwtEsperado)

        val response = authService.registrar(request)

        // 1. Verifica que se invocó el encoder con la contraseña plana
        verify(passwordEncoder).encode("clavePlana123")

        // 2. Captura el usuario persistido y comprueba que tiene la contraseña hasheada y no en texto plano
        val usuarioCaptor = ArgumentCaptor.forClass(Usuario::class.java)
        verify(usuarioRepository).save(usuarioCaptor.capture())

        val usuarioGuardado = usuarioCaptor.value
        assertEquals("Carlos Perez", usuarioGuardado.nombre)
        assertEquals("carlos@example.com", usuarioGuardado.correo)
        assertEquals(contrasenaHasheada, usuarioGuardado.contrasena)

        // 3. Comprueba el retorno del JWT válido
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

        // Verifica que no se guarde nada en el repositorio ni se encripte
        verify(usuarioRepository, never()).save(any())
        verify(passwordEncoder, never()).encode(anyString())
        org.mockito.Mockito.verifyNoInteractions(jwtService)
    }
}
