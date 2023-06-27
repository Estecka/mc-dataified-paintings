package tk.estecka.datapaint.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.ResourceReloader;
import tk.estecka.datapaint.DataPaint;
import tk.estecka.datapaint.VariantResourceListener;

@Mixin(ResourceManagerHelperImpl.class)
public class ResourceManagerHelperImplMixin 
{
	/**
	 * Keeping this here for the sake of documentation; this mixin must not actually be used.
	 * 
	 * Failed attempt at getting the VariantResourceListener to fire before the TagManagerLoader.
	 * This didn't cause any noticeable change.

	 * The Inject annotation may pretend to have a compile-time error, but the injection seems to work nonetheless and the build may proceed.
	 * (Unable to get obfuscation mapping for target.)
	 */
	// @Inject(
	// 	method="Lnet/fabricmc/fabric/impl/resource/loader/ResourceManagerHelperImpl;sort(Ljava/util/List;)V",
	// 	at = @At("RETURN")
	// )
	protected void	sortPriorityListener(List<ResourceReloader> listeners, CallbackInfo info){
		ResourceReloader ls;
		DataPaint.LOGGER.warn("Beep beep, reordering");
		for (int i=0; i<listeners.size(); ++i)
		if ((ls=listeners.get(i)) instanceof VariantResourceListener){
			listeners.remove(i);
			listeners.add(0, ls);
			break;
		}

		for (int i=0; i<listeners.size(); ++i)
		if ((ls=listeners.get(i)) instanceof VariantResourceListener)
			DataPaint.LOGGER.warn("Variant");
		else if (ls instanceof TagManagerLoader)
			DataPaint.LOGGER.warn("Tags");
	}
}
