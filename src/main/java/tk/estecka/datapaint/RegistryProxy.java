package tk.estecka.datapaint;

import java.util.IdentityHashMap;
import org.slf4j.Logger;
import com.mojang.serialization.Lifecycle;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import tk.estecka.datapaint.mixin.SimpleRegistryMixin;

public class RegistryProxy 
{
	static public final SimpleRegistry<PaintingVariant> ACTIVE_VARIANTS = (SimpleRegistry<PaintingVariant>)Registries.PAINTING_VARIANT;
	static private final SimpleRegistryMixin<PaintingVariant> ACTIVE_INTERNALS = (SimpleRegistryMixin<PaintingVariant>)(Object)ACTIVE_VARIANTS;
	static private boolean wasFrozen;

	static private SimpleRegistry<PaintingVariant> DEFAULT_VARIANTS = null;
	static private SimpleRegistryMixin<PaintingVariant> DEFAULT_INTERNALS = null;
	
	static private final Logger LOGGER = DataPaint.LOGGER;


	static synchronized public void	EnableBypass(){
		LOGGER.info("Bypassing freeze state on {}", ACTIVE_VARIANTS);
		wasFrozen = ACTIVE_INTERNALS.isFrozen();
		ACTIVE_INTERNALS.setFrozen(false);
	}

	static synchronized public void	DisableBypass(){
		LOGGER.info("Freeze state is no longer being bypassed on {}", ACTIVE_VARIANTS);
		ACTIVE_INTERNALS.setFrozen(wasFrozen);
	}


/******************************************************************************/
/* ## Backup and Restore                                                      */
/******************************************************************************/
/*
 * Here be dragons.
 * This section is about restoring the registry to a "no-datapack" state, before
 * a reload is executed.
 * 
 * This section  is higly delicate, particularly when it comes to unloading old
 * paintings, and may easily lead to crashes when not handled properly. 
 * 
 * 	1. Registry reload MUST finalize before the tags are reloaded.
 * Otherwise, in the best case, paintings are not tagged properly.  In the worst 
 * case,  a painting from the datapack-to-be-discarded will be tagged, and cause
 * a crash somewhere down the line,  when that particular painting can no longer
 * be found in the registry.
 * 
 * 	2. Registry is not threadsafe
 * While  the server  thread performs  a datapack reload,  the render thread may 
 * still attemp to access the registry. Most notably while paintings are visible
 * on-screen, and probably the same for painting tooltips.
 * Thread-safety was put in place for write operations, but a fully satisfactory
 * solution has yet to be found.
 * 
 * If all hells break loose,  the code in this section  can be put on the bench,
 * and still  leave the mod  in a working -albeit incomplete- state.  This will
 * prevent  painting variants  from old datapacks  from being unloaded,  but new
 * ones can be added,  or overwrite old ones.  Tagging them properly might need 
 * a second reloads to actually work.
 */

	static synchronized public boolean	HasDefault(){
		return DEFAULT_VARIANTS != null;
	}

	static private void	Copy(SimpleRegistryMixin<PaintingVariant> SRC, SimpleRegistryMixin<PaintingVariant> DST){
		DST.get_rawIdToEntry          ().addAll( SRC.get_rawIdToEntry()     );
		DST.get_entryToLifecycle      ().putAll( SRC.get_entryToLifecycle() );
		DST.get_entryToRawId          ().putAll( SRC.get_entryToRawId()     );
		DST.get_idToEntry             ().putAll( SRC.get_idToEntry()        );
		DST.get_keyToEntry            ().putAll( SRC.get_keyToEntry()       );
		DST.get_valueToEntry          ().putAll( SRC.get_valueToEntry()     );

		// // These ones appears to self-regenerate.
		// // Hopefully this won't backfire in a cataclysmic spectacle.
		// DST.get_tagToEntryList().putAll(SRC.get_tagToEntryList());
		// DST.set_cachedEntries(null);

		// The painting registry appears to be non-intrusive, so all this is probably overly-cautious paranoïa.
		// But I won't pretend to know the inner workings of the system just yet.
		var intrusiveDST = DST.get_intrusiveValueToEntry();
		var intrusiveSRC = SRC.get_intrusiveValueToEntry();
		if ((intrusiveSRC==null) != (intrusiveDST==null))
			LOGGER.warn("Intrusivity status was changed to {} during copy", (intrusiveSRC==null) ? "null" : "non-null");
		if (intrusiveSRC == null)
			DST.set_intrusiveValueToEntry(null);
		else {
			if (intrusiveDST==null)
					DST.set_intrusiveValueToEntry(new IdentityHashMap<PaintingVariant, RegistryEntry.Reference<PaintingVariant>>());
			intrusiveDST.putAll(intrusiveSRC);
		}
	}

