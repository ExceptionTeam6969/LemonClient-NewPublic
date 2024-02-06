package dev.lemonclient.systems.modules.movement;

import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;

public class AntiCrawl extends Module {
    public AntiCrawl() {
        super(Categories.Movement, "Anti Crawl", "Doesn't crawl or sneak when in low space (should be used on 1.12.2).");
    }
}
