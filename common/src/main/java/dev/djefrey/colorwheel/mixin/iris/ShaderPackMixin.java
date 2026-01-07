package dev.djefrey.colorwheel.mixin.iris;

import com.google.common.collect.ImmutableList;
import dev.djefrey.colorwheel.accessors.ShaderPackAccessor;
import dev.djefrey.colorwheel.shaderpack.ClrwlShaderProperties;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.AsyncShaderPack;
import net.irisshaders.iris.shaderpack.DefltShaderPack;
import net.irisshaders.iris.shaderpack.ShaderPack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShaderPack.class)
public abstract class ShaderPackMixin implements ShaderPackAccessor {

    @Shadow @Final public AsyncShaderPack asyncImplementation;
    @Shadow @Final public DefltShaderPack defaultImplementation;
    @Shadow @Final public boolean useLegacyShaderPack;

    @Override
    public ImmutableList<StringPair> colorwheel$getEnvironmentDefines() {
        if (useLegacyShaderPack && asyncImplementation != null) {
            return ((ShaderPackAccessor) asyncImplementation).colorwheel$getEnvironmentDefines();
        } else if (!useLegacyShaderPack && defaultImplementation != null) {
            return ((ShaderPackAccessor) defaultImplementation).colorwheel$getEnvironmentDefines();
        }
        return ImmutableList.of();
    }

    @Override
    public ClrwlShaderProperties colorwheel$getProperties() {
        if (useLegacyShaderPack && asyncImplementation != null) {
            return ((ShaderPackAccessor) asyncImplementation).colorwheel$getProperties();
        } else if (!useLegacyShaderPack && defaultImplementation != null) {
            return ((ShaderPackAccessor) defaultImplementation).colorwheel$getProperties();
        }
        return new ClrwlShaderProperties();
    }
}