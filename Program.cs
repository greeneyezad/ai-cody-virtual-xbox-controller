using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Text.Json;
using AiCody.ControllerHost;

Console.WriteLine("AI Cody Virtual Xbox 360 Controller Host");
Console.WriteLine("Loopback TCP listener: 127.0.0.1:26760");
Console.WriteLine("Press Ctrl+C to exit. Controller resets on disconnect.");
using var listener = new TcpListener(IPAddress.Loopback, 26760);
listener.Start();
using var output = new VigemOutput();
try { output.Connect(); Console.WriteLine("Virtual Xbox 360 output connected."); }
catch (Exception ex) { Console.WriteLine($"Virtual output unavailable: {ex.Message}"); Console.WriteLine("Install a compatible ViGEmBus driver to enable XInput output; input receiver still starts."); }

while (true)
{
    using var client = await listener.AcceptTcpClientAsync();
    Console.WriteLine($"Client connected: {client.Client.RemoteEndPoint}");
    try
    {
        using var reader = new StreamReader(client.GetStream(), Encoding.UTF8);
        while (await reader.ReadLineAsync() is { } line)
        {
            try
            {
                var state = JsonSerializer.Deserialize<ControllerState>(line, JsonOptions.Options);
                if (state is not null) output.Apply(state);
            }
            catch (Exception ex) { Console.WriteLine($"Rejected packet: {ex.Message}"); output.Reset(); }
        }
    }
    catch (IOException ex) { Console.WriteLine($"Client I/O: {ex.Message}"); }
    finally { output.Reset(); Console.WriteLine("Controller state reset."); }
}
