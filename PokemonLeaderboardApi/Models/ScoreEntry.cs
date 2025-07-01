namespace PokemonLeaderboardApi.Models
{
    public class ScoreEntry
    {
        public int Id { get; set; }
        public string? Username { get; set; }
        public int TimeInSeconds { get; set; }
        public DateTime Timestamp { get; set; }
    }
}
