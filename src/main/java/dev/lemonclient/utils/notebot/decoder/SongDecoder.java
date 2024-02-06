package dev.lemonclient.utils.notebot.decoder;

import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.misc.Notebot;
import dev.lemonclient.utils.notebot.song.Song;

import java.io.File;

public abstract class SongDecoder {
    protected Notebot notebot = Modules.get().get(Notebot.class);

    /**
     * Parse file to a {@link Song} object
     *
     * @param file Song file
     * @return A {@link Song} object
     */
    public abstract Song parse(File file) throws Exception;
}
