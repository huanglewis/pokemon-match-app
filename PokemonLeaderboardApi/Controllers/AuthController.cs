using Microsoft.AspNetCore.Mvc;
using PokemonLeaderboardApi.Data;
using PokemonLeaderboardApi.Models;
using System.Linq;

namespace PokemonLeaderboardApi.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly AppDbContext _context;

        public AuthController(AppDbContext context)
        {
            _context = context;
        }

        [HttpPost("login")]
        public IActionResult Login([FromBody] User credentials)
        {
            var user = _context.Users.FirstOrDefault(u =>
                u.Username == credentials.Username && u.Password == credentials.Password);

            if (user == null)
                return Unauthorized("Invalid username or password");

            return Ok(new { message = "Login successful" });
        }
    }
}
