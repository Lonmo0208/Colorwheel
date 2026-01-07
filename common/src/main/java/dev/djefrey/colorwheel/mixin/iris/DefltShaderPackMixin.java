package dev.djefrey.colorwheel.mixin.iris;

import com.google.common.collect.ImmutableList;
import dev.djefrey.colorwheel.shaderpack.ClrwlShaderProperties;
import dev.djefrey.colorwheel.accessors.PackShadowDirectivesAccessor;
import dev.djefrey.colorwheel.accessors.ShaderPackAccessor;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.DefltShaderPack;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.option.ShaderPackOptions;
import net.irisshaders.iris.shaderpack.preprocessor.PropertiesPreprocessor;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Mixin(DefltShaderPack.class)
public abstract class DefltShaderPackMixin implements ShaderPackAccessor {
    @Shadow @Final private ShaderPackOptions shaderPackOptions;
    @Shadow private Map<NamespacedId, String> dimensionMap;
    @Shadow @Final private ProgramSet base;

    @Unique
    private ImmutableList<StringPair> colorwheel$environmentDefines;

    @Unique
    private ClrwlShaderProperties colorwheel$properties;

    @Inject(
            method = "getProgramSet(Lnet/irisshaders/iris/shaderpack/materialmap/NamespacedId;)Lnet/irisshaders/iris/shaderpack/programs/ProgramSet;",
            at = @At("HEAD"),
            cancellable = true, remap = false
    )
    private void fixGetProgramSet(NamespacedId dimension, CallbackInfoReturnable<ProgramSet> cir) {
        if (dimensionMap == null) {
            cir.setReturnValue(base);
        }
    }

    @Inject(method = "<init>",
            at = @At("TAIL"))
    private void injectInit(Path root, Map<String, String> changedConfigs,
                            ImmutableList<StringPair> environmentDefines, boolean isZip,
                            ShaderPack shaderPack, CallbackInfo ci) {

        this.colorwheel$environmentDefines = environmentDefines;

        try {
            this.colorwheel$properties = loadProperties(root, "colorwheel.properties", environmentDefines)
                    .map(str -> new ClrwlShaderProperties(str, shaderPackOptions, environmentDefines))
                    .orElseGet(ClrwlShaderProperties::new);
        } catch (Exception e) {
            net.irisshaders.iris.Iris.logger.error("Failed to load colorwheel.properties", e);
            this.colorwheel$properties = new ClrwlShaderProperties();
        }

        try {
            if (base != null && base.getPackDirectives() != null &&
                    base.getPackDirectives().getShadowDirectives() != null) {
                ((PackShadowDirectivesAccessor) base.getPackDirectives().getShadowDirectives())
                        .colorwheel$setFlywheelShadowRendering(colorwheel$getProperties().shouldRenderShadow());
            }
        } catch (Exception e) {
            net.irisshaders.iris.Iris.logger.error("Failed to set shadow rendering in DefltShaderPack", e);
        }
    }

    @Unique
    private static Optional<String> loadProperties(Path shaderPath, String name,
                                                   Iterable<StringPair> environmentDefines) {
        try {
            String fileContents = Files.readString(shaderPath.resolve(name), StandardCharsets.ISO_8859_1);
            String processed = PropertiesPreprocessor.preprocessSource(fileContents, environmentDefines);
            return Optional.of(processed);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            net.irisshaders.iris.Iris.logger.error("IO error reading colorwheel.properties", e);
            return Optional.empty();
        }
    }

    @Override
    public ImmutableList<StringPair> colorwheel$getEnvironmentDefines() {
        return colorwheel$environmentDefines;
    }

    @Override
    public ClrwlShaderProperties colorwheel$getProperties() {
        return colorwheel$properties;
    }
}