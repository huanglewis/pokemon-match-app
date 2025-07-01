package iss.nus.edu.sg.fragments.workshop.pokemonmemorygame

import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PokemonAdapter(
    private val drawables: List<Int>,
    private val onSelect: (Int) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.ViewHolder>() {

    private val selected = mutableSetOf<Int>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = drawables.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setImageResource(drawables[position])
        holder.image.alpha = if (selected.contains(position)) 0.5f else 1f

        holder.image.setOnClickListener {
            if (selected.contains(position)) {
                selected.remove(position)
            } else if (selected.size < 6) {
                selected.add(position)
            }
            onSelect(selected.size)
            notifyDataSetChanged()
        }
    }

    fun getSelectedDrawables() = selected.map { drawables[it] }
}
