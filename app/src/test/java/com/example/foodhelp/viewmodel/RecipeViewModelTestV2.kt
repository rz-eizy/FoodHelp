package com.example.foodhelp.viewmodel

import com.example.foodhelp.data.Categoria
import com.example.foodhelp.data.Ingrediente
import com.example.foodhelp.data.Receta
import com.example.foodhelp.repository.RecetaRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTestV2 {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val repository = mockk<RecetaRepository>()
    private lateinit var receta1: Receta
    private lateinit var receta2: Receta
    private lateinit var demo: Receta
    @Before
    fun setup(){
        receta1 = Receta(
            id = 101L,
            nombre = "Test1",
            descripcion = "",
            tiempoPreparacion = 0,
            imagenUrl = "",
            instrucciones = "",
            ingredientes = listOf(Ingrediente(0,"",1.0,"")),
            categoria = Categoria(0,"")
        )
        receta2 = Receta(
            id = 100L,
            nombre = "Test2",
            descripcion = "",
            tiempoPreparacion = 0,
            imagenUrl = "",
            instrucciones = "",
            ingredientes = listOf(Ingrediente(0,"",1.0,"")),
            categoria = Categoria(0,"")
        )
        demo = Receta(
            id = 20L,
            nombre = "Demo",
            descripcion = "",
            tiempoPreparacion = 0,
            imagenUrl = "",
            instrucciones = "",
            ingredientes = listOf(Ingrediente(0,"",1.0,"")),
            categoria = Categoria(0,"")
        )
    }
    @Test
    fun buscarRecetasExito() = runTest {
        val recetas = listOf(receta1, receta2)

        coEvery { repository.findRecipeByName("pasta") } returns recetas

        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("pasta")
        advanceUntilIdle()

        val state = vm.uiState.value

        assert(state.recetasEncontradas == recetas)
        assert(state.navigateToRecipeId == 101L)
        assert(state.errorMessage == null)
    }
    @Test
    fun buscarRecetas_queryConEspacios_muestraError() =runTest {
        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("      ")
        advanceUntilIdle()

        val state = vm.uiState.value
        assert(state.errorMessage == "La busqueda no puede estar vacia")
        assert(state.recetasEncontradas.isEmpty())
        assert(!state.isLoading)
    }
    @Test
    fun onErrorHandled_limpiaElError() = runTest {
        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("   ")
        advanceUntilIdle()

        assert(vm.uiState.value.errorMessage != null)

        vm.onErrorHandled()
        assert(vm.uiState.value.errorMessage == null)
    }
    @Test
    fun buscarRecetas_sinResultados_muestraError() = runTest {
        coEvery { repository.findRecipeByName("pollo") } returns emptyList()

        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("pollo")
        advanceUntilIdle()

        val state = vm.uiState.value
        assert(state.recetasEncontradas.isEmpty())
        assert(state.errorMessage == "No se encontraron resultados.")
    }

    @Test
    fun buscarRecetas_excepcion_muestraError() = runTest {
        coEvery { repository.findRecipeByName("carne") } throws Exception("fallo X")

        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("carne")
        advanceUntilIdle()

        val state = vm.uiState.value
        assert(state.errorMessage!!.contains("Ocurrio un error al buscar"))
        assert(!state.isLoading)
    }
    @Test
    fun onNavigationHandled_limpiaNavegacion() = runTest {
        val recetas = listOf(demo)

        coEvery { repository.findRecipeByName("demo") } returns recetas
        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("demo")
        advanceUntilIdle()

        assert(vm.uiState.value.navigateToRecipeId == 20L)
        vm.onNavigationHandled()
        assert(vm.uiState.value.navigateToRecipeId == null)
    }
    @Test
    fun buscarRecetas_queryMayusMinus_devuelveResultados() = runTest {
        val recetas = listOf(receta1)

        coEvery { repository.findRecipeByName("PaStA") } returns recetas
        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("PaStA")
        advanceUntilIdle()

        val state = vm.uiState.value
        assert(state.recetasEncontradas == recetas)
        assert(state.errorMessage == null)
    }
    @Test
    fun buscarRecetas_siNuevaBusquedaFalla_mantieneResultadosPrevios() = runTest {
        coEvery { repository.findRecipeByName("carne") } returns listOf(receta1)
        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("carne")
        advanceUntilIdle()

        val previo = vm.uiState.value.recetasEncontradas
        coEvery { repository.findRecipeByName("pollo") } throws Exception("fallo")

        vm.buscarRecetas("pollo")
        advanceUntilIdle()

        val state = vm.uiState.value
        assert(state.errorMessage != null)
        assert(state.recetasEncontradas == previo)
    }
    @Test
    fun buscarRecetas_listaDesordenada_navegaAlPrimeroDeLaLista() = runTest {
        val recetas = listOf(receta2.copy(id = 200L), receta1.copy(id = 999L))
        coEvery { repository.findRecipeByName("xyz") } returns recetas

        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("xyz")
        advanceUntilIdle()

        val state = vm.uiState.value
        assert(state.navigateToRecipeId == 200L)
    }
    @Test
    fun buscarRecetas_finalizaCarga_isLoadingFalse() = runTest {
        coEvery { repository.findRecipeByName("tomate") } returns listOf(receta1)
        val vm = RecipeViewModel(repository)
        vm.dispatcher = mainDispatcherRule.testDispatcher

        vm.buscarRecetas("tomate")
        advanceUntilIdle()

        val state = vm.uiState.value
        assert(!state.isLoading)
    }
}