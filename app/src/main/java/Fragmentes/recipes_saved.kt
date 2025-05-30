package Fragmentes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.recipeai.R

class SavedRecipesFragment : Fragment() {
    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        adapter = RecipeAdapter { recipe ->
            // Al hacer clic en una receta guardada
            val bundle = bundleOf("recipe" to recipe)
            findNavController().navigate(R.id.action_to_recipeFragment, bundle)
        }

        view.rv_saved_recipes.adapter = adapter
        view.rv_saved_recipes.layoutManager = LinearLayoutManager(context)

        loadSavedRecipes()

        return view
    }

    private fun loadSavedRecipes() {
        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "recipes-db"
        ).build()

        GlobalScope.launch {
            val recipes = db.recipeDao().getAll()
            withContext(Dispatchers.Main) {
                adapter.submitList(recipes)
            }
        }
    }
}