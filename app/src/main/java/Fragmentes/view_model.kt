package Fragmentes

import Database.AppDatabase
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).recipeDao()

    val allRecipes: LiveData<List<Recipe>> = dao.getAll().asLiveData()

    fun insert(recipe: Recipe) {
        val viewModelScope = null
        viewModelScope.launch {
            dao.insert(recipe)
        }
    }
}