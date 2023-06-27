package tk.estecka.datapaint.mixin;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.core.LifeCycle;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryMixin<T>
{

	@Accessor("frozen")
	void	setFrozen(boolean value);
	@Accessor("frozen")
	boolean	isFrozen();

	/*@Mutable*/ @Accessor("rawIdToEntry")     ObjectList<RegistryEntry.Reference<T>>          get_rawIdToEntry();
	/*@Mutable*/ @Accessor("entryToRawId")     Object2IntMap<T>                                get_entryToRawId();
	/*@Mutable*/ @Accessor("idToEntry")        Map<Identifier, RegistryEntry.Reference<T>>     get_idToEntry();
	/*@Mutable*/ @Accessor("keyToEntry")       Map<RegistryKey<T>, RegistryEntry.Reference<T>> get_keyToEntry();
	/*@Mutable*/ @Accessor("valueToEntry")     Map<T, RegistryEntry.Reference<T>>              get_valueToEntry();
	/*@Mutable*/ @Accessor("entryToLifecycle") Map<T, LifeCycle>                               get_entryToLifecycle();

	@Nullable @Accessor("intrusiveValueToEntry") Map<T, RegistryEntry.Reference<T>> get_intrusiveValueToEntry();
	@Nullable @Accessor("cachedEntries")         List<RegistryEntry.Reference<T>>   get_cachedEntries();
	@Accessor("intrusiveValueToEntry") void set_intrusiveValueToEntry(Map<T, RegistryEntry.Reference<T>> value);
	@Accessor("cachedEntries")         void set_cachedEntries        (List<RegistryEntry.Reference<T>>   value);

	@Accessor("tagToEntryList") Map<TagKey<T>, RegistryEntryList.Named<T>> get_tagToEntryList();
}
