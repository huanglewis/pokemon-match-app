using Microsoft.EntityFrameworkCore;
using PokemonLeaderboardApi.Models;

namespace PokemonLeaderboardApi.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options)
        {
        }

        public DbSet<ScoreEntry> Scores { get; set; }

        public DbSet<User> Users { get; set; }
    }
}
