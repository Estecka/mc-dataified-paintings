package tk.estecka.datapaint.mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;
import tk.estecka.datapaint.DataPaint;
import tk.estecka.datapaint.VariantResourceListener;

@Mixin(TagManagerLoader.class)
public class TagManagerMixin {
	private static VariantResourceListener variantLoader = new VariantResourceListener();

	@Inject(
		method="reload",
		at = @At("HEAD")
	)
	public void	DoVariantsBeforeTags(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> info){
		DataPaint.LOGGER.info("Piggy-backing the TagManagerLoader to refresh painting variants.");
		variantLoader.reload(manager);
		DataPaint.LOGGER.info("TagManagerLoader will now be resuming normal duties.");
	}
	
}
