package de.jeff_media.drop2inventory.utils;

import com.google.common.base.Enums;
import de.jeff_media.daddy.CallHome;
import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class SoundUtils {

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    private final Sound sound;
    private final boolean soundEnabled;
    private final boolean soundGlobal;
    private final float soundPitch;
    private final float soundVolume;
    private final float soundPitchVariant;

    public SoundUtils() {
        final Main main = Main.getInstance();
        final String soundName = main.getConfig().getString(Config.SOUND_EFFECT);
        sound = Enums.getIfPresent(Sound.class, soundName).orNull();
        CallHome.callHome(main);
        if (sound == null) {
            main.getLogger().warning("Unknown sound effect: " + soundName);
        }
        soundEnabled = main.getConfig().getBoolean(Config.SOUND_ENABLED);
        soundGlobal = main.getConfig().getBoolean(Config.SOUND_GLOBAL);
        soundVolume = (float) main.getConfig().getDouble(Config.SOUND_VOLUME);
        soundPitch = (float) main.getConfig().getDouble(Config.SOUND_PITCH);
        soundPitchVariant = (float) main.getConfig().getDouble(Config.SOUND_PITCH_VARIANT,0.0);

    }

    public void playPickupSound(Player player) {
        if(!soundEnabled) {
            return;
        }
        if(sound==null) return;
        final float pitchVariant = soundPitchVariant == 0 ? 0 : (float) (random.nextDouble(soundPitchVariant) - (soundPitchVariant / 2));
        if(soundGlobal) {
            player.getWorld().playSound(player.getLocation(),sound,soundVolume,soundPitch + pitchVariant);
        } else {
            player.playSound(player.getLocation(),sound,soundVolume,soundPitch + pitchVariant);
        }
    }

}

