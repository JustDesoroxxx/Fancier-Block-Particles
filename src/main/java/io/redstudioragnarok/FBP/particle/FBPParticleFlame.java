package io.redstudioragnarok.FBP.particle;

import io.redstudioragnarok.FBP.FBP;
import io.redstudioragnarok.FBP.renderer.CubeBatchRenderer;
import io.redstudioragnarok.FBP.renderer.RenderType;
import io.redstudioragnarok.FBP.renderer.color.ColorUtil;
import io.redstudioragnarok.FBP.renderer.light.LightUtil;
import io.redstudioragnarok.FBP.renderer.texture.TextureUtil;
import io.redstudioragnarok.FBP.vector.Vector3D;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static io.redstudioragnarok.FBP.FBP.snowTexture;

public class FBPParticleFlame extends ParticleFlame {

	Minecraft mc;

	double startScale, scaleAlpha, prevParticleScale, prevParticleAlpha;
	double endMult = 1;

	boolean spawnAnother;

	Vector3D startPos;

	final float AngleY;

	protected FBPParticleFlame(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double mY, boolean spawnAnother) {
		super(worldIn, xCoordIn, yCoordIn - 0.06, zCoordIn, 0, mY, 0);
		IBlockState blockState = worldIn.getBlockState(new BlockPos(posX, posY, posZ));

		this.spawnAnother = spawnAnother;

		if (blockState == Blocks.TORCH.getDefaultState())
			prevPosY = posY = posY + 0.04;

		startPos = new Vector3D((float) posX, (float) posY, (float) posZ);

		mc = Minecraft.getMinecraft();

		this.motionY = -0.00085;
		this.particleGravity = -0.05f;

		this.particleTexture = snowTexture;

		particleScale *= FBP.scaleMult * 2.5;
		particleMaxAge = FBP.random.nextInt(3, 5);

		this.particleRed = 1;
		this.particleGreen = 1;
		this.particleBlue = 0;

		AngleY = rand.nextFloat() * 80;

		particleAlpha = 1;

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.9875, 1);

		multipleParticleScaleBy(1);
	}

	@Override
	public Particle multipleParticleScaleBy(float scale) {
		Particle particle = super.multipleParticleScaleBy(scale);

		startScale = particleScale;
		scaleAlpha = particleScale * 0.35;

		float newScale = particleScale / 80;

		this.setBoundingBox(new AxisAlignedBB(posX - newScale, posY - newScale, posZ - newScale, posX + newScale, posY + newScale, posZ + newScale));

		return particle;
	}

	@Override
	public int getFXLayer() {
		return 0;
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;

		if (!FBP.fancyFlame)
			this.isExpired = true;

		if (++this.particleAge >= this.particleMaxAge) {
			if (FBP.randomFadingSpeed)
				particleScale *= 0.95 * endMult;
			else
				particleScale *= 0.95;

			if (particleAlpha > 0.01 && particleScale <= scaleAlpha) {
				if (FBP.randomFadingSpeed)
					particleAlpha *= 0.9 * endMult;
				else
					particleAlpha *= 0.9;
			}

			if (particleAlpha <= 0.01)
				setExpired();
			else if (particleAlpha <= 0.325 && spawnAnother && world.getBlockState(new BlockPos(posX, posY, posZ)).getBlock() == Blocks.TORCH) {
				spawnAnother = false;

				mc.effectRenderer.addEffect(new FBPParticleFlame(world, startPos.x, startPos.y, startPos.z, 0, false));
			}
		}

		motionY -= 0.02 * this.particleGravity;
		move(0, motionY, 0);
		motionY *= 0.95;

		if (this.onGround) {
			this.motionX *= 0.89;
			this.motionZ *= 0.89;
		}
	}

	@Override
	public void move(double x, double y, double z) {
		double Y = y;

		List<AxisAlignedBB> list = this.world.getCollisionBoxes(null, this.getBoundingBox().expand(x, y, z));

		for (AxisAlignedBB axisalignedbb : list) {
			y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
		}

		this.setBoundingBox(this.getBoundingBox().offset(0, y, 0));

		for (AxisAlignedBB axisalignedbb : list) {
			x = axisalignedbb.calculateXOffset(this.getBoundingBox(), x);
		}

		this.setBoundingBox(this.getBoundingBox().offset(x, 0, 0));

		for (AxisAlignedBB axisalignedbb : list) {
			z = axisalignedbb.calculateZOffset(this.getBoundingBox(), z);
		}

		this.setBoundingBox(this.getBoundingBox().offset(0, 0, z));

		// RESET
		resetPositionToBB();
		this.onGround = y != Y;
	}

	@Override
	protected void resetPositionToBB() {
		AxisAlignedBB axisalignedbb = this.getBoundingBox();
		this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2;
		this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2;
		this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float x = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
		float y = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
		float z = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		int brightness = getBrightnessForRender(partialTicks);

		float alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);

		float scale = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

		if (this.particleAge >= this.particleMaxAge)
			this.particleGreen = (float) (scale / startScale);

		scale *= 0.0125F;

		CubeBatchRenderer.renderCube(RenderType.BLOCK_TEXTURE, x, y, z, 0.0F, AngleY, 0.0F, scale, scale, scale,
				TextureUtil.pointTexCoordProvider(particleTexture.getInterpolatedU(4.4F), particleTexture.getInterpolatedV(4.4F)),
				ColorUtil.multiplyingColorProvider(particleRed, particleGreen, particleBlue, alpha, 0.95F),
				LightUtil.uniformLightCoordProvider(brightness));
	}

	@Override
	public int getBrightnessForRender(float partialTick) {
		return LightUtil.getCombinedLight(world, posX, posY, posZ);
	}
}
