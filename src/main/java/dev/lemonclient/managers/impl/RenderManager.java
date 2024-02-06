package dev.lemonclient.managers.impl;
/*
import dev.lemonclient.LemonClient;
import dev.lemonclient.events.render.Render3DEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.function.DoubleFunc;
import meteordevelopment.orbit.EventHandler;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RenderManager {
    private final List<RenderTask> renderTasks = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    public RenderManager() {
        LemonClient.EVENT_BUS.subscribe(this);
    }

    public void start() {
        register(((event, renderTask) -> {
        }));
    }

    private void register(DoubleFunc<Render3DEvent, RenderTask> p) {
        this.submit(p);
    }

    public void submit(DoubleFunc<Render3DEvent, RenderTask> p) {
        renderTasks.add(new RenderTask(p));
    }

    public void submit(long delay, DoubleFunc<Render3DEvent, RenderTask> task, int priority) {
        renderTasks.add(new RenderTask(delay, task, priority));
    }

    public void submit(long delay, DoubleFunc<Render3DEvent, RenderTask> task) {
        renderTasks.add(new RenderTask(delay, task));
    }

    public void submit(DoubleFunc<Render3DEvent, RenderTask> task, int priority) {
        renderTasks.add(new RenderTask(task, priority));
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (RenderTask task : renderTasks) {
            if (task.alwaysRender) return;

            int ticks = (int) (task.renderDelays / 20);
            if (ticks > 0) {
                task.renderDelays -= 20L;
            } else {
                task.destroy();
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        for (RenderTask task : renderTasks) {
            if (task.task != null) {
                task.task.accept(event, task);
            }
        }
    }

    public class RenderTask {
        private long renderDelays;
        private boolean isDelay;

        private DoubleFunc<Render3DEvent, RenderTask> task;
        private boolean alwaysRender;
        private int priority = 1000;

        private RenderTask(long renderDelays, boolean isDelay, DoubleFunc<Render3DEvent, RenderTask> task, boolean alwaysRender, int priority) {
            this.renderDelays = renderDelays;
            this.isDelay = isDelay;
            this.task = task;
            this.alwaysRender = alwaysRender;
            this.priority = priority;
        }

        public RenderTask(long renderDelays, DoubleFunc<Render3DEvent, RenderTask> task, int priority) {
            this(renderDelays, true, task, false, priority);
        }

        public RenderTask(long renderDelays, DoubleFunc<Render3DEvent, RenderTask> task) {
            this(renderDelays, task, 1000);
        }

        private RenderTask(DoubleFunc<Render3DEvent, RenderTask> task, boolean alwaysRender) {
            this(0, false, task, alwaysRender, 1000);
        }

        public RenderTask(DoubleFunc<Render3DEvent, RenderTask> task) {
            this(task, true);
        }

        public RenderTask(DoubleFunc<Render3DEvent, RenderTask> task, int priority) {
            this(0, false, task, true, priority);
        }

        public void destroy() {
            RenderManager.this.renderTasks.remove(this);

            this.task = null;
            isDelay = false;
            alwaysRender = false;
            priority = 0;
            renderDelays = 0;
        }
    }
}
*/
