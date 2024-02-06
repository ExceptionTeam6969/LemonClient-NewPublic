package dev.lemonclient.systems.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lemonclient.events.render.WeatherRenderEvent;
import dev.lemonclient.events.world.TickEvent;
import dev.lemonclient.settings.Setting;
import dev.lemonclient.settings.SettingGroup;
import dev.lemonclient.systems.modules.Categories;
import dev.lemonclient.systems.modules.Module;
import dev.lemonclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

import static net.minecraft.client.render.WorldRenderer.getLightmapCoordinates;

public class CustomWeather extends Module {
    public CustomWeather() {
        super(Categories.Render, "Custom Weather", "Force some weather to be added to the game.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCustomize = settings.createGroup("Customize");

    //--------------------General--------------------//
    private final Setting<PrecipitationType> precipitationSetting = enumSetting(sgGeneral, "precipitation", PrecipitationType.Snow);
    private final Setting<Double> height = doubleSetting(sgGeneral, "Height", -64, -64, 320);
    private final Setting<Double> strength = doubleSetting(sgGeneral, "Strength", 0.8, 0.1, 2.0);
    private final Setting<SettingColor> weatherColor = colorSetting(sgGeneral, "Color", SettingColor.WHITE);
    private final Setting<Double> snowFallingSpeedMultiplier = doubleSetting(sgGeneral, "Snow Falling Speed Multiplier", 1.0, 0.0, 10.0);

    //--------------------Customize--------------------//
    private final Setting<Integer> expandSize = intSetting(sgCustomize, "Expand Size", 5, 1, 10);

    private static final Identifier RAIN = new Identifier("textures/environment/rain.png");
    private static final Identifier SNOW = new Identifier("textures/environment/snow.png");

    private int ticks = 0;
    private static final float[] weatherXCoords = new float[1024];
    private static final float[] weatherYCoords = new float[1024];

    static {
        for (int xRange = 0; xRange < 32; ++xRange) {
            for (int zRange = 0; zRange < 32; ++zRange) {
                float x = (float) (zRange - 16);
                float z = (float) (xRange - 16);
                float length = MathHelper.sqrt(x * x + z * z);
                weatherXCoords[xRange << 5 | zRange] = -z / length;
                weatherYCoords[xRange << 5 | zRange] = x / length;
            }
        }
    }

    public enum PrecipitationType {
        None,
        Rain,
        Snow,
        Both;

        public Biome.Precipitation toMC() {
            return switch (this) {
                case None, Both -> Biome.Precipitation.NONE;
                case Rain -> Biome.Precipitation.RAIN;
                case Snow -> Biome.Precipitation.SNOW;
            };
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ++ticks;
    }

    @EventHandler
    private void onWeather(WeatherRenderEvent event) {
        if (precipitationSetting.get().equals(PrecipitationType.Both)) {
            render(event, PrecipitationType.Rain);
            render(event, PrecipitationType.Snow);
            event.cancel();
            return;
        }

        render(event, precipitationSetting.get());

        event.cancel();
    }

    private void render(WeatherRenderEvent event, PrecipitationType precipitationType) {
        double cameraX = event.cameraX;
        double cameraY = event.cameraY;
        double cameraZ = event.cameraZ;

        float f = strength.get().floatValue();
        float red = weatherColor.get().r / 255f;
        float blue = weatherColor.get().b / 255f;
        float green = weatherColor.get().g / 255f;

        event.lightmapTextureManager.enable();
        int cameraIntX = MathHelper.floor(cameraX);
        int cameraIntY = MathHelper.floor(cameraY);
        int cameraIntZ = MathHelper.floor(cameraZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.setShader(GameRenderer::getParticleProgram);

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int expand = expandSize.get();
        int tessPosition = -1;
        float fallingValue = (float) this.ticks + event.tickDelta;

        for (int zRange = cameraIntZ - expand; zRange <= cameraIntZ + expand; ++zRange) {
            for (int xRange = cameraIntX - expand; xRange <= cameraIntX + expand; ++xRange) {
                int coordPos = (zRange - cameraIntZ + 16) * 32 + xRange - cameraIntX + 16;

                if (coordPos < 0 || coordPos > 1023) continue;

                double xCoord = (double) weatherXCoords[coordPos] * 0.5;
                double zCoord = (double) weatherYCoords[coordPos] * 0.5;
                mutable.set(xRange, cameraY, zRange);

                int maxHeight = height.get().intValue();
                int minIntY = cameraIntY - expand;
                int expandedCameraY = cameraIntY + expand;
                if (minIntY < maxHeight) {
                    minIntY = maxHeight;
                }

                if (expandedCameraY < maxHeight) {
                    expandedCameraY = maxHeight;
                }

                int maxRenderY = Math.max(maxHeight, cameraIntY);

                if (minIntY != expandedCameraY) {
                    Random random = Random.create((long) xRange * xRange * 3121 + xRange * 45238971L ^ (long) zRange * zRange * 418711 + zRange * 13761L);
                    mutable.set(xRange, minIntY, zRange);
                    float texTextureV;
                    float weatherAlpha;
                    Biome.Precipitation precipitation = precipitationType.toMC();
                    if (precipitation == Biome.Precipitation.RAIN) {
                        if (tessPosition != 0) {
                            if (tessPosition >= 0) {
                                tessellator.draw();
                            }

                            tessPosition = 0;
                            RenderSystem.setShaderTexture(0, RAIN);
                            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }

                        int randomSeed = this.ticks + xRange * xRange * 3121 + xRange * 45238971 + zRange * zRange * 418711 + zRange * 13761 & 31;
                        texTextureV = -((float) randomSeed + event.tickDelta) / 32.0F * (3.0F + random.nextFloat());
                        double xOffset = (double) xRange + 0.5 - cameraX;
                        double yOffset = (double) zRange + 0.5 - cameraZ;
                        float dLength = (float) Math.sqrt(xOffset * xOffset + yOffset * yOffset) / (float) expand;
                        weatherAlpha = ((1.0F - dLength * dLength) * 0.5F + 0.5F) * f;
                        mutable.set(xRange, maxRenderY, zRange);
                        int lightmapCoord = getLightmapCoordinates(mc.world, mutable);

                        bufferBuilder.vertex(xRange - cameraX - xCoord + 0.5, expandedCameraY - cameraY, zRange - cameraZ - zCoord + 0.5).texture(0.0F, (float) minIntY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord).next();
                        bufferBuilder.vertex(xRange - cameraX + xCoord + 0.5, expandedCameraY - cameraY, zRange - cameraZ + zCoord + 0.5).texture(1.0F, (float) minIntY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord).next();
                        bufferBuilder.vertex(xRange - cameraX + xCoord + 0.5, minIntY - cameraY, zRange - cameraZ + zCoord + 0.5).texture(1.0F, (float) expandedCameraY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord).next();
                        bufferBuilder.vertex(xRange - cameraX - xCoord + 0.5, minIntY - cameraY, zRange - cameraZ - zCoord + 0.5).texture(0.0F, (float) expandedCameraY * 0.25F + texTextureV).color(red, green, blue, weatherAlpha).light(lightmapCoord).next();
                    } else if (precipitation == Biome.Precipitation.SNOW) {
                        if (tessPosition != 1) {
                            if (tessPosition == 0) {
                                tessellator.draw();
                            }

                            tessPosition = 1;
                            RenderSystem.setShaderTexture(0, SNOW);
                            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }
                        float snowSmooth = -((float) (this.ticks & 511) + event.tickDelta) / 512.0F;
                        texTextureV = (float) (random.nextDouble() + (double) fallingValue * 0.01 * (double) ((float) random.nextGaussian()));
                        float fallingSpeed = (float) ((float) (random.nextDouble() + (double) (fallingValue * (float) random.nextGaussian()) * 0.001) * snowFallingSpeedMultiplier.get());
                        double xOffset = (double) xRange + 0.5 - cameraX;
                        double yOffset = (double) zRange + 0.5 - cameraZ;
                        weatherAlpha = (float) Math.sqrt(xOffset * xOffset + yOffset * yOffset) / (float) expand;
                        float snowAlpha = ((1.0F - weatherAlpha * weatherAlpha) * 0.3F + 0.5F) * f;
                        mutable.set(xRange, maxRenderY, zRange);
                        int lightMapCoord = getLightmapCoordinates(mc.world, mutable);
                        int lightmapCalcV = lightMapCoord >> 16 & '\uffff';
                        int lightmapCalcU = lightMapCoord & '\uffff';
                        int lightmapV = (lightmapCalcV * 3 + 240) / 4;
                        int lightmapU = (lightmapCalcU * 3 + 240) / 4;
                        bufferBuilder.vertex(xRange - cameraX - xCoord + 0.5, expandedCameraY - cameraY, zRange - cameraZ - zCoord + 0.5).texture(0.0F + texTextureV, (float) minIntY * 0.25F + snowSmooth + fallingSpeed).color(red, green, blue, snowAlpha).light(lightmapU, lightmapV).next();
                        bufferBuilder.vertex(xRange - cameraX + xCoord + 0.5, expandedCameraY - cameraY, zRange - cameraZ + zCoord + 0.5).texture(1.0F + texTextureV, (float) minIntY * 0.25F + snowSmooth + fallingSpeed).color(red, green, blue, snowAlpha).light(lightmapU, lightmapV).next();
                        bufferBuilder.vertex(xRange - cameraX + xCoord + 0.5, minIntY - cameraY, zRange - cameraZ + zCoord + 0.5).texture(1.0F + texTextureV, (float) expandedCameraY * 0.25F + snowSmooth + fallingSpeed).color(red, green, blue, snowAlpha).light(lightmapU, lightmapV).next();
                        bufferBuilder.vertex(xRange - cameraX - xCoord + 0.5, minIntY - cameraY, zRange - cameraZ - zCoord + 0.5).texture(0.0F + texTextureV, (float) expandedCameraY * 0.25F + snowSmooth + fallingSpeed).color(red, green, blue, snowAlpha).light(lightmapU, lightmapV).next();
                    }
                }
            }
        }

        if (tessPosition >= 0) {
            tessellator.draw();
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        event.lightmapTextureManager.disable();
    }
}
