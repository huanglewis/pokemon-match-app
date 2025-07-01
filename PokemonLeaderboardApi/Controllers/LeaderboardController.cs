using Microsoft.AspNetCore.Mvc;
using PokemonLeaderboardApi.Data;
using PokemonLeaderboardApi.Models;

namespace PokemonLeaderboardApi.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class LeaderboardController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly ILogger<LeaderboardController> _logger;

        public LeaderboardController(AppDbContext context, ILogger<LeaderboardController> logger)
        {
            _context = context;
            _logger = logger;
        }

        [HttpPost]
        public async Task<IActionResult> SubmitScore([FromBody] ScoreEntry entry)
        {
            if (entry == null)
            {
                _logger.LogWarning("❌ Received null score entry.");
                return BadRequest("Score entry is null.");
            }

            if (string.IsNullOrWhiteSpace(entry.Username) || entry.TimeInSeconds <= 0)
            {
                _logger.LogWarning("❌ Invalid entry: Username='{Username}', Time={Time}",
                    entry.Username, entry.TimeInSeconds);
                return BadRequest("Invalid score data.");
            }

            entry.Timestamp = DateTime.UtcNow;

            _logger.LogInformation("✅ Score received: {Username} - {Time} seconds",
                entry.Username, entry.TimeInSeconds);

            try
            {
                _context.Scores.Add(entry);
                await _context.SaveChangesAsync();
                return Ok();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "🔥 Error saving score to database.");
                return StatusCode(500, "Internal server error.");
            }
        }

        [HttpGet("top5")]
        public ActionResult<IEnumerable<ScoreEntry>> GetTop5()
        {
            var top5 = _context.Scores
                .OrderBy(s => s.TimeInSeconds)
                .Take(5)
                .ToList();

            _logger.LogInformation("📊 Top 5 scores requested. Count: {Count}", top5.Count);

            return Ok(top5);
        }
    }
}
