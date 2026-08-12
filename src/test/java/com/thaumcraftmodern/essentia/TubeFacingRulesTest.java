package com.thaumcraftmodern.essentia;

import com.thaumcraftmodern.essentia.tube.TubeFacingRules;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TubeFacingRulesTest {
    @Test
    void repeatedClickOnSameFaceFlipsToExactOpposite() {
        Direction first = TubeFacingRules.toggleFacing(
                Direction.DOWN, Direction.UP);
        Direction second = TubeFacingRules.toggleFacing(first, Direction.UP);

        assertEquals(Direction.UP, first);
        assertEquals(Direction.DOWN, second);
    }

    @Test
    void advancesToFirstFacingWhoseOppositeSideIsConnectedAndOpen() {
        EnumSet<Direction> connectedOpenSides = EnumSet.of(
                Direction.EAST, Direction.UP);

        // After NORTH the candidates are SOUTH, WEST, EAST, DOWN, UP.
        // WEST wins because its controlled side EAST is connected and open.
        assertEquals(Direction.WEST, TubeFacingRules.nextConnectedFacing(
                Direction.NORTH, connectedOpenSides::contains));
    }

    @Test
    void keepsFacingWhenItIsTheOnlyConnectedOpenDirection() {
        assertEquals(Direction.UP, TubeFacingRules.nextConnectedFacing(
                Direction.UP, side -> side == Direction.DOWN));
    }

    @Test
    void keepsFacingWhenNoSideCanAcceptTheOutline() {
        assertEquals(Direction.SOUTH, TubeFacingRules.nextConnectedFacing(
                Direction.SOUTH, side -> false));
    }
}
