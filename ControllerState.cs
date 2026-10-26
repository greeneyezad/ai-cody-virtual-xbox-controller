using System.Text.Json.Serialization;

namespace AiCody.ControllerHost;

public sealed class ControllerState
{
    [JsonPropertyName("type")] public string Type { get; set; } = "";
    [JsonPropertyName("profile")] public string Profile { get; set; } = "Default";
    [JsonPropertyName("buttons")] public List<string> Buttons { get; set; } = [];
    [JsonPropertyName("leftX")] public float LeftX { get; set; }
    [JsonPropertyName("leftY")] public float LeftY { get; set; }
    [JsonPropertyName("rightX")] public float RightX { get; set; }
    [JsonPropertyName("rightY")] public float RightY { get; set; }
    [JsonPropertyName("leftTrigger")] public float LeftTrigger { get; set; }
    [JsonPropertyName("rightTrigger")] public float RightTrigger { get; set; }

    public void Validate()
    {
        if (Type != "state") throw new InvalidDataException("Unknown message type.");
        if (Buttons.Count > 32 || Buttons.Any(b => !Allowed.Contains(b))) throw new InvalidDataException("Invalid buttons.");
        LeftX = Axis(LeftX); LeftY = Axis(LeftY); RightX = Axis(RightX); RightY = Axis(RightY);
        LeftTrigger = Trigger(LeftTrigger); RightTrigger = Trigger(RightTrigger);
    }
    private static float Axis(float v) => float.IsFinite(v) ? Math.Clamp(v, -1f, 1f) : 0f;
    private static float Trigger(float v) => float.IsFinite(v) ? Math.Clamp(v, 0f, 1f) : 0f;
    public static readonly HashSet<string> Allowed = new(StringComparer.OrdinalIgnoreCase)
        { "A","B","X","Y","LB","RB","BACK","START","L3","R3","UP","DOWN","LEFT","RIGHT","GUIDE" };
}

public static class JsonOptions
{
    public static readonly System.Text.Json.JsonSerializerOptions Options = new() { PropertyNameCaseInsensitive = true };
}
