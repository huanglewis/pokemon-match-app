package iss.nus.edu.sg.fragments.workshop.pokemonmemorygame

import android.view.*
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import iss.nus.edu.sg.fragments.workshop.pokemonmemorygame.R

data class Card(
    val imageRes: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

class CardAdapter(
    val cards: List<Card>,
    private val onCardClick: (Int) -> Unit
) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardImage: ImageView = view.findViewById(R.id.cardImage)

        init {
            view.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onCardClick(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = cards.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = cards[position]
        val imageRes = if (card.isFlipped || card.isMatched) card.imageRes else R.drawable.card_back
        holder.cardImage.setImageResource(imageRes)
    }
}
