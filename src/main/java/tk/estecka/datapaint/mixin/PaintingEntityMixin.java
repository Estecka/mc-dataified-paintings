package tk.estecka.datapaint.mixin;

import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import tk.estecka.datapaint.DataPaint;
import tk.estecka.datapaint.RegistryProxy;

@Mixin(PaintingEntity.class)
public class PaintingEntityMixin 
{
	static private final DefaultedRegistry<PaintingVariant> PAINTING_VARIANTS = Registries.PAINTING_VARIANT;

	/**
	 * @author Estecka
	 * @reason Forces the creation of a placeholder when a non-existent variant 
	 * is found, instead of returning Kebab.
	 */
	@Overwrite
	static public Optional<RegistryEntry<PaintingVariant>>	readVariantFromNbt(NbtCompound nbt){
		Identifier id = Identifier.tryParse(nbt.getString(PaintingEntity.VARIANT_NBT_KEY));
		if (id == null)
			return Optional.empty();
		if (PAINTING_VARIANTS.containsId(id))
			return Optional.of( PAINTING_VARIANTS.getEntry(PAINTING_VARIANTS.get(id)) );
		else
			return Optional.of( RegistryProxy.GetOrCreatePlaceholder(id) );
	}

	/**
	 * Forces pre-existing paintings to refresh after a Datapack reload has occured.
	 */
	@Redirect(
		method = "getVariant",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/entity/data/DataTracker.get (Lnet/minecraft/entity/data/TrackedData;)Ljava/lang/Object;"
		)
	)
	public Object	getOrUpdateVariant(DataTracker tracker, TrackedData<RegistryEntry<PaintingVariant>> data){
		PaintingEntity entity =(PaintingEntity)(Object)this;
		RegistryEntry<PaintingVariant> variantEntry = tracker.get(data);
		RegistryKey<PaintingVariant> key = variantEntry.getKey().get();
		PaintingVariant oldValue = variantEntry.value();

		if (key == null){
			DataPaint.LOGGER.error(
				"This painting has no variant key. I don't know how to handle it: {} {}",
				entity.getUuid(),
				entity.getPose()
			);
		}
		else if (PAINTING_VARIANTS.getKey(oldValue).isEmpty()) {
			Identifier id = key.getValue();
			DataPaint.LOGGER.warn("Painting with obsolete variant for {} found.", id);
			if (Registries.PAINTING_VARIANT.containsId(id)) {
				PaintingVariant newValue = Registries.PAINTING_VARIANT.get(id);
				DataPaint.LOGGER.info(
					"Updating painting to the more recent version: {}x{} => {}x{}",
					oldValue.getWidth()/16, oldValue.getHeight()/16,
					newValue.getWidth()/16, newValue.getHeight()/16
				);
				entity.setVariant(Registries.PAINTING_VARIANT.getEntry(newValue));
			}
			else {
				DataPaint.LOGGER.error("The variant {} no longer exist.", id);
				var ph = RegistryProxy.GetOrCreatePlaceholder(id);
				entity.setVariant(ph);
			}
			return tracker.get(data);
		}

		return variantEntry;
	}
}
