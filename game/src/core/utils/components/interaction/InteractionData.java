package core.utils.components.interaction;

import core.Entity;
import core.components.InteractionComponent;
import core.components.PositionComponent;
import core.utils.Point;

public record InteractionData(
        Entity e, PositionComponent pc, InteractionComponent ic, float dist, Point unitDir) {}
