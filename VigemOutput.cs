using Nefarius.ViGEm.Client;
using Nefarius.ViGEm.Client.Targets;
using Nefarius.ViGEm.Client.Targets.Xbox360;

namespace AiCody.ControllerHost;

public sealed class VigemOutput : IDisposable
{
    private ViGEmClient? _client;
    private IXbox360Controller? _controller;
    private bool _connected;

    public void Connect()
    {
        _client = new ViGEmClient();
        _controller = _client.CreateXbox360Controller();
        _controller.Connect();
        _connected = true;
    }

    public void Apply(ControllerState state)
    {
        state.Validate();
        if (!_connected || _controller is null) return;
        foreach (var (name, button) in ButtonMap)
            _controller.SetButtonState(button, state.Buttons.Contains(name, StringComparer.OrdinalIgnoreCase));
        _controller.SetAxisValue(Xbox360Axis.LeftThumbX, Axis16(state.LeftX));
        _controller.SetAxisValue(Xbox360Axis.LeftThumbY, Axis16(-state.LeftY));
        _controller.SetAxisValue(Xbox360Axis.RightThumbX, Axis16(state.RightX));
        _controller.SetAxisValue(Xbox360Axis.RightThumbY, Axis16(-state.RightY));
        _controller.SetSliderValue(Xbox360Slider.LeftTrigger, (byte)Math.Round(state.LeftTrigger * 255));
        _controller.SetSliderValue(Xbox360Slider.RightTrigger, (byte)Math.Round(state.RightTrigger * 255));
    }

    public void Reset()
    {
        if (!_connected || _controller is null) return;
        foreach (var button in ButtonMap.Values) _controller.SetButtonState(button, false);
        _controller.SetAxisValue(Xbox360Axis.LeftThumbX, 0);
        _controller.SetAxisValue(Xbox360Axis.LeftThumbY, 0);
        _controller.SetAxisValue(Xbox360Axis.RightThumbX, 0);
        _controller.SetAxisValue(Xbox360Axis.RightThumbY, 0);
        _controller.SetSliderValue(Xbox360Slider.LeftTrigger, 0);
        _controller.SetSliderValue(Xbox360Slider.RightTrigger, 0);
    }

    private static short Axis16(float value) => (short)Math.Clamp(Math.Round(value * 32767), -32768, 32767);
    private static readonly Dictionary<string, Xbox360Button> ButtonMap = new(StringComparer.OrdinalIgnoreCase)
    {
        ["A"] = Xbox360Button.A, ["B"] = Xbox360Button.B, ["X"] = Xbox360Button.X, ["Y"] = Xbox360Button.Y,
        ["LB"] = Xbox360Button.LeftShoulder, ["RB"] = Xbox360Button.RightShoulder,
        ["BACK"] = Xbox360Button.Back, ["START"] = Xbox360Button.Start,
        ["L3"] = Xbox360Button.LeftThumb, ["R3"] = Xbox360Button.RightThumb,
        ["UP"] = Xbox360Button.Up, ["DOWN"] = Xbox360Button.Down, ["LEFT"] = Xbox360Button.Left, ["RIGHT"] = Xbox360Button.Right,
        ["GUIDE"] = Xbox360Button.Guide
    };
    public void Dispose()
    {
        Reset();
        if (_connected) _controller?.Disconnect();
        _controller?.Dispose();
        _client?.Dispose();
    }
}
