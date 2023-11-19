package core.components;

import core.Component;
import core.systems.IdleSoundSystem;

/**
 * Stores a String path to a sound file that can be played by the {@link IdleSoundSystem}.
 *
 * @param soundEffect Path to the sound file to play.
 * @see IdleSoundSystem
 */
public record IdleSoundComponent(String soundEffect) implements Component {}
