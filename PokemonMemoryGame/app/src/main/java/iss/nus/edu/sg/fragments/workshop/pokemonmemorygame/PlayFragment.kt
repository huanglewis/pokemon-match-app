package iss.nus.edu.sg.fragments.workshop.pokemonmemorygame

import android.os.*
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import iss.nus.edu.sg.fragments.workshop.pokemonmemorygame.databinding.FragmentPlayBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class PlayFragment : Fragment() {
    private lateinit var binding: FragmentPlayBinding
    private lateinit var cardAdapter: CardAdapter

    private var firstSelectedPos: Int? = null
    private var matchedPairs = 0
    private var isBusy = false

    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private var elapsedTime = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayBinding.inflate(inflater, container, false)

        // Play gameplay music
        AudioPlayerManager.playMusic(requireContext(), R.raw.play_fragment_music)

        val selected = PlayFragmentArgs.fromBundle(requireArguments()).selected
        val cards = (selected + selected).map { Card(it) }.shuffled()

        cardAdapter = CardAdapter(cards, ::onCardClick)
        binding.playGrid.layoutManager = GridLayoutManager(context, 3)
        binding.playGrid.adapter = cardAdapter

        updateMatchDisplay()
        updateTimerDisplay()
        startTimer()

        return binding.root
    }

    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                elapsedTime++
                handler.post { updateTimerDisplay() }
            }
        }, 1000, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun updateMatchDisplay() {
        binding.matchCountText.text = "Matches: $matchedPairs"
    }

    private fun updateTimerDisplay() {
        binding.timerText.text = "Time: ${elapsedTime}s"
    }

    private fun onCardClick(position: Int) {
        if (isBusy || position !in cardAdapter.cards.indices) return

        val card = cardAdapter.cards[position]
        if (card.isMatched || card.isFlipped) return

        card.isFlipped = true
        cardAdapter.notifyItemChanged(position)

        if (firstSelectedPos == null) {
            firstSelectedPos = position
        } else {
            val firstCard = cardAdapter.cards[firstSelectedPos!!]
            val secondCard = card

            if (firstCard.imageRes == secondCard.imageRes) {
                firstCard.isMatched = true
                secondCard.isMatched = true
                matchedPairs++
                updateMatchDisplay()

                if (matchedPairs == 6) {
                    stopTimer()
                    AudioPlayerManager.stopMusic()

                    val username = SessionManager.loggedInUser ?: "Unknown"
                    submitScore(username, elapsedTime)
                }

                firstSelectedPos = null
            } else {
                isBusy = true
                handler.postDelayed({
                    firstCard.isFlipped = false
                    secondCard.isFlipped = false
                    cardAdapter.notifyItemChanged(firstSelectedPos!!)
                    cardAdapter.notifyItemChanged(position)
                    firstSelectedPos = null
                    isBusy = false
                }, 1000)
            }
        }
    }

    private fun submitScore(username: String, timeInSeconds: Int) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("username", username)
            put("timeInSeconds", timeInSeconds)
        }

        val jsonStr = json.toString()
        Log.d("PlayFragment", "Submitting score: $jsonStr")

        val body = jsonStr.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://172.20.10.2:5079/api/leaderboard")
            .post(body)
            .build()

        Log.d("PlayFragment", "POST URL: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PlayFragment", "❌ Failed to submit: ${e.message}", e)
                handler.post {
                    Toast.makeText(requireContext(), "❌ Failed to submit score", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("PlayFragment", "Response code: ${response.code}")
                Log.d("PlayFragment", "Response body: $responseBody")

                handler.post {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "✅ Score submitted!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.leaderboardFragment)
                    } else {
                        Toast.makeText(requireContext(), "⚠️ Submission error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        stopTimer()
        AudioPlayerManager.stopMusic()
        super.onDestroyView()
    }
}