	static synchronized public void	CaptureDefault(){
		LOGGER.info("Snapshoting the current painting variants");
		if (HasDefault())
			LOGGER.warn("A snapshot was already made and will being overwritten.");

		DEFAULT_VARIANTS = new SimpleRegistry<PaintingVariant>(RegistryKeys.PAINTING_VARIANT, Lifecycle.experimental(), false);
		DEFAULT_INTERNALS = (SimpleRegistryMixin<PaintingVariant>)(Object)DEFAULT_VARIANTS;

		Copy(ACTIVE_INTERNALS, DEFAULT_INTERNALS);
		DEFAULT_VARIANTS.freeze();
	}

	static synchronized public void	RestoreDefault(){
		LOGGER.info("Restoring the painting registry's default paintings.");

		ACTIVE_INTERNALS.get_entryToLifecycle().clear();
		ACTIVE_INTERNALS.get_entryToRawId    ().clear();
		ACTIVE_INTERNALS.get_idToEntry       ().clear();
		ACTIVE_INTERNALS.get_keyToEntry      ().clear();
		ACTIVE_INTERNALS.get_rawIdToEntry    ().clear();
		ACTIVE_INTERNALS.get_tagToEntryList  ().clear();
		ACTIVE_INTERNALS.get_valueToEntry    ().clear();

		ACTIVE_INTERNALS.set_cachedEntries(null);

		if (ACTIVE_INTERNALS.get_intrusiveValueToEntry() != null)
			ACTIVE_INTERNALS.get_intrusiveValueToEntry().clear();

		Copy(DEFAULT_INTERNALS, ACTIVE_INTERNALS);
	}


/******************************************************************************/
/* ## Datapack Content registrations                                          */
/******************************************************************************/

	static private RegistryEntry<PaintingVariant>	AddVariant(Identifier id, PaintingVariant value){
		LOGGER.info(
			"Registering new {}x{} painting \"{}\"",
			value.getWidth()/16, value.getHeight()/16,
			id
		);
		Registry.register(
			ACTIVE_VARIANTS,
			RegistryKey.of(RegistryKeys.PAINTING_VARIANT, id),
			value
		);
		return ACTIVE_VARIANTS.getEntry(value);
	}

	static private void	UpdateVariant(Identifier id, PaintingVariant newValue){
		PaintingVariant oldValue = ACTIVE_VARIANTS.get(id);
		LOGGER.warn(
			"Overwritting existing painting from {}x{} to {}x{} ({})",
			oldValue.getWidth()/16, oldValue.getHeight()/16,
			newValue.getWidth()/16, newValue.getHeight()/16,
			id
		);

		int rawId     = ACTIVE_VARIANTS.getRawId(oldValue);
		var key       = ACTIVE_VARIANTS.getKey(oldValue).get();
		var lifecycle = ACTIVE_VARIANTS.getEntryLifecycle(oldValue);
		ACTIVE_VARIANTS.set(rawId, key, newValue, lifecycle);
	}

	static synchronized public void	AddOrUpdateVariant(Identifier id, PaintingVariant value){
		if (ACTIVE_VARIANTS.containsId(id))
			UpdateVariant(id, value);
		else
			AddVariant(id, value);
	}

	static synchronized public RegistryEntry<PaintingVariant>	GetOrCreatePlaceholder(Identifier id){
		RegistryEntry<PaintingVariant> r;
		RegistryProxy.EnableBypass();
		var key = RegistryKey.of(RegistryKeys.PAINTING_VARIANT, id);
		if (ACTIVE_VARIANTS.contains(key)){
			DataPaint.LOGGER.info("A placeholder needed not be ve created for {}", id);
			r = ACTIVE_VARIANTS.getEntry(key).get();
		}
		else {
			DataPaint.LOGGER.warn("Creating placeholder variant for {}", id);
			r = AddVariant(id, new PaintingVariant(16, 16));
		}
		RegistryProxy.DisableBypass();
		return r;
	}

}
