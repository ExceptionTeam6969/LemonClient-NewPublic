package dev.lemonclient.managers.impl.notification;

import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.Render2DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.utils.timers.MSTimer;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    public final List<Notification> notifications = new ArrayList<>();

    public NotificationManager() {
        timer.reset();

        LemonClient.EVENT_BUS.subscribe(this);
    }

    private final MSTimer timer = new MSTimer();

    public void info(String title, String txt) {
        notifications.add(new Notification(Notification.Type.INFO, title, txt).withShowTime(30));
    }

    public void success(String title, String txt) {
        notifications.add(new Notification(Notification.Type.SUCCESS, title, txt).withShowTime(30));
    }

    public void warn(String title, String txt) {
        notifications.add(new Notification(Notification.Type.WARING, title, txt).withShowTime(30));
    }

    public void error(String title, String txt) {
        notifications.add(new Notification(Notification.Type.ERROR, title, txt).withShowTime(30));
    }

    private void renderUpdate() {
        if (timer.hasTimePassed(1)) {
            if (!notifications.isEmpty()) {
                Notification main = notifications.get(0);
                if (main.startUpdated) {
                    main.update();
                }

                if (main.showTime <= 0 && main.willRemove) {
                    //main.destroy();
                    notifications.remove(0);
                }
            }
            timer.reset();
        }
    }

    private void tickUpdate() {
        if (!notifications.isEmpty()) {
            Notification main = notifications.get(0);
            if (main.startUpdated) {
                main.update();
            }

            if (main.showTime <= 0 && main.willRemove) {
                //main.destroy();
                notifications.remove(0);
            }
        }
    }

    @EventHandler
    private void onRender(Render2DEvent event) {
        //renderUpdate();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        tickUpdate();
    }
}
