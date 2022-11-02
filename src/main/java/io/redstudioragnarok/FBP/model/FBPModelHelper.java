package io.redstudioragnarok.FBP.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class FBPModelHelper {

	static int vertexes = 0;

	static boolean isAllCorruptedTexture = true;

	public static boolean isModelValid(IBlockState state) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);

		if (model.getParticleTexture().getIconName().equals("missingno"))
			return false;

		vertexes = 0;

		try {
			FBPModelTransformer.transform(model, state, 0, (quad, element, data) -> {
				if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION)
					vertexes++;

				if (!quad.getSprite().getIconName().equals("missingno"))
					isAllCorruptedTexture = false;

				return data;
			});
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

		return (vertexes >= 3) && !isAllCorruptedTexture;
	}
}
