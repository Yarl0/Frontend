package Fragmentes

import Database.AppDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.adapters.Converters
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.TypeConverters
import com.bumptech.glide.Glide
import com.example.recipeai.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.jvm.internal.CompletedContinuation.context

class RecipeFragment : Fragment() {
    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelable<Recipe>("recipe")?.let { recipe ->
            binding.tvRecipeTitle.text = recipe.title
            binding.tvRecipeIngredients.text = recipe.ingredients.joinToString("\n")
            binding.tvRecipeSteps.text = recipe.steps

            Glide.with(this)
                .load(recipe.imageUrl)
                .into(binding.ivRecipeImage)

            binding.btnSaveRecipe.setOnClickListener {
                saveRecipeToDatabase(recipe)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
        return view
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveRecipeToDatabase(recipe: Recipe) {
        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "recipes-db"
        ).build()

        GlobalScope.launch {
            db.recipeDao().insert(recipe)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Receta guardada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
@Entity
data class Recipe(
    @PrimaryKey val id: String,
    val title: String,
    @TypeConverters(Converters::class)
    val ingredients: List<String>,
    val steps: String,
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)
)