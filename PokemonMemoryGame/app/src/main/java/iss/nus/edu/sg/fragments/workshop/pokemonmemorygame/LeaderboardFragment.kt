package iss.nus.edu.sg.fragments.workshop.pokemonmemorygame

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class LeaderboardFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var playAgainButton: Button
    private lateinit var notTopTextView: TextView
    private val client = OkHttpClient()
    private var hasPlayedMusic = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        listView = view.findViewById(R.id.leaderboardList)
        playAgainButton = view.findViewById(R.id.playAgainButton)
        notTopTextView = view.findViewById(R.id.notTop5Text)

        playAgainButton.setOnClickListener {
            AudioPlayerManager.stopMusic()
            findNavController().navigate(R.id.action_leaderboard_to_fetch)
        }

        fetchTopScores()
        return view
    }

    override fun onResume() {
        super.onResume()
        if (!hasPlayedMusic) {
            AudioPlayerManager.playMusic(requireContext(), R.raw.leaderboard_music)
            hasPlayedMusic = true
        }
    }

    private fun fetchTopScores() {
        val request = Request.Builder()
            .url("http://172.20.10.2:5079/api/leaderboard/top5")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LeaderboardFragment", "❌ Failed to fetch scores", e)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "❌ Failed to load scores", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    Log.e("LeaderboardFragment", "⚠️ Response failed: ${response.code}")
                    return
                }

                val scores = JSONArray(body)
                val parsedScores = mutableListOf<String>()
                val currentUser = SessionManager.loggedInUser ?: ""
                var userFound = false

                for (i in 0 until scores.length()) {
                    val obj = scores.getJSONObject(i)
                    val user = obj.getString("username")
                    val time = obj.getInt("timeInSeconds")
                    parsedScores.add("${i + 1}. $user - ${time}s")
                    if (user == currentUser) userFound = true
                }

                activity?.runOnUiThread {
                    listView.adapter = object : ArrayAdapter<String>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        parsedScores
                    ) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getView(position, convertView, parent) as TextView
                            val entry = parsedScores[position]
                            val user = entry.split(". ")[1].split(" -")[0]

                            if (user == currentUser) {
                                view.setTypeface(null, Typeface.BOLD)
                                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.poke_yellow))
                                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.poke_black))
                            } else {
                                view.setTypeface(null, Typeface.NORMAL)
                                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.gba_white))
                                view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                            }
                            return view
                        }
                    }

                    notTopTextView.visibility = if (userFound) View.GONE else View.VISIBLE
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasPlayedMusic = false
    }
}
