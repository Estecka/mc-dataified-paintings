package tk.estecka.datapaint;

import net.fabricmc.api.ModInitializer;
// import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
// import net.minecraft.resource.ResourceType;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataPaint implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Dataified Paintings");

	@Override
	public void onInitialize() {
		// // Fabric's ResourceManager won't cut it, it seems.
		// // I need to guarantee that painting variants are loaded in BEFORE 
		// // the tags, which I achieved by piggy-backing the TagManagerLoader
		// // in a mixin
		// ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new VariantResourceListener());
	}
}
