package dev.lemonclient.managers;

import dev.lemonclient.managers.impl.*;
import dev.lemonclient.managers.impl.notification.NotificationManager;
import dev.lemonclient.music.MusicManager;
import dev.lemonclient.renderer.MeshRenderer;

public class Managers {
    public static final HoldingManager HOLDING = new HoldingManager();
    public static final NotificationManager NOTIFICATION = new NotificationManager();
    public static final MusicManager MUSIC = new MusicManager();
    public static final OnGroundManager ON_GROUND = new OnGroundManager();
    public static final PingSpoofManager PING_SPOOF = new PingSpoofManager();
    public static final PlayerManager PLAYER = new PlayerManager();
    //public static final RenderManager RENDER = new RenderManager();
    public static final RotationManager ROTATION = new RotationManager();
    public static final ShaderManager SHADER = new ShaderManager();
    public static final SoundManager SOUND = new SoundManager();
    public static final SpeedManager SPEED = new SpeedManager();
    public static final MeshRenderer MESH_RENDERER = new MeshRenderer();
}
