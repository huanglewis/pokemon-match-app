package iss.nus.edu.sg.fragments.workshop.pokemonmemorygame

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import iss.nus.edu.sg.fragments.workshop.pokemonmemorygame.databinding.FragmentFetchBinding

class FetchFragment : Fragment() {

    private var _binding: FragmentFetchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PokemonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFetchBinding.inflate(inflater, container, false)

        // Start background music
        AudioPlayerManager.playMusic(requireContext(), R.raw.login_fragment_music)

        val pokemonDrawables = (1..151).map {
            resources.getIdentifier("pokemon_%03d".format(it), "drawable", requireContext().packageName)
        }

        adapter = PokemonAdapter(pokemonDrawables) { selectedCount ->
            binding.selectionCounter.text = "Selected: $selectedCount / 6"
            binding.startGameButton.isEnabled = selectedCount == 6

            // Play select sound
            if (selectedCount <= 6) {
                AudioPlayerManager.playSoundEffect(requireContext(), R.raw.select_image)
            }
        }

        binding.pokemonGrid.layoutManager = GridLayoutManager(context, 3)
        binding.pokemonGrid.adapter = adapter

        binding.startGameButton.setOnClickListener {
            // Stop background music
            AudioPlayerManager.stopMusic()

            // Play "game start" effect
            AudioPlayerManager.playSoundEffect(requireContext(), R.raw.game_start)

            val navController = binding.root.findNavController()
            val currentDestination = navController.currentDestination?.id
            Log.d("FetchFragment", "Current destination: $currentDestination")

            if (currentDestination == R.id.fetchFragment) {
                val selected = adapter.getSelectedDrawables().toIntArray()
                val action = FetchFragmentDirections.actionFetchFragmentToPlayFragment(selected)
                navController.navigate(action)
            } else {
                Log.w("FetchFragment", "Blocked navigation: not currently at fetchFragment")
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}