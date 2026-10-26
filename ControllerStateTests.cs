using AiCody.ControllerHost;
using Xunit;

namespace AiCody.ControllerHost.Tests;

public class ControllerStateTests
{
    [Fact]
    public void ValidStateClampsAxesAndTriggers()
    {
        var state = new ControllerState { Type="state", Buttons=["A"], LeftX=2, LeftY=-2, LeftTrigger=3 };
        state.Validate();
        Assert.Equal(1, state.LeftX);
        Assert.Equal(-1, state.LeftY);
        Assert.Equal(1, state.LeftTrigger);
    }

    [Fact]
    public void InvalidButtonIsRejected()
    {
        var state = new ControllerState { Type="state", Buttons=["PWN"] };
        Assert.Throws<InvalidDataException>(() => state.Validate());
    }

    [Fact]
    public void WrongMessageTypeIsRejected()
    {
        var state = new ControllerState { Type="admin" };
        Assert.Throws<InvalidDataException>(() => state.Validate());
    }
}
