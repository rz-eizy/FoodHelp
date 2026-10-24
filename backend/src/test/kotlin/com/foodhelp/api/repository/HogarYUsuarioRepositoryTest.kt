package com.foodhelp.api.repository

import com.foodhelp.api.models.Hogar
import com.foodhelp.api.models.Usuario
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.annotation.Rollback
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Rollback
class HogarYUsuarioRepositoryTest {
    @Autowired
    lateinit var hogarRepository: HogarRepository

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Test
    fun `Test 1 - guardar un Hogar genera un UUID no nulo`() {
        val hogar = Hogar(nombre = "Casa Principal", codigoHogar = "CODIGO-001")

        val guardado = hogarRepository.saveAndFlush(hogar)

        assertNotNull(guardado.id, "El UUID del Hogar no debe ser nulo tras persistirse")
    }

    @Test
    fun `Test 2 - guardar dos Hogares con el mismo codigoHogar lanza DataIntegrityViolationException`() {
        val hogar1 = Hogar(nombre = "Hogar Alfa", codigoHogar = "CODIGO-DUPLICADO")
        val hogar2 = Hogar(nombre = "Hogar Beta", codigoHogar = "CODIGO-DUPLICADO")

        hogarRepository.saveAndFlush(hogar1)

        assertThrows<DataIntegrityViolationException> {
            hogarRepository.saveAndFlush(hogar2)
        }
    }

    @Test
    fun `Test 3 - persistir un Usuario con hogar null es valido`() {
        val usuario = Usuario(nombre = "Ana García", contrasena = "secreto123", hogar = null)

        val guardado = usuarioRepository.saveAndFlush(usuario)

        assertNotNull(guardado.id, "El UUID del Usuario no debe ser nulo tras persistirse")
        assertNull(guardado.hogar, "El hogar del Usuario debe ser null")
    }
}
