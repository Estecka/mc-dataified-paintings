package tk.estecka.datapaint;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class VariantResourceListener 
implements SimpleSynchronousResourceReloadListener
{
	private static class	JsonVariant {
		public int width;
		public int height;
	}

	private final Identifier	identifier;
	private final Gson gson = new Gson();

	public VariantResourceListener(){
		this.identifier = new Identifier("datapaint", "variantloader");
	}

	public Identifier	getFabricId(){
		return this.identifier;
	}

	private boolean ShouldReadFile(Identifier path){
		return path.getPath().endsWith(".json");
	}

	public void	reload(ResourceManager manager){
		synchronized (RegistryProxy.class)
		{
			RegistryProxy.EnableBypass();

			if (RegistryProxy.HasDefault())
				RegistryProxy.RestoreDefault();
			else
				RegistryProxy.CaptureDefault();

			// // To test for thread safety, look at a bunch of custom paintings in singleplayer,
			// // and `/reload` with this code enabled.
			// try {
			// 	Thread.sleep(1000);
			// }
			// catch(InterruptedException e)
			// {}

			for (Entry<Identifier, Resource> entry : manager.findResources("paintings", this::ShouldReadFile).entrySet())
			{
				PaintingVariant variant;
				try (InputStream stream = entry.getValue().getInputStream())
				{
					DataPaint.LOGGER.info("Reading file: {}", entry.getKey());
					JsonElement json = JsonParser.parseReader(new InputStreamReader(stream));
					JsonVariant jVar = gson.fromJson(json, JsonVariant.class);
					variant = new PaintingVariant(jVar.width*16, jVar.height*16);
				}
				catch(IOException e){
					DataPaint.LOGGER.error("Unable to read {} :\n {}", identifier.toString(), e);
					continue;
				}

				Identifier id;
				{
					id = entry.getKey();
					String name = id.getPath();
					name = name.substring(0, name.lastIndexOf(".json"));
					name = name.substring("paintings/".length());
					id = new Identifier(id.getNamespace(), name);
				}

				RegistryProxy.AddOrUpdateVariant(id, variant);
			}

			RegistryProxy.DisableBypass();
		}
	}
}
